-- Xima 初始化数据脚本

USE xima;

-- 初始化角色数据
INSERT INTO `role` (`name`, `description`) VALUES 
('ROLE_USER', '普通用户'),
('ROLE_MODERATOR', '管理员'),
('ROLE_ADMIN', '超级管理员')
ON DUPLICATE KEY UPDATE `description` = VALUES(`description`);

-- 创建测试用户 (密码: 123456, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `status`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@xima.com', 'OFFLINE'),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 'test@xima.com', 'OFFLINE')
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`);

-- 为admin用户分配管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) 
SELECT u.id, r.id FROM `user` u, `role` r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;

-- 为admin用户分配普通用户角色
INSERT INTO `user_role` (`user_id`, `role_id`) 
SELECT u.id, r.id FROM `user` u, `role` r 
WHERE u.username = 'admin' AND r.name = 'ROLE_USER'
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;

-- 为test用户分配普通用户角色
INSERT INTO `user_role` (`user_id`, `role_id`) 
SELECT u.id, r.id FROM `user` u, `role` r 
WHERE u.username = 'test' AND r.name = 'ROLE_USER'
ON DUPLICATE KEY UPDATE `user_id` = `user_id`;
