# 登录与鉴权接口契约（auth-api）

> 适用范围：campus-market 前端（何曼妮）、数据库（张爱阳）、缓存（周泓颖）、后端（马智贤）
> 状态：**已按后端现有代码落地**，前端可据此并行开发。
> 最后更新：2026-11-09

## 一、全局约定

| 项 | 约定 |
|---|---|
| Base URL | `/api/v1` |
| 成功码 | `code = 1` |
| 未登录 / token 失效 | HTTP 200 + `code = 401` |
| 无权限 | HTTP 200 + `code = 403` |
| 参数错误 | HTTP 200 + `code = 400` |
| 业务失败 | HTTP 200 + `code = 500` |
| 鉴权头 | `Authorization: Bearer <token>` |
| token 有效期 | 2 小时 |
| 异常处理 | 全局 `@RestControllerAdvice`：参数/格式错误转 400，其余转 500，堆栈只写日志 |

统一返回结构：

```json
{ "code": 1, "msg": "成功", "data": { } }
```

> 注意：本项目成功码是 **1**，不是 200。前端 `src/api/http.js` 已按 `code === 1` 判断，不要改。

---

## 二、接口清单

### 1. 获取图形验证码（无需登录）

```
POST /api/v1/auth/captcha
```

请求体：无

返回：

```json
{
  "code": 1,
  "msg": "成功",
  "data": {
    "captchaKey": "8f3a1c9d4b7e4c2a9f0d1e2b3c4d5e6f",
    "imageBase64": "data:image/png;base64,iVBORw0KGgo...",
    "expireIn": 60
  }
}
```

说明：
- `imageBase64` 直接给 `<img :src="imageBase64">` 用，已带 `data:image/png;base64,` 前缀。
- 验证码为**简单算术题**（如 `3+5=?`），用户填结果数字。
- 有效期 60 秒，**校验一次即失效**，失败必须重新获取。
- `captchaKey` 必须和用户填写的答案一起提交给登录接口。

---

### 2. 登录（无需登录）

```
POST /api/v1/user/login
```

请求体：

```json
{
  "username": "2024110640",
  "password": "123456",
  "captchaKey": "8f3a1c9d4b7e4c2a9f0d1e2b3c4d5e6f",
  "captchaCode": "8"
}
```

字段说明：
- `username` 传学号；后端兼容 `studentNo` 字段名。
- `captchaKey` / `captchaCode` 在 `auth.captcha.enabled=true`（默认）时必填；后端联调期可用配置临时关闭。

返回：

```json
{
  "code": 1,
  "msg": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "studentNo": "2024110640",
      "nickName": "马智贤",
      "avatar": null,
      "userType": 9,
      "accountStatus": "NORMAL",
      "roleCode": "ADMIN",
      "perms": ["user:manage", "goods:audit"]
    },
    "roles": ["ADMIN"],
    "perms": ["user:manage", "goods:audit"]
  }
}
```

错误场景：

| 场景 | 返回 |
|---|---|
| 验证码错 / 过期 | `code: 400`，`msg: "验证码错误或已失效，请刷新后重试"` |
| 账号或密码错 | `code: 500`，`msg: "账号或密码错误"`（两种错误统一文案，防账号枚举） |
| 账号被禁用 | `code: 403`，`msg: "账号状态异常（BANNED），请联系管理员"` |

登录成功后的副作用：
- 校验 `sys_user.account_status`，**只有 `NORMAL` 允许登录**；状态校验放在密码校验之后，避免通过返回信息探测账号状态。
- 写入 `last_login_time`（`LocalDateTime.now()`），后台用户列表的"最后登录"依赖该字段。

前端处理要点：
- `token` 存 `localStorage.setItem('token', res.token)`；
- `user` 整体存 `localStorage.setItem('userInfo', JSON.stringify(res.user))`；
- 路由守卫与按钮权限以后端 `perms` 为准，不再依赖本地硬编码 `roleCode`。

---

### 3. 注册（无需登录）

```
POST /api/v1/user/register
```

请求体：

```json
{
  "studentNo": "2024110641",
  "phone": "13800138000",
  "password": "123456",
  "nickName": "何曼妮"
}
```

校验规则（后端已实现）：
- 学号不能为空，且不可重复；
- 密码不能为空；
- 手机号为 11 位大陆手机号（`^1[3-9]\d{9}$`），且不可重复；
- 密码用 BCrypt 加密存储（自带随机盐，同密码不同用户密文不同）。

返回：`{"code":1,"msg":"注册成功","data":null}`

错误：

| 场景 | msg |
|---|---|
| 学号为空 | 学号不能为空 |
| 密码为空 | 密码不能为空 |
| 手机号格式错 | 请输入正确的手机号 |
| 学号已存在 | 该学号已注册 |
| 手机号已存在 | 该手机号已被注册 |

注册成功后后端自动绑定 `STUDENT` 角色。

---

### 4. 退出登录（需要 token）

```
POST /api/v1/auth/logout
Authorization: Bearer <token>
```

请求体：无

返回：`{"code":1,"msg":"已退出登录","data":null}`

行为：
- 把当前 token 写入黑名单（`token:black:{token}`），**TTL = token 剩余有效秒数**；
- 之后用同一个 token 请求任何受保护接口都会返回 401。

前端要点：**必须先调该接口成功，再清本地 `token` / `userInfo`**；只清本地等于没退出。

---

### 4b. 忘记密码 / 重置密码（无需登录）

```
POST /api/v1/auth/password/reset
```

请求体：

```json
{ "phone": "13800138000", "captchaKey": "xxx", "captchaCode": "8", "newPassword": "reset123" }
```

返回：`{"code":1,"msg":"密码重置成功，请用新密码登录","data":null}`

校验顺序与错误码：

| 场景 | code | msg |
|---|---|---|
| 手机号为空 | 400 | 请输入手机号 |
| 手机号格式不对 | 400 | 请输入正确的手机号 |
| 新密码少于 6 位 | 400 | 新密码至少 6 位 |
| 验证码错/过期/重复使用 | 400 | 验证码错误或已失效，请刷新后重试 |
| 手机号未注册 | 400 | 该手机号未注册 |

说明：
- 项目暂无短信通道，当前用**图形验证码**承担"确认本人操作"的作用，接口参数结构不变；
- 后续接入短信服务，只需把 `CaptchaService.verify()` 换成短信校验；
- 重置成功后**不会自动登录**，前端引导回登录页；
- 该路径已在 `WebConfig` 放行，不需要 token。

---

### 5. 当前用户权限（需要 token）

```
GET /api/v1/user/permissions
```

返回：

```json
{
  "code": 1,
  "data": {
    "roles": ["ADMIN"],
    "perms": ["user:manage", "user:status", "goods:audit", "report:handle", "category:manage", "dict:manage"]
  }
}
```

用途：按钮级权限（`v-permission="'goods:audit'"`）。

---

### 6. 当前用户可见菜单（需要 token）

```
GET /api/v1/user/menus
```

返回：

```json
{
  "code": 1,
  "data": [
    { "id": 1, "permCode": "menu:admin:users", "title": "用户管理", "path": "/admin/users", "parentId": 0 },
    { "id": 2, "permCode": "menu:admin:goods", "title": "商品管理", "path": "/admin/goods", "parentId": 0 }
  ]
}
```

用途：前端按 `path` 动态生成路由与侧边栏；普通学生只拿到学生端菜单，管理员额外拿到后台菜单。

---

### 7. 当前用户信息（需要 token）

```
GET /api/v1/user/info
```

返回：`{code:1,data:{...SysUser，password 为 null}}`

---

## 三、权限码字典（数据库种子必须一字不差）

后端 `AuthInterceptor` 里的管理端权限判定集合是写死的，库里写成别的字符串会导致永久 403：

```
user:manage
user:status
goods:audit
report:handle
category:manage
dict:manage
```

权限类型字段约定（`sys_permission.perm_type`）：

| 值 | 含义 | 关键字段 |
|---|---|---|
| `MENU` | 菜单资源 | `perm_path` 存前端路由地址 |
| `BUTTON` | 按钮资源 | `perm_code` 存权限码 |
| `API` | 接口资源 | `perm_path` 存后端 URL |

> 这是讲解里点名的考点：菜单对应的是 **URL**，按钮对应的是 **操作权限码**。

---

## 四、数据库现状核查（2026-11-09 实测，张爱阳按此对齐）

已连 `campus_market` 实测，现状如下：

| 项 | 现状 | 待办 |
|---|---|---|
| 表 | 已建 18 张（含 `sys_user`、`sys_permission`、`sys_role`、`sys_user_role`、`sys_role_permission` 等） | — |
| `sys_user.phone` | **已存在**（`varchar(20)`，可空） | 建议加唯一索引（注册时后端已做查重，数据库层再加一道） |
| `sys_user.password` | `varchar(128)` | 够存 BCrypt 密文（60 字符），无需改 |
| `sys_user.account_status` | `varchar(30)`，默认 `NORMAL` | 前端展示注意取值是 `NORMAL`，不是 `ACTIVE` |
| `sys_permission` | 16 条，`perm_type` **全部是 `API`** | **必须补 `MENU` 类型权限**，否则 `/user/menus` 永远返回空数组 |
| 缺失表 | `stored_file`、`file_upload_session` | 分片上传功能要用，按实体补建 |

### 菜单权限种子数据建议（学生会用到的）

| perm_code | perm_name | perm_type | perm_path | sort |
|---|---|---|---|---|
| `menu:user:product` | 商品集市 | `MENU` | `/product` | 1 |
| `menu:user:publish` | 发布闲置 | `MENU` | `/publish` | 2 |
| `menu:user:mine` | 我的发布 | `MENU` | `/my-publishes` | 3 |
| `menu:user:favorites` | 我的收藏 | `MENU` | `/my-favorites` | 4 |
| `menu:user:orders` | 我的订单 | `MENU` | `/my-orders` | 5 |
| `menu:user:ai` | 智能助手 | `MENU` | `/ai` | 6 |
| `menu:admin:users` | 用户管理 | `MENU` | `/admin/users` | 21 |
| `menu:admin:goods` | 商品管理 | `MENU` | `/admin/goods` | 22 |
| `menu:admin:categories` | 分类管理 | `MENU` | `/admin/categories` | 23 |
| `menu:admin:reports` | 举报处理 | `MENU` | `/admin/reports` | 24 |
| `menu:admin:roles` | 角色权限 | `MENU` | `/admin/roles` | 25 |

管理员拿到全部 11 条，学生只拿前 6 条。

---

## 五、缓存键约定（周泓颖接 Redis 时对齐）

| 键 | 值 | TTL |
|---|---|---|
| `captcha:{captchaKey}` | 验证码答案 | 60 秒 |
| `token:black:{token}` | 拉黑时间戳 | token 剩余有效期 |
| `login:fail:{账号}` | 失败次数 | 60 秒 |
| `dict:{类型}` | 字典 JSON | 不设 TTL（写时刷新） |

缓存键统一定义在 `com.campusmarket.common.CacheKeys`，不要在业务里硬编码字符串。

字典缓存已落地：`GET /dict/data/list?dictType=xxx` 走 Cache Aside，启动时 `DictWarmupRunner` 把 4 个字典类型（`goods_status / audit_status / goods_quality / order_status`）15 条数据全部预热进内存缓存，运行期不再查库；后续加写接口时"先写库、再删缓存"。

---

## 六、后端代码位置索引

| 内容 | 文件 |
|---|---|
| 验证码生成与校验 | `service/CaptchaService.java` |
| 验证码 / 退出接口 | `controller/AuthController.java` |
| 登录 / 注册 / 用户信息 | `controller/SysUserController.java` |
| 权限 / 菜单 | `controller/RbacUserController.java` |
| token 校验 + 黑名单 + 权限拦截 | `interceptor/AuthInterceptor.java` |
| 白名单配置 | `config/WebConfig.java` |
| 缓存抽象层 | `cache/CacheStore.java`、`cache/InMemoryCacheStore.java` |
| 缓存键常量 | `common/CacheKeys.java` |
| 全局异常处理 | `common/GlobalExceptionHandler.java` |
| 字典缓存预热 | `config/DictWarmupRunner.java` |

## 商品上下架接口（2026-11-09 补）

- `PUT /api/v1/goods/{id}/status?status=ON_SHELF|OFF_SHELF`
- 仅本人发布的商品可操作；`SOLD` 由订单流程触发，不允许手动改。
- 附带修了一个 schema 问题：`goods.goods_status` / `goods.audit_status` 原本是 int，与字典 String 设计不符，已 ALTER 为 `varchar(20)`（张爱阳后续同步到建表脚本）。
| 密码加密 / JWT 工具 | `utils/PasswordUtil.java`、`utils/JwtUtil.java` |

---

## 七、联调常用业务接口路径（给前端何曼妮）

| 前端要的功能 | 实际路径 |
|---|---|
| 我的发布 | `GET /api/v1/goods/my` |
| 我的收藏 | `GET /api/v1/goodsCollect/my`；取消收藏 `DELETE /api/v1/goodsCollect/{goodsId}`；是否已收藏 `GET /api/v1/goodsCollect/has/{goodsId}` |
| 我买的 / 我卖的 | `GET /api/v1/order/myBuy`、`GET /api/v1/order/mySell`；下单 `POST /api/v1/order/create`；确认收货 `PUT /api/v1/order/confirm/{id}`；取消 `PUT /api/v1/order/cancel/{id}` |
| 我的举报 | `POST /api/v1/report/add`、`GET /api/v1/report/my` |

### 聊天消息（新做，已走登录拦截）

```
GET  /api/v1/chat/messages?userId=对方id   # 拉取会话历史，按时间正序，进入会话自动把对方发来的未读标已读
POST /api/v1/chat/messages                 # 发消息
```

POST body：

```json
{ "receiverId": 5, "content": "你好，这件还在吗", "msgImage": "可选，图片地址" }
```

`fromUserId` 从 token 取，前端不传也不能伪造；返回整条 `ChatMessage`：

| 字段 | 含义 |
|---|---|
| `senderId` / `receiverId` | 发送 / 接收人 id |
| `msgContent` | 文本内容 |
| `msgImage` | 图片地址（可空） |
| `readFlag` | 0 未读，1 已读 |
| `isAiMessage` | 0 用户消息，1 智能助手回复 |
| `createTime` | 发送时间 |

错误：缺 `receiverId` / 格式错 / 发给自己 / 内容全空 → 400；未带 token → 401。
