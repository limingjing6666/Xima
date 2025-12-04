# Xima Docker 部署指南

## 1. 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 4GB 内存
- 至少 10GB 磁盘空间

## 2. 快速启动

### 2.1 开发环境（推荐）

```bash
# 进入项目根目录
cd Xima

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 2.2 仅启动基础设施（MySQL + Redis）

```bash
# 只启动MySQL和Redis
docker-compose up -d mysql redis

# 本地开发时连接配置
# MySQL: localhost:3306, 用户: xima, 密码: xima123456
# Redis: localhost:6379
```

## 3. 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| mysql | 3306 | MySQL 8.0 数据库 |
| redis | 6379 | Redis 7 缓存 |
| backend | 8080 | Spring Boot 后端 |
| frontend | 80 | Vue.js 前端 (Nginx) |
| nginx | 8000/443 | 反向代理（生产环境） |

## 4. 访问地址

- **前端**: http://localhost
- **后端API**: http://localhost/api
- **Swagger文档**: http://localhost/api/swagger-ui.html
- **健康检查**: http://localhost/api/v1/test/health

## 5. 常用命令

### 5.1 服务管理

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启某个服务
docker-compose restart backend

# 重新构建并启动
docker-compose up -d --build

# 查看服务日志
docker-compose logs -f backend
docker-compose logs -f mysql
```

### 5.2 数据库操作

```bash
# 进入MySQL容器
docker exec -it xima-mysql mysql -u xima -pxima123456 xima

# 备份数据库
docker exec xima-mysql mysqldump -u xima -pxima123456 xima > backup.sql

# 恢复数据库
docker exec -i xima-mysql mysql -u xima -pxima123456 xima < backup.sql
```

### 5.3 Redis操作

```bash
# 进入Redis容器
docker exec -it xima-redis redis-cli

# 查看所有key
docker exec xima-redis redis-cli keys '*'

# 清空缓存
docker exec xima-redis redis-cli flushall
```

## 6. 配置说明

### 6.1 环境变量

可以通过 `.env` 文件或 docker-compose.yml 配置：

```env
# MySQL
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=xima
MYSQL_USER=xima
MYSQL_PASSWORD=xima123456

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# 邮件（可选）
MAIL_USERNAME=your_qq_email
MAIL_PASSWORD=your_email_password
```

### 6.2 数据持久化

数据存储在 Docker volumes 中：
- `mysql_data`: MySQL 数据
- `redis_data`: Redis 数据
- `uploads_data`: 上传文件

```bash
# 查看volumes
docker volume ls

# 清理未使用的volumes（谨慎操作）
docker volume prune
```

## 7. 生产环境部署

### 7.1 启用Nginx反向代理

```bash
docker-compose --profile production up -d
```

### 7.2 配置SSL证书

1. 将证书文件放入 `docker/nginx/ssl/` 目录
2. 修改 `docker/nginx/conf.d/default.conf` 启用HTTPS配置
3. 重启nginx服务

### 7.3 安全建议

1. 修改默认密码
2. 配置防火墙规则
3. 启用HTTPS
4. 定期备份数据
5. 监控服务状态

## 8. 故障排查

### 8.1 服务无法启动

```bash
# 查看详细日志
docker-compose logs backend

# 检查容器状态
docker ps -a

# 检查网络
docker network ls
```

### 8.2 数据库连接失败

```bash
# 检查MySQL是否就绪
docker exec xima-mysql mysqladmin ping -h localhost -u root -proot123456

# 检查网络连通性
docker exec xima-backend ping mysql
```

### 8.3 清理重建

```bash
# 停止并删除所有容器、网络
docker-compose down

# 删除volumes（会丢失数据！）
docker-compose down -v

# 重新构建
docker-compose build --no-cache

# 启动
docker-compose up -d
```

## 9. 架构图

```
                    ┌─────────────┐
                    │   Client    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │   Nginx     │ :80/:443
                    │  (可选)     │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐     │     ┌──────▼──────┐
       │  Frontend   │     │     │   Backend   │
       │  (Nginx)    │     │     │ (Spring)    │
       │    :80      │     │     │   :8080     │
       └─────────────┘     │     └──────┬──────┘
                           │            │
                    ┌──────┴──────┬─────┴─────┐
                    │             │           │
             ┌──────▼──────┐ ┌────▼────┐ ┌────▼────┐
             │   MySQL     │ │  Redis  │ │ Uploads │
             │   :3306     │ │  :6379  │ │ Volume  │
             └─────────────┘ └─────────┘ └─────────┘
```
