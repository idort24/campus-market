# campus-market 集群部署说明（周泓颖 / 中间件与联调）

## 一、拓扑与端口

```
浏览器
  │ HTTP 8080 / HTTPS 8443
  ▼
Nginx（SSL 卸载、动静分离、四种负载策略）
  ├─ pool_rr      轮询          ┐
  ├─ pool_weight  加权 3:1:1     ├─→ 127.0.0.1:8082  campus-market 实例 A
  ├─ pool_iphash  IP 哈希        ├─→ 127.0.0.1:8083  campus-market 实例 B
  └─ pool_least   最少连接      ┘   127.0.0.1:8084  campus-market 实例 C
                                        │
                                   MySQL 3307 / Redis 6380（Docker）
```

| 组件 | 端口 | 说明 |
| --- | --- | --- |
| Nginx HTTP | 8080 | 静态托管 + 反代 + 四策略演示入口 |
| Nginx HTTPS | 8443 | SSL 卸载，自签证书 |
| 后端实例 | 8082 / 8083 / 8084 | 同一 jar，不同 `--server.port` |
| MySQL | 3307 | `huobajie-mysql` 容器 |
| Redis | 6380 | `campus-redis` 容器，`database 0` |

> 8081 在本机被其他课程实验占用，故实例端口顺延为 8082-8084。
> 若 8081 空闲，三个 upstream 里的端口统一改回 8081/8082/8083 即可。

## 二、为什么不是 war 部署到 Tomcat

后端是 **Spring Boot 3.3.5**，使用 `jakarta.servlet` 命名空间，内嵌 Tomcat。
传统 Tomcat 9 是 `javax.servlet`，war 部署会直接报类加载失败；要war部署必须换 Tomcat 10.1+。

因此集群由**同一份 jar 起三个实例**构成，Nginx 在HTTP 层做负载均衡。
这与"多节点集群"的演示目标一致，且更贴近生产中的无状态服务多副本部署。

## 三、会话与缓存

- 登录用 JWT，**无传统 HttpSession**，请求自带身份，不依赖容器会话复制。
- 跨实例共享的是**缓存**：`cache.store=redis`，验证码、登录失败计数、字典缓存落在 Redis 6380。
- 因此不需要 Tomcat 的 `Cluster` / `distributable` 配置；对应的能力由"无状态 + Redis 集中共享"承担。
  若答辩要求演示容器级会话复制，需要另建 Tomcat 10.1 + war 环境，与当前后端架构不同。

## 四、启动步骤

```bash
# 1. 依赖
docker start campus-redis huobajie-mysql

# 2. 打包（在仓库根目录）
./mvnw -DskipTests package

# 3. 起三实例（连接参数用启动参数覆盖，仓库配置保持默认）
java -jar target/campus-market-0.0.1-SNAPSHOT.jar --server.port=8082 ^
  --spring.datasource.url="jdbc:mysql://localhost:3307/campus_market?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true" ^
  --spring.datasource.password=123456 --cache.store=redis
# 8083 / 8084 同理，只改 --server.port

# 4. Nginx
nginx -p <nginx目录> -c conf/nginx.conf -t
nginx -p <nginx目录> -c conf/nginx.conf -s reload
```

## 五、验证清单

| 验证项 | 命令 | 预期 |
| --- | --- | --- |
| 轮询 | `curl -I http://127.0.0.1:8080/rr/api/goods?pageNum=1` 连发 3 次 | `X-Upstream` 依次 8082→8083→8084 |
| 加权 | 同上，改用 `/weight/`，连发 5 次 | 8082 出现 3 次，8083/8084 各 1 次 |
| IP 哈希 | 同上，改用 `/iphash/`，连发 3 次 | 三次 `X-Upstream` 完全相同 |
| 最少连接 | 同上，改用 `/least/` | 三台轮流 |
| 代理头 | 后端日志或调试接口看 `Host / X-Real-IP / X-Forwarded-For / X-Forwarded-Proto` | 四项均有值 |
| 动静分离 | `curl -I http://127.0.0.1:8080/static/index.html` | 200，且 access.log 里 upstream 为空 |
| SSL 卸载 | `curl -k https://127.0.0.1:8443/api/goods?pageNum=1` | 200，`X-Upstream` 有值，后端侧仍是 http |
| 健康摘除 | 停掉 8084 后连发 12 次 `/rr/` | 全部 200，`X-Upstream` 不再出现 8084；30s 后恢复自动加入 |
| 平滑重载 | 改配置后 `nginx -t` → `-s reload` | 校验通过，reload 期间请求不中断 |

## 六、答辩口径提醒

1. 端口：Nginx 占 8080，后端实例是 8082-8084，**不是** 8080/8081/8082，被问到时按实际答。
2. 集群条件：同版本同应用（同一份 jar）、统一入口（Nginx upstream）、无状态可横向扩，满足"集群成立"；
   但**没有** Tomcat 的 `web.xml` + `distributable` + 组播会话复制，因为后端不是 war + HttpSession 架构。
3. 节点标识：由 Nginx 的 `X-Upstream` 响应头回显，不需要前端写死 1/2/3。