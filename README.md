# Xima 即时通讯应用

<p align="center">
  <img src="docs/assets/logo.png" alt="Xima Logo" width="120">
</p>

<p align="center">
  <strong>一个功能完整的即时通讯应用，支持私聊、群聊、文件传输</strong>
</p>

<p align="center">
  <a href="#功能特性">功能特性</a> •
  <a href="#技术栈">技术栈</a> •
  <a href="#快速开始">快速开始</a> •
  <a href="#部署指南">部署指南</a> •
  <a href="#项目文档">项目文档</a>
</p>

---

## 功能特性

### 用户系统
- ✅ 用户注册/登录（JWT认证）
- ✅ 邮箱验证码
- ✅ 个人资料管理
- ✅ 头像/聊天背景设置

### 好友系统
- ✅ 好友搜索/添加
- ✅ 好友请求处理
- ✅ 好友备注
- ✅ 好友列表管理

### 私聊功能
- ✅ 实时消息（WebSocket）
- ✅ 消息已读状态
- ✅ 离线消息
- ✅ 消息撤回
- ✅ 图片/文件发送

### 群聊功能
- ✅ 创建/解散群组
- ✅ 群成员管理
- ✅ 群公告
- ✅ 群消息
- ✅ @成员功能

## 技术栈

### 后端
| 技术 | 说明 |
|------|------|
| Spring Boot 2.7 | 基础框架 |
| Spring Security | 安全认证 |
| JWT | Token认证 |
| MyBatis | ORM框架 |
| MySQL 8.0 | 主数据库 |
| Redis | 缓存/验证码 |
| WebSocket | 实时通讯 |
| Swagger | API文档 |

### 前端
| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| Vite | 构建工具 |
| Element Plus | UI组件库 |
| Pinia | 状态管理 |
| Vue Router | 路由管理 |
| Axios | HTTP客户端 |

### 部署
| 技术 | 说明 |
|------|------|
| Docker | 容器化 |
| Docker Compose | 容器编排 |
| Nginx | 反向代理 |

## 项目结构

```
Xima/
├── src/
│   ├── backend/xima-app/        # Spring Boot 后端
│   │   ├── src/main/java/       # Java源码
│   │   ├── src/main/resources/  # 配置文件
│   │   └── src/test/            # 单元测试
│   └── frontend/xima-web/       # Vue 前端
│       ├── src/views/           # 页面组件
│       ├── src/api/             # API接口
│       └── src/stores/          # 状态管理
├── docker/                      # Docker配置
│   ├── mysql/                   # MySQL配置和初始化
│   ├── redis/                   # Redis配置
│   └── nginx/                   # Nginx配置
├── deploy/                      # 部署脚本
├── sql/                         # SQL脚本
└── docs/                        # 项目文档
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7+
- Maven 3.8+

### 本地开发

**1. 克隆项目**
```bash
git clone https://github.com/your-username/Xima.git
cd Xima
```

**2. 初始化数据库**
```bash
mysql -u root -p < sql/local-init.sql
```

**3. 启动后端**
```bash
cd src/backend/xima-app
mvn spring-boot:run
```

**4. 启动前端**
```bash
cd src/frontend/xima-web
npm install
npm run dev
```

**5. 访问应用**
- 前端: http://localhost:3000
- 后端API: http://localhost:8080/api
- Swagger: http://localhost:8080/api/swagger-ui.html

### 测试账号
| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | 123456 | 管理员 |
| test | 123456 | 测试用户 |
| user1 | 123456 | 普通用户 |
| user2 | 123456 | 普通用户 |

## 部署指南

### Docker 一键部署

```bash
# 启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### CentOS 7 服务器部署

```bash
# 1. 上传项目到服务器 /opt/xima

# 2. 安装 Docker
chmod +x deploy/*.sh
./deploy/install-docker.sh

# 3. 部署应用
./deploy/deploy.sh
```

详细部署文档：[CentOS7部署指南](docs/CentOS7部署指南.md)

## 项目文档

| 文档 | 说明 |
|------|------|
| [Docker部署指南](docs/Docker部署指南.md) | Docker容器化部署 |
| [CentOS7部署指南](docs/CentOS7部署指南.md) | 云服务器部署 |
| [性能测试指南](docs/性能测试指南.md) | 性能测试方法 |
| [技术架构设计](docs/技术架构设计.md) | 系统架构说明 |
| [开发路线图](docs/开发路线图.md) | 项目规划 |

## API 文档

启动后端后访问 Swagger UI：
```
http://localhost:8080/api/swagger-ui.html
```

## 测试

```bash
cd src/backend/xima-app

# 运行所有测试
mvn test

# 测试覆盖率
mvn test jacoco:report
```

当前测试统计：
- 单元测试：119 个
- 集成测试：14 个
- 性能测试：6 个

## 开发计划

- [x] 单体应用开发
- [x] 用户认证系统
- [x] 好友系统
- [x] 私聊功能
- [x] 群聊功能
- [x] 文件上传
- [x] Docker容器化
- [ ] 微服务拆分
- [ ] Kubernetes部署
- [ ] 消息队列集成

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License