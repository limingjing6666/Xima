-- 集成测试初始化数据

-- 创建角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    chat_background VARCHAR(255),
    status VARCHAR(20) DEFAULT 'OFFLINE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 创建好友关系表
CREATE TABLE IF NOT EXISTS friendship (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT,
    content_type VARCHAR(20) DEFAULT 'TEXT',
    status VARCHAR(20) DEFAULT 'SENT',
    recalled BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建群组表
CREATE TABLE IF NOT EXISTS chat_group (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    avatar VARCHAR(255),
    owner_id BIGINT NOT NULL,
    max_members INT DEFAULT 200,
    member_count INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建群成员表
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    muted BOOLEAN DEFAULT FALSE,
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建群消息表
CREATE TABLE IF NOT EXISTS group_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT,
    content_type VARCHAR(20) DEFAULT 'TEXT',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建群消息已读表
CREATE TABLE IF NOT EXISTS group_message_read (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    last_read_message_id BIGINT DEFAULT 0
);

-- 插入角色数据
INSERT INTO role (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO role (id, name) VALUES (2, 'ROLE_ADMIN');

-- 插入测试用户 (密码: password123，使用BCrypt加密)
INSERT INTO user (id, username, password, email, nickname, status) VALUES 
(1, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'testuser@qq.com', 'Test User', 'OFFLINE'),
(2, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'testuser2@qq.com', 'Test User 2', 'OFFLINE'),
(3, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@qq.com', 'Admin', 'OFFLINE');

-- 分配角色
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO user_role (user_id, role_id) VALUES (2, 1);
INSERT INTO user_role (user_id, role_id) VALUES (3, 1);
INSERT INTO user_role (user_id, role_id) VALUES (3, 2);

-- 创建好友关系
INSERT INTO friendship (user_id, friend_id, status) VALUES (1, 2, 'ACCEPTED');
INSERT INTO friendship (user_id, friend_id, status) VALUES (2, 1, 'ACCEPTED');

-- 创建测试消息
INSERT INTO message (sender_id, receiver_id, content, content_type, status) VALUES 
(1, 2, 'Hello!', 'TEXT', 'SENT'),
(2, 1, 'Hi there!', 'TEXT', 'SENT');

-- 创建测试群组
INSERT INTO chat_group (id, name, description, owner_id, member_count) VALUES 
(1, 'Test Group', 'A test group', 1, 2);

-- 添加群成员
INSERT INTO group_member (group_id, user_id, role) VALUES 
(1, 1, 'OWNER'),
(1, 2, 'MEMBER');
