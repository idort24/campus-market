# Redis 接入联调记录（feature/redis）

> 记录人：周泓颖　日期：2026-11-09　分支：`feature/redis`（bab3ad2）

## 一、环境

| 组件 | 标识 | 端口 | 说明 |
|---|---|---|---|
| Redis | Docker 容器 `campus-redis` | 宿主 6380 → 容器 6379 | 512MB、allkeys-lru、AOF 开启、`database 0` |
| MySQL | Docker 容器 `huobajie-mysql` | 3307 | 库 `campus_market`（17 张表） |
| 后端 | Spring Boot 3.3.5 | 8090 | `cache.store=redis` 时启用 RedisCacheStore |

6379 被火把节项目的 `huobajie-redis` 占用，因此 campus 独立用 6380，两个实例天然隔离，不靠切换 database 隔离。

## 二、本地配置改动（仅本机，不提交）

`src/main/resources/application.yaml`：

```yaml
spring:
  data:
    redis:
      port: 6380          # 本机 6379 被占用，campus-redis 映射在 6380
  datasource:
    url: jdbc:mysql://localhost:3307/campus_market?...
    password: 123456      # 本地 campus_market 跑在 huobajie-mysql

cache:
  store: redis            # 联调期临时切 Redis，提交时保持 memory
```

提交策略：只带 `redis.port: 6380` 这一处，`cache.store` 保持仓库默认 `memory`，避免别人没起 Redis 时启动失败。

## 三、四项验收结果

| # | 验收项 | 结果 | 证据 |
|---|---|---|---|
| 1 | 登录（注册 → 验证码 → 登录） | 通过 | `POST /api/v1/user/login` 返回 `code:1`，data 含 token、roles=[STUDENT]、perms 10 条 |
| 2 | 验证码连续刷新两次 | 通过 | 每次生成新 `captcha:{uuid}`，Redis 中 TTL 实测 60 → 44 秒递减，旧 key 到期自动消失 |
| 3 | 退出后旧 token 立即 401 | 通过 | `POST /api/v1/auth/logout` 返回「已退出登录」；同一 token 再请求返回 `code:401 登录已退出，请重新登录` |
| 4 | 字典命中 Redis | 通过 | 启动预热写入 4 个 key：`dict:goods_status`、`dict:order_status`、`dict:audit_status`、`dict:goods_quality` |

补充验证：

- 黑名单 `token:black:{token}` TTL 实测 7180 秒，等于 token 剩余有效期（签发 7200 秒），说明 `JwtUtil.getRemainingSeconds` 对接正确。
- 验证码为一次性：校验通过后立即 `DEL`，无法复用。
- 最终 `DBSIZE = 5`（1 个黑名单 + 4 个字典），验证码过期后不残留。

## 四、复现命令

```bash
# 1. 起 Redis 与 MySQL
docker start campus-redis huobajie-mysql
docker exec campus-redis redis-cli ping          # PONG

# 2. 启动后端（cache.store=redis）
mvnw.cmd spring-boot:run

# 3. 观察 key
docker exec campus-redis redis-cli keys "*"
docker exec campus-redis redis-cli ttl "captcha:<key>"
docker exec campus-redis redis-cli ttl "token:black:<token>"
```

## 五、联调中发现的两个数据缺口（需张爱阳处理）

1. `sys_user` 表缺 `phone` 列：实体与注册接口要求手机号必填，但建表脚本没有该列，注册直接 500。本地已临时 `ALTER TABLE` 补列并加唯一索引，正式 `db/schema.sql` 需要补上。
2. `GET /api/v1/user/menus` 返回空数组：`sys_permission` 的 16 条记录全是 API 类型（`perm_type=2`），没有 MENU 类型（`perm_type=1`），前端动态菜单无从生成。需要按 api-auth.md 第四节补菜单数据并写 `perm_path`。

## 六、当前 git 状态

- 分支 `feature/redis`，工作区改动：`application.yaml`（本地环境配置，见第二节）
- 新增未跟踪：`docs/deploy/`、`docs/postman/`、`docs/答辩物料/`（从旧副本搬入正确仓库位置）
- `main` 上的旧改动已 stash：`stash@{0}`（旧副本的 pom.xml、application.yaml），确认不再需要后可丢弃
