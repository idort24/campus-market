# -*- coding: utf-8 -*-
"""演示前环境自检（只读，不写库）。双击「演示前自检.cmd」运行。"""
import base64
import json
import os
import socket
import struct
import sys
import time
import urllib.error
import urllib.request

sys.stdout.reconfigure(encoding="utf-8")

HOST = sys.argv[1] if len(sys.argv) > 1 else os.environ.get("LAB_HOST", "10.37.11.159")
BASE = "http://%s:8080" % HOST
results = []


def check(label, ok, detail=""):
    results.append((label, ok, detail))
    print("%s %-26s %s" % ("[OK]  " if ok else "[FAIL]", label, detail))


def http(method, path, timeout=6):
    req = urllib.request.Request(BASE + path, method=method)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return r.status, r.read().decode("utf-8", "replace"), dict(r.headers)
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace"), dict(e.headers)
    except Exception as e:
        return None, repr(e), {}


def ws_probe(seconds=6):
    key = base64.b64encode(os.urandom(16)).decode()
    s = socket.create_connection((HOST, 8080), timeout=seconds)
    s.sendall(
        ("GET /ws/consult HTTP/1.1\r\nHost: %s\r\nUpgrade: websocket\r\n"
         "Connection: Upgrade\r\nSec-WebSocket-Key: %s\r\n"
         "Sec-WebSocket-Version: 13\r\n\r\n" % (HOST, key)).encode()
    )
    buf = b""
    while b"\r\n\r\n" not in buf:
        c = s.recv(4096)
        if not c:
            break
        buf += c
    head, _, rest = buf.partition(b"\r\n\r\n")
    first = head.split(b"\r\n")[0].decode("utf-8", "replace")
    if b"101" not in head.split(b"\r\n")[0]:
        s.close()
        return False, "握手失败：" + first
    s.settimeout(1)
    deadline = time.time() + seconds
    while time.time() < deadline:
        if len(rest) >= 2:
            ln = rest[1] & 0x7F
            idx = 2
            if ln == 126:
                ln = struct.unpack(">H", rest[2:4])[0]
                idx = 4
            elif ln == 127:
                ln = struct.unpack(">Q", rest[2:10])[0]
                idx = 10
            if len(rest) >= idx + ln:
                payload, op = rest[idx:idx + ln], rest[0] & 0x0F
                s.close()
                if op == 1:
                    try:
                        return True, "首帧 version=%s" % json.loads(payload.decode("utf-8"))["version"]
                    except Exception:
                        return True, "收到文本帧 %d 字节" % len(payload)
                return True, "收到帧 opcode=%d" % op
        try:
            rest += s.recv(4096)
        except socket.timeout:
            continue
    s.close()
    return False, "%d 秒内没有收到任何数据" % seconds


print("=" * 62)
print("演示前环境自检  ->  %s" % BASE)
print("=" * 62)

# 1 入口与节点回显
status, body, _ = http("GET", "/api/v1/consult/node")
if status == 200 and '"code":1' in body:
    try:
        node = json.loads(body)["data"]["nodeName"]
    except Exception:
        node = "?"
    check("Nginx 入口 + 节点回显", True, "-> %s" % node)
else:
    check("Nginx 入口 + 节点回显", False, "HTTP=%s  %s" % (status, body[:70]))

# 2 四种负载策略
for p in ["rr", "weight", "iphash", "least"]:
    status, body, headers = http("GET", "/%s/api/v1/consult/node" % p)
    inst = headers.get("X-Instance", "-")
    check("策略 /%s/" % p, status == 200 and inst != "-", "X-Instance=%s" % inst)

# 3 静态目录
status, body, _ = http("GET", "/static/")
check("静态目录 /static/", status == 200, "HTTP=%s" % status)

# 4 REST 快照兜底
status, body, _ = http("GET", "/api/v1/consult/snapshot", timeout=10)
ok, detail = False, ""
if status == 200:
    try:
        obj = json.loads(body)
        data = obj.get("data")
        if obj.get("code") == 1 and isinstance(data, dict):
            ok = True
            detail = "version=%s total=%s sessions=%s backlog=%s" % (
                data.get("version"), data.get("totalEvents"),
                data.get("sessionCount"), data.get("backlogCount"))
        else:
            detail = "code=%s msg=%s" % (obj.get("code"), obj.get("msg"))
    except Exception as e:
        detail = "JSON 解析失败：%r" % (e,)
else:
    detail = "HTTP=%s" % status
check("REST 快照兜底", ok, detail)

# 5 WebSocket 推送
try:
    ok, detail = ws_probe()
except Exception as e:
    ok, detail = False, "连接失败：%r" % (e,)
check("WebSocket 推送", ok, detail)

print()
bad = [r for r in results if not r[1]]
if bad:
    print("结论：%d 项未通过，先别开始演示。" % len(bad))
else:
    print("结论：全部通过，可以演示。")
print("")
