-- =============================================
-- Xima 本地开发环境 - 完整数据库初始化脚本
-- 使用方法: mysql -u root -p < local-init.sql
-- =============================================

-- 创建数据库
DROP DATABASE IF EXISTS xima;
CREATE DATABASE xima DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xima;

-- =============================================
-- 1. 用户相关表
-- =============================================

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `chat_background` VARCHAR(255) COMMENT '聊天背景',
    `status` ENUM('ONLINE', 'OFFLINE', 'AWAY', 'BUSY', 'INVISIBLE') DEFAULT 'OFFLINE' COMMENT '在线状态',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    `description` VARCHAR(255) COMMENT '角色描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE `user_role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 2. 好友关系表
-- =============================================

CREATE TABLE `friendship` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT NOT NULL COMMENT '好友ID',
    `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED') DEFAULT 'PENDING' COMMENT '状态',
    `remark` VARCHAR(50) COMMENT '好友备注名',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_friend_id` (`friend_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`friend_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- =============================================
-- 3. 私聊消息表
-- =============================================

CREATE TABLE `message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `content_type` ENUM('TEXT', 'IMAGE', 'FILE', 'VOICE', 'VIDEO', 'SYSTEM') DEFAULT 'TEXT' COMMENT '内容类型',
    `file_url` VARCHAR(500) COMMENT '文件URL',
    `file_name` VARCHAR(255) COMMENT '文件名',
    `file_size` BIGINT COMMENT '文件大小',
    `status` ENUM('SENT', 'DELIVERED', 'READ', 'RECALLED') DEFAULT 'SENT' COMMENT '消息状态',
    `recalled` TINYINT(1) DEFAULT 0 COMMENT '是否撤回',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX `idx_sender_id` (`sender_id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_conversation` (`sender_id`, `receiver_id`, `create_time`),
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';

-- =============================================
-- 4. 群组相关表
-- =============================================

-- 群组表
CREATE TABLE `chat_group` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '群组ID',
    `name` VARCHAR(100) NOT NULL COMMENT '群名称',
    `avatar` VARCHAR(255) COMMENT '群头像URL',
    `description` VARCHAR(500) COMMENT '群描述',
    `owner_id` BIGINT NOT NULL COMMENT '群主ID',
    `max_members` INT DEFAULT 200 COMMENT '最大成员数',
    `member_count` INT DEFAULT 1 COMMENT '当前成员数',
    `invite_mode` ENUM('ANYONE', 'ADMIN_ONLY', 'OWNER_ONLY') DEFAULT 'ANYONE' COMMENT '邀请模式',
    `join_mode` ENUM('FREE', 'APPROVAL', 'INVITE_ONLY') DEFAULT 'FREE' COMMENT '加入模式',
    `mute_all` TINYINT(1) DEFAULT 0 COMMENT '是否全员禁言',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_name` (`name`),
    FOREIGN KEY (`owner_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- 群成员表
CREATE TABLE `group_member` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` ENUM('OWNER', 'ADMIN', 'MEMBER') DEFAULT 'MEMBER' COMMENT '角色',
    `nickname` VARCHAR(50) COMMENT '群内昵称',
    `muted` TINYINT(1) DEFAULT 0 COMMENT '是否被禁言',
    `mute_end_time` DATETIME COMMENT '禁言结束时间',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `last_read_time` DATETIME COMMENT '最后阅读时间',
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role` (`role`),
    FOREIGN KEY (`group_id`) REFERENCES `chat_group`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群成员表';

-- 群消息表
CREATE TABLE `group_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `content_type` ENUM('TEXT', 'IMAGE', 'FILE', 'VOICE', 'VIDEO', 'SYSTEM') DEFAULT 'TEXT' COMMENT '消息类型',
    `file_url` VARCHAR(500) COMMENT '文件URL',
    `file_name` VARCHAR(255) COMMENT '文件名',
    `file_size` BIGINT COMMENT '文件大小',
    `at_users` VARCHAR(500) COMMENT '@的用户ID列表',
    `at_all` TINYINT(1) DEFAULT 0 COMMENT '是否@全体',
    `status` ENUM('NORMAL', 'RECALLED') DEFAULT 'NORMAL' COMMENT '消息状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_sender_id` (`sender_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_group_time` (`group_id`, `create_time`),
    FOREIGN KEY (`group_id`) REFERENCES `chat_group`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群消息表';

-- 群消息已读记录表
CREATE TABLE `group_message_read` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `group_id` BIGINT NOT NULL COMMENT '群组ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `last_read_message_id` BIGINT DEFAULT 0 COMMENT '最后已读消息ID',
    `last_read_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    FOREIGN KEY (`group_id`) REFERENCES `chat_group`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群消息已读记录表';

-- =============================================
-- 5. 初始化数据
-- =============================================

-- 初始化角色
INSERT INTO `role` (`name`, `description`) VALUES 
('ROLE_USER', '普通用户'),
('ROLE_MODERATOR', '管理员'),
('ROLE_ADMIN', '超级管理员');

-- 创建测试用户 (密码都是: 123456)
-- BCrypt加密: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `status`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@xima.com', 'OFFLINE'),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 'test@xima.com', 'OFFLINE'),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户一', 'user1@xima.com', 'OFFLINE'),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '用户二', 'user2@xima.com', 'OFFLINE');

-- 分配角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES 
(1, 3),  -- admin -> ROLE_ADMIN
(1, 1),  -- admin -> ROLE_USER
(2, 1),  -- test -> ROLE_USER
(3, 1),  -- user1 -> ROLE_USER
(4, 1);  -- user2 -> ROLE_USER

-- 创建好友关系 (test 和 user1, user2 互为好友)
INSERT INTO `friendship` (`user_id`, `friend_id`, `status`) VALUES 
(2, 3, 'ACCEPTED'),
(3, 2, 'ACCEPTED'),
(2, 4, 'ACCEPTED'),
(4, 2, 'ACCEPTED'),
(3, 4, 'ACCEPTED'),
(4, 3, 'ACCEPTED');

-- 创建测试群组
INSERT INTO `chat_group` (`name`, `description`, `owner_id`, `member_count`) VALUES 
('Xima开发群', '项目开发讨论群', 1, 4);

-- 添加群成员
INSERT INTO `group_member` (`group_id`, `user_id`, `role`) VALUES 
(1, 1, 'OWNER'),
(1, 2, 'ADMIN'),
(1, 3, 'MEMBER'),
(1, 4, 'MEMBER');

-- =============================================
-- 完成
-- =============================================
SELECT '本地开发数据库初始化完成！' AS message;
SELECT '测试账号: admin/test/user1/user2, 密码: 123456' AS info;
