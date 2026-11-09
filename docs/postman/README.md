# Postman 验收用例集（campus-market）

文件：`campus-market.postman_collection.json`

## 导入

Postman → Import → 选这个 json → 导入后集合名「campus-market 验收用例集（周泓颖）」。

导入后在集合变量里改 `baseUrl`：

| 场景 | baseUrl |
|---|---|
| 本机自测 | `http://localhost:8090` |
| 连马智贤的机器 | `http://<他的局域网IP>:8090`（vite 里当前是 172.20.10.2） |

其余变量（token / captchaKey / oldToken）由前面的请求自动写入，不用手填。

## 5 类验收怎么跑

| 类别 | 用例 | 说明 |
|---|---|---|
| 验证码 | 01 | 拿 `captchaKey` + `imageBase64`，key 自动存进集合变量 |
| 验证码 | 02 | 故意用 `0000` 错码，断言登录被拒（一次性作废要在 03 用真码之前跑 02，否则真码也被消耗掉） |
| 登录 | 03 | 用浏览器打开 01 返回的 base64 图肉眼认字，把 4 位填进 `captchaCode` 变量再跑 |
| 鉴权 | 04 / 05 / 06 | 权限码、菜单、越权 403。**05 会在菜单为空时失败，这是当前 `sys_permission` 没有 MENU 数据的真实信号，不是脚本 bug** |
| 退出黑名单 | 07 → 08 | 07 把旧 token 存入 `oldToken` 再清空 `token`，08 用旧 token 访问必须 401。这两条是 Redis 黑名单的直接证据 |
| 限流 | 09 | 同手机号 60 秒内第 6 次登录。手工连点 5 次 02，第 6 次跑 09；或用 Runner 把 09 迭代 6 次 |
| 缓存命中 | 10 | 连跑两次，看 Console 里的 `responseTime`，第二次应明显下降；同时 `docker exec campus-redis redis-cli keys "category:*"` 能看到 key |
| 穿透 | 11 | 50 并发打不存在 id，配合 MySQL `SHOW GLOBAL STATUS LIKE 'Com_select'` 前后差值做对比实验 |

## 已知依赖（现在跑必然失败的部分）

| 用例 | 依赖 |
|---|---|
| 01 / 02 / 03 / 07 / 08 | 马智贤的 `AuthController`（captcha / logout）+ 你的 Redis 存储 |
| 09 | 你的登录限流 |
| 10 | 你的字典/分类缓存 |
| 11 | 你的空值缓存 |
| 04 / 05 / 06 | 张爱阳的 RBAC 数据（`sys_permission` 补 MENU 行） |

现在能跑通的是 04（权限码）和 05 的"结构校验"，其余等对应功能落地。

## 成功码口径（未决，跑之前先确认）

分工表写「成功 1」，接口设计文档已统一改成「200」。本集合断言按 **code = 1** 写。若后端最终采用 200，打开集合 → 逐个用例的 Tests 标签，把 `pm.expect(body.code).to.eql(1)` 改成 `200`（共 4 处）。
