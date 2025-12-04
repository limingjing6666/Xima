# Xima 文档中心

## 快速导航

### 🚀 快速开始
- [项目介绍](../README.md) - 项目概述、功能特性、技术栈
- [项目启动指南](项目启动指南.md) - 本地开发环境搭建

### 📦 部署文档
- [Docker部署指南](Docker部署指南.md) - Docker Compose 一键部署
- [CentOS7部署指南](CentOS7部署指南.md) - 云服务器部署完整流程

### 🔧 开发文档
- [技术架构设计](技术架构设计.md) - 系统架构和技术选型
- [开发进度跟踪](开发进度跟踪.md) - 当前开发状态和API列表
- [开发任务清单](开发任务清单.md) - 详细任务分解
- [开发路线图](开发路线图.md) - 项目规划

### 📊 测试文档
- [性能测试指南](性能测试指南.md) - 压力测试、负载测试

### 📚 学习资源
- [学习计划](学习计划.md) - 学习路径规划
- [学习资源指南](学习资源指南.md) - 推荐学习资料

## 项目状态

| 模块 | 状态 |
|------|------|
| 用户系统 | ✅ 完成 |
| 好友系统 | ✅ 完成 |
| 私聊功能 | ✅ 完成 |
| 群聊功能 | ✅ 完成 |
| 文件上传 | ✅ 完成 |
| 单元测试 | ✅ 119个 |
| Docker部署 | ✅ 完成 |
| 微服务拆分 | 🔲 待开发 |

## 快速命令

```bash
# 本地开发
mysql -u root -p < sql/local-init.sql
cd src/backend/xima-app && mvn spring-boot:run
cd src/frontend/xima-web && npm run dev

# Docker部署
docker compose up -d

# 运行测试
cd src/backend/xima-app && mvn test