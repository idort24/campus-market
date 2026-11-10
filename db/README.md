# 数据库交付说明（campus_market）

本目录是「校园轻集市」数据库这条线的完整交付物。负责人：张爱阳。

---

## 一、文件清单

| 文件 | 用途 | 什么时候用 |
|---|---|---|
| `01_schema.sql` | 17 张表的建表语句（含 DROP，可重复执行） | 全新初始化时第一个跑 |
| `02_seed.sql` | 角色、权限、字典、分类、测试账号的初始数据 | 建完表后第二个跑，只跑一轮 |
| `03_check.sql` | 17 项自检报表 | 初始化后验收，每次改库后复核 |
| `04_upgrade_20261109.sql` | 已有旧库的增量修复（不改表结构） | 库里已有数据、不想重建时用 |
| `05_demo_data.sql` | 24 条演示商品 + 分类统计快照（可重复执行） | 前端关掉 mock 后页面空白时跑，灌演示数据 |
| `06_upgrade_phone_20261109.sql` | 给 `sys_user` 补 `phone` 列 + 唯一索引 | **仅存量库需要**；全新初始化不用跑（`01` 已含该列） |
| `07_menu_permissions_20261109.sql` | 菜单权限从 7 条补到 11 条（学生 6 条 + 后台 5 条） | **仅存量库需要**；全新初始化不用跑（`02` 已含 11 条） |
| `_backup_before_upgrade_20261109.sql` | 升级前的完整备份 | 出问题回滚用 |

> 根目录那个 `campus_market.sql` 是旧文件，**不要再用**。它的测试账号密码密文被 PowerShell 的 `$` 变量解析截断成了 32 字符（完整应为 60 字符），拿它导入后登录必然失败。原因见第四节第 3 条。

---

## 二、两种使用场景

### 场景 A：全新初始化（推荐，给你自己做）

```powershell
# 1. 建表
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 -e "source D:/files/cm/db/01_schema.sql"

# 2. 灌数据
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 -e "source D:/files/cm/db/02_seed.sql"

# 3. 验收（输出 17 行 OK 才算成功）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 --table -e "source D:/files/cm/db/03_check.sql"

# 4. 可选：灌演示数据（24 条商品，前端页面才有内容）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 --table -e "source D:/files/cm/db/05_demo_data.sql"
```

### 场景 B：库里已有数据，只想补齐

```powershell
# 1. 修 user_type、补账号状态字典、补 7 条菜单权限（可重复执行）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 -e "source D:/files/cm/db/04_upgrade_20261109.sql"

# 2. 补 sys_user.phone 列（只跑一次，ALTER 不可重复执行）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 -e "source D:/files/cm/db/06_upgrade_phone_20261109.sql"

# 3. 菜单权限补到 11 条（可重复执行）
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --user=root --password=你的密码 --default-character-set=utf8mb4 --table -e "source D:/files/cm/db/07_menu_permissions_20261109.sql"
```

`04` 只做三件事：修 `user_type`、补账号状态字典、补 7 条菜单权限。不改任何表结构、不删业务数据，可重复执行。

`06` 是 2026-11-09 前端联调时补的：后端 `SysUser` 实体有 `phone` 字段，建表时漏了，导致所有登录请求报
`Unknown column 'phone' in 'field list'` 直接 500。跑完 `06` 登录才恢复。

`07` 补的是另外 4 条学生端菜单（`menu:user:product` `/product`、`menu:user:publish` `/publish`、
`menu:user:favorites` `/my-favorites`、`menu:user:ai` `/ai`）。补完 `sys_permission` 的 MENU 从 7 条变 11 条，
管理员拿到全部 11 条、学生拿 6 条，`GET /api/v1/user/menus` 才返回完整菜单（此前接口能通但列表缺项）。
这 4 条路径对应 `campus-market/src/router/index.js` 里真实存在的路由，前端页面都已到位，缺的只是库里的数据。

---

## 三、测试账号

| 学号 | 手机号 | 密码 | 昵称 | user_type | 角色 |
|---|---|---|---|---|---|
| `2024110640` | `13800000001` | `123456` | 张爱阳 | 9 管理员 | ADMIN（23 项权限） |
| `2024777777` | `13800000002` | `123456` | 测试学生 | 1 普通学生 | STUDENT（12 项权限） |

密码是 BCrypt 密文入库，明文 `123456` 已用后端 `PasswordUtil.matches()` 实测校验通过（`true`），错误密码返回 `false`。

登录账号口径（实测确认，见 `SysUserController.login`）：

- **密码登录**：`username` 传**学号**，后端按 `student_no` 查
- **短信登录**：`username` 传**手机号**，后端按 `phone` 查
- 不认邮箱、不认昵称

---

## 四、权限模型（重点，别改错）

### 4.1 两类权限

`sys_permission.perm_type` 只有三种取值：

| 值 | 含义 | perm_path 存什么 | 当前数量 |
|---|---|---|---|
| `API` | 后端接口权限，同时被前端 `v-permission` 当作按钮权限码 | 接口路径 | 16 |
| `MENU` | 前端左侧菜单项 | vue-router 路由 | 7 |
| `BUTTON` | 纯按钮权限，预留未用 | — | 0 |

### 4.2 六个管理权限码是红线

后端 `AuthInterceptor.ADMIN_PERMS` 里写死了这 6 个，字符必须完全一致：

```
user:manage   user:status   goods:audit
report:handle  category:manage  dict:manage
```

**改一个字符，管理端所有接口立刻全部 403。**

### 4.3 角色权限分配

- ADMIN：23 项全给（16 API + 7 MENU）
- STUDENT：12 项 = 10 个基础买卖 API + 2 个用户端菜单
- 学生不应拥有任何管理码，`03_check.sql` 第 8 项专门查这个

---

## 五、已知问题与修复

### 问题 1：关联表「逻辑删除 + 唯一键」会报 1062（已实测，需后端配合）

**现象**：管理员在后台第二次或第三次重新分配角色权限时，接口 500，MySQL 报错：

```
ERROR 1062: Duplicate entry '1-1-1' for key 'sys_role_permission.uk_role_perm'
```

**根因**：`RbacServiceImpl.assignRolePermissions()` 是「先删旧的、再插新的」全量覆盖，而删除走的是 MyBatis-Plus 的 `@TableLogic` 逻辑删除：

```java
rolePermMapper.delete(wrapper);      // 实际执行 UPDATE ... SET is_deleted = 1
rolePermMapper.insert(rp);           // 再插入 is_deleted = 0 的新行
```

唯一键是 `(role_id, permission_id, is_deleted)`：
- 第 1 次分配：插入 (1,1,0)，正常
- 第 2 次分配：删成 (1,1,1)，再插 (1,1,0)，正常
- 第 3 次分配：要把 (1,1,0) 删成 (1,1,1)，但这一行已经存在 → **1062**

**我试过的错误修法（不要照做）**：把唯一键改成 `(role_id, permission_id)`。实测结果更糟——第 2 次就报 `Duplicate entry '1-1'`，因为逻辑删除的旧行还占着位。所以我已经把索引还原成原设计。

**正确修法（要马智贤改代码）**，二选一：

- **方案 A（推荐）**：这三张关联表改物理删除。在 `RbacServiceImpl` 里把 `delete(wrapper)` 换成真实 DELETE，或者干脆去掉实体上的 `@TableLogic`。
  涉及三处：`assignRolePermissions`、`assignUserRoles`、收藏功能的取消收藏。
- **方案 B**：保留逻辑删除，但 insert 前先物理清理同键的旧行（`DELETE FROM ... WHERE is_deleted = 1`）。

改完代码后，可以顺手把唯一键收紧成不含 `is_deleted`（我准备了语句，但**不要在后端改代码之前执行**，否则报错提前）。

**受影响的三张表**：`sys_user_role`、`sys_role_permission`、`goods_collect`。

**答辩风险提示**：如果老师现场演示「给角色重新分配权限」点两次以上，很可能撞到这个 500。改代码前建议演示时只点一次，或者提前跟马智贤确认已修。

### 问题 2：注册接口和表约束对不上（需后端配合）

`sys_user` 有三个 NOT NULL + UNIQUE 字段：`student_no`、`email`、`nick_name`。

但 `SysUserController.register` 只要求传 `studentNo` 和 `password`：

```java
@PostMapping("/register")
public R register(@RequestBody SysUser sysUser){
    // 只查了学号重复，没管 email 和 nick_name
```

**后果**：不传 `email` 或 `nickName` 时，INSERT 直接触发 NOT NULL 约束报错，前端收到 500。

**修法（马智贤）**：注册接口必须校验并接收 `email`、`nickName`，同时补这两个字段的重复检查（表上有 UNIQUE）。或者给这两个字段加默认值（不推荐，会造出空邮箱账号）。

`03_check.sql` 不检查这一项，因为它是接口层问题，不是数据层问题。

### 问题 3：旧 SQL 文件里的密码密文被截断（已修复）

旧 `campus_market.sql` 里写的是：

```
.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq     -- 32 字符，坏的
```

正确值应为 60 字符：

```
$2b$12$k4q.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq
```

原因：BCrypt 密文前缀有三段 `$`，在 PowerShell 里被当成变量解析（`$2b`、`$12`、`$k4q`）吃掉了。

**规矩**：以后任何含 BCrypt 密文的 SQL，都不要用 `echo`、双引号字符串、`Set-Content` 去拼。写成 `.sql` 文件后 `source` 执行，或者用单引号 here-string。`02_seed.sql` 里已经存的是正确值，别再手抄。

### 问题 4：学生账号 user_type 曾是 0（已修复）

旧脚本写的是 `VALUES ('2024777777', ..., 0, 'NORMAL', 0)`，`0` 不在合法范围（表注释写明 `1普通学生 9管理员`）。前端路由守卫用 `userType === 9` 判断管理员，0 虽不会被误判为管理员，但任何按 `user_type = 1` 统计学生的查询都会漏掉它。

`04_upgrade` 第一段已经把它改回 1，主库当前已修正。

### 问题 5：旧库一条 MENU 权限都没有（已补）

旧库 16 条权限全是 `API` 类型。前端 `router/index.js` 目前用静态路由 + `roleCode === 'ADMIN'` 判断，还没消费菜单数据，所以不影响现在的功能。

但补上这 7 条有两个理由：
1. 答辩要讲「菜单由权限表驱动」，得有数据支撑；
2. 后续 `/api/v1/user/menus` 接口（马智贤任务第 8 项）需要它。

**注意**：前端尚未接入这套菜单，目前是静态路由。别在答辩时说「菜单完全由数据库动态生成」，会被追问代码在哪。

---

### 问题 6：`sys_user` 缺 `phone` 列，登录直接 500（已修复）

**现象**（2026-11-09 前端切真后端实测）：`POST /api/v1/user/login` 返回 `{"code":500,"msg":"服务器开小差了"}`，后端日志堆栈：

```
Caused by: java.sql.SQLSyntaxErrorException: Unknown column 'phone' in 'field list'
```

**根因**：后端实体 `SysUser` 有 `private String phone; // 手机号 phone（注册必填，唯一索引）`，
但 `01_schema.sql` 建表时漏了这一列。登录时 `SELECT ... phone ... FROM sys_user` 直接抛 SQL 异常，
密码登录、短信登录、注册全部不可用。

**影响**：整条登录链路瘫痪。这是「前后端都对、库少一列」的典型。

**已修**：
- `01_schema.sql` 的 `sys_user` 增加 `phone varchar(20)` + `uk_phone` 唯一索引（全新库直接可用）
- `02_seed.sql` 两个测试账号带手机号
- 存量库跑 `06_upgrade_phone_20261109.sql` 补列

**验证**：补列后重新登录 → `code=1`，拿到 token，`roleCode=ADMIN`、23 项权限正常返回。

---

## 六、表结构总览（17 张）

### RBAC（5）

| 表 | 作用 | 关键字段 |
|---|---|---|
| `sys_user` | 用户 | `student_no` 登录账号、`password` BCrypt、`user_type` 1/9、`account_status` |
| `sys_role` | 角色 | `role_code` ADMIN/STUDENT，内置角色禁删 |
| `sys_permission` | 权限 | `perm_code` 鉴权依据、`perm_type` MENU/API/BUTTON、`perm_path` |
| `sys_user_role` | 用户-角色 | `uk_user_role (user_id, role_id, is_deleted)` |
| `sys_role_permission` | 角色-权限 | `uk_role_perm (role_id, permission_id, is_deleted)` |

### 业务（12）

| 表 | 作用 |
|---|---|
| `goods` | 商品，含审核状态、上下架状态、AI 生成标记 |
| `goods_category` | 分类树（邻接表，`parent_id = 0` 为根） |
| `goods_collect` | 收藏 |
| `goods_stat` | 商品预聚合统计快照 |
| `trade_order` | 线下意向订单（无线上支付） |
| `evaluation` | 交易双向评价 |
| `chat_message` | 聊天消息，含 Agent 自动回复、诈骗风险词标记 |
| `report_work_order` | 举报工单 |
| `dict_type` / `dict_data` | 字典类型 / 字典数据 |
| `rag_history_record` | RAG 历史成交知识库元数据（向量在 ChromaDB） |
| `demo_file_blob` | 演示用的 BLOB 存文件表，业务不使用，仅答辩对比 |

### 字典现状（5 类 19 条）

| 字典编码 | 条数 | 取值 |
|---|---|---|
| `goods_status` | 3 | ON_SHELF / OFF_SHELF / SOLD |
| `audit_status` | 3 | PENDING / PASS / REJECT |
| `goods_quality` | 5 | NEW / 90 / 80 / 70 / USED |
| `order_status` | 4 | WAIT_CONFIRM / WAIT_TRADE / FINISH / CANCEL |
| `account_status` | 4 | NORMAL / WARN / LIMIT / BAN |

---

## 七、常见报错排查

| 报错 | 原因 | 解决 |
|---|---|---|
| `ERROR 1045 Access denied` | 密码不对 | `application.yaml` 里配的是 `root / sys20261013`，确认改过密码没有 |
| `ERROR 1046 No database selected` | `-e` 里没带库名 | 加 `--database=campus_market` 或 SQL 里写 `USE` |
| `ERROR 1062 Duplicate entry` | 唯一键冲突 | 大概率是第五节问题 1，看是哪个索引名 |
| `ERROR 1061 Duplicate key name` | 索引已存在 | 升级脚本里的 ALTER 已经执行过一次，跳过即可 |
| `ERROR 1054 Unknown column` | 库结构比实体旧 | 跑 `01_schema.sql` 重建，或补对应字段 |
| 登录返回「账号或密码错误」但密码确实是 123456 | 密文被截断 | `SELECT LENGTH(password)` 应为 60，不是就重跑 `02_seed.sql` |
| 管理端接口 403 | 权限码对不上 | `03_check.sql` 第 5 项应显示 6，不是就补 `02` / `04` |
| 中文乱码 | 连接字符集 | 命令里加 `--default-character-set=utf8mb4` |

---

## 八、交接给别人时要说清的事

给**马智贤**（后端）：

1. 注册接口必须补 `email`、`nickName` 两个字段和重复校验（第五节问题 2）
2. 三张关联表的删除改成物理删除（第五节问题 1），改完告诉我，我把唯一键收紧
3. `/api/v1/user/permissions` 现在返回 `{roles, perms}`，前端已经在用；`/user/menus` 还没实现

给**何曼妮**（前端）：

1. 菜单权限 7 条已在库里，`perm_path` 与 `router/index.js` 的路径完全对齐，可以直接用
2. `v-permission` 用 API 类的权限码，例如 `v-permission="'goods:audit'"`
3. 管理员判断条件现在是 `roleCode === 'ADMIN' || userType === 9`，数据层已保证两者一致

给**周泓颖**（工程化）：

1. 库名 `campus_market`，账号 `root / sys20261013`，与 `application.yaml` 一致
2. Redis 不参与本目录任何逻辑，纯 MySQL

---

## 九、验证记录（2026-11-09）

| 验证项 | 方法 | 结果 |
|---|---|---|
| 全新初始化 | 临时库 `campus_market_verify` 从零跑 01 + 02 + 03 | 17 项全 OK |
| 增量升级 | 主库跑 04 | 17 项全 OK |
| 密码可登录 | 用后端 `PasswordUtil.matches("123456", 库中密文)` | `true`；错误密码 `false` |
| 密文完整性 | `SELECT LENGTH(password)` | 两个账号均 60 |
| 唯一键缺陷 | 事务内模拟第三次重分配 | 复现 1062，已记录在第五节 |

临时验证库 `campus_market_verify` 用完可以删：

```sql
DROP DATABASE campus_market_verify;
```
