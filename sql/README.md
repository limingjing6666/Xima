# Xima 数据库脚本

## 脚本说明

| 文件 | 用途 | 使用场景 |
|------|------|----------|
| `local-init.sql` | 本地开发完整初始化 | 首次搭建开发环境 |

## 本地开发使用

### 方式一：命令行执行
```bash
# Windows
mysql -u root -p < sql/local-init.sql

# 或指定数据库
mysql -u root -p123456 < sql/local-init.sql
```

### 方式二：MySQL Workbench / Navicat
1. 打开 `local-init.sql`
2. 执行全部脚本

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 管理员 | 超级管理员 |
| test | 123456 | 用户 | 测试用户 |
| user1 | 123456 | 用户 | 普通用户 |
| user2 | 123456 | 用户 | 普通用户 |

## 初始数据

- **好友关系**: test、user1、user2 互为好友
- **测试群组**: "Xima开发群"，包含所有用户

## Docker 部署

Docker 部署使用 `docker/mysql/init/` 目录下的脚本，会自动执行：
- `01-schema.sql` - 建表
- `02-data.sql` - 初始数据

## 数据库表结构

```
xima
├── user              # 用户表
├── role              # 角色表
├── user_role         # 用户角色关联
├── friendship        # 好友关系
├── message           # 私聊消息
├── chat_group        # 群组
├── group_member      # 群成员
├── group_message     # 群消息
└── group_message_read # 群消息已读记录
```
