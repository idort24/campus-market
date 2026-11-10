# campus-market 本地部署说明（Nginx + 后端 + Redis + MySQL）

> 责任人：周泓颖（Redis 与工程化，第 12 项）
> 目标：一台机器上用 Nginx 把前端静态页和后端接口统一到一个端口，浏览器直接访问 `http://localhost/campus-market/`

## 一、组件与端口一览

| 组件 | 端口 | 说明 |
|---|---|---|
| Nginx | 80 | 对外唯一入口 |
| 前端（Vite 产物 dist） | — | 由 Nginx 托管在 `/campus-market/` |
| 后端 Spring Boot | 8090 | Nginx 把 `/api/` 反代到这里 |
| Redis（Docker `campus-redis`） | 6380 | 验证码、黑名单、限流、缓存 |
| MySQL | 3307 | 库 `campus_market`，账号 root |

## 二、启动顺序（缺一步页面就白屏或 502）

### 1. Redis（已就绪）

```powershell
docker start campus-redis
docker exec campus-redis redis-cli ping      # 期望 PONG
```

> 容器创建命令（换机器时重跑一次即可）：
> `docker run -d --name campus-redis -p 6380:6379 -v campus-redis-data:/data redis:7 --maxmemory 512mb --maxmemory-policy allkeys-lru --appendonly yes`

### 2. MySQL

后端 `application.yaml` 里数据源是 `localhost:3307`，确认这个端口上的 MySQL 容器已经起来：

```powershell
docker ps --filter "name=mysql"
```

没起来就先启动对应容器，并确认库 `campus_market` 存在（张爱阳的 `schema.sql` / `data.sql` 已导入）。

### 3. 后端

```powershell
cd D:\campus-market\campus-market\campus-market
.\mvnw.cmd -DskipTests package
java -jar target\campus-market-0.0.1-SNAPSHOT.jar
```

看到 `Tomcat started on port 8090` 即成功。浏览器访问 `http://localhost:8090/api/v1/goods/list` 应返回 JSON（不是 404 白页）。

### 4. 前端打包

```powershell
cd D:\campus-market\campus-market
npm run build
```

产物在 `dist\`。注意 `vite.config.js` 里 `base: '/campus-market/'`，打包后 `dist\index.html` 里的资源路径都带这个前缀 —— 所以 Nginx 必须把站点挂在 `/campus-market/` 下，挂到根路径会 404。

### 5. Nginx

1. 下载 Windows 版 nginx（`nginx-1.24.x.zip`），解压到 `D:\nginx`
2. 用本目录的 `nginx.conf` 覆盖 `D:\nginx\conf\nginx.conf`
3. 检查配置语法：

```powershell
cd D:\nginx
.\nginx.exe -t
```

期望输出 `syntax is ok` / `test is successful`。

4. 启动：

```powershell
cd D:\nginx
.\nginx.exe
```

> Windows 上 nginx 是后台进程，改完配置要 `.\nginx.exe -s reload`，彻底停止用 `.\nginx.exe -s stop`（或在任务管理器结束 nginx.exe）。别直接双击 `nginx.exe`，会一闪而过。

5. 验证：

```powershell
# 前端页面（应返回 200 且是 HTML）
curl.exe -I http://localhost/campus-market/
# 接口反代（应返回 JSON，不是 502）
curl.exe http://localhost/api/v1/goods/list
```

## 三、访问地址

| 内容 | 地址 |
|---|---|
| 前端 | `http://localhost/campus-market/` |
| 接口 | `http://localhost/api/v1/...` |

## 四、常见问题对照

| 现象 | 原因 | 处理 |
|---|---|---|
| 页面 404，访问 `/` 空白 | `base` 是 `/campus-market/`，站点却挂在根路径 | 必须用 `location /campus-market/` |
| 页面白屏、JS/CSS 404 | 用了源码目录而不是 `dist` | 先 `npm run build`，alias 指向 `dist` |
| 接口 502 | 后端没起，或不是 8090 | `netstat -ano | findstr 8090` 确认 |
| 接口 404（页面正常） | `proxy_pass` 写了 `/api/` 结尾，把前缀吃掉了 | `proxy_pass` 只写 `http://127.0.0.1:8090`，不要带路径 |
| 上传报 413 | nginx 默认 1MB 限制 | `client_max_body_size 200m` |
| 刷新子路由 404 | 缺 history 回落 | `try_files $uri $uri/ /campus-market/index.html` |
