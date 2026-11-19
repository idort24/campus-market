# A/B 套验证页面（前端 · 何曼妮）

三个单文件 HTML，没有构建步骤，浏览器直接打开。用于课程实验 A 套（Nginx 多实例负载均衡）与 B 套（事件统计大屏）的现场验证与演示。

## 文件

| 文件 | 对应实验 | 验证什么 |
| --- | --- | --- |
| `统计大屏.html` | B 套 | WebSocket 接收、version 前端去重、REST 兜底、断流重连 |
| `Tomcat回显测试页.html` | A 套 | 请求落到哪个实例（`nodeName` / `X-Instance`）、四种负载策略的实际分布 |
| `静态资源页.html` | A 套 | gzip、缓存、传输大小、响应头诊断 |
| `assets/纹理.svg` | — | 静态资源页的测量对象 |
| `assets/echarts.min.js` | B 套 | ECharts 5.5.1 本地副本，离线/断网可渲染，不再走 CDN |

## 怎么跑

推荐放进 Nginx 的静态目录，同源访问：

```
http://<nginx主机>:8080/static/统计大屏.html
```

**不要本地双击打开**。`file://` 协议下 REST 请求会被浏览器按跨域拦掉，回显页会一直显示“请求失败”。

页面上的 WS 地址和 REST 地址默认按当前入口同源推导（换机器/换网络不用手改），也能在界面里直接手动覆盖；`file://` 直开时兜底 `127.0.0.1:8090`。

## 依赖的后端接口

| 接口 | 用途 |
| --- | --- |
| `ws://<host>/ws/consult` | 快照广播，250ms 一次，无人在线跳过不发，重连不补发历史 |
| `GET /api/v1/consult/snapshot` | 全量兜底，返回 `{code,msg,data}`，要取 `data` |
| `GET /api/v1/consult/node` | 实例回显：`nodeName` / `port` / `ip` / `host` / `cacheStore` / `redis` |
| `POST /api/v1/consult/snapshot/rebuild` | 从 `event_log` 重算快照，缺表时后端会返回缺表提示 |

## B 套字段与校验规则

快照字段：`version` / `totalEvents` / `sessionCount` / `typeDistribution{view,favorite,order}` / `minuteTrend[{minute,count}]` / `backlogCount` / `snapshotTime`，另有 `tps` / `successRate` / `dedupBlocked`。

前端校验规则：`version = MAX(event_log.id)`，收到的 `version` **小于等于**当前已展示值就丢弃（相等也丢）。后端不保证严格有序、也不保证不重复，这一层由前端兜住。

断流判定：250ms 推一次，超过 5 秒（20 个周期）没有快照就主动重连。浏览器 WebSocket API 发不出原生 ping frame，所以这里不用心跳包，走的是被动超时检测。

## 环境自检

`_health_check.py` 只读跑一遍关键通路（入口、四种策略、静态目录、REST 快照、WS 推送），不写库、不改服务：

```powershell
python _health_check.py 10.37.11.159
```

地址作为第一个参数传入；不传时读环境变量 `LAB_HOST`，再没有才用脚本里的默认值。8 项全部 `[OK]` 才适合开始演示。

## 注意

- 页面打开时默认跑**内置模拟数据**，点「连接」才切到真实端点。演示或截图时不要把模拟数据当真实统计讲。
- 模拟数据每 20 条会故意重发一次相同 version，用来演示“相等也丢”。
- 集群里 `/ws/` 固定落 node-1，快照版本在同一条序列上递增；「版本回落」提示是兜底，正常不应触发。
- 部署时把整个 `lab-arch-demo` 目录（HTML + `assets/`）一起拷进 Nginx 静态目录，单独拷 HTML 会丢 ECharts 和纹理图。
