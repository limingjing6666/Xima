-- 性能优化：添加数据库索引
-- 注意：如果索引已存在会报错，可以忽略

-- message 表索引
ALTER TABLE message ADD INDEX idx_message_sender_id (sender_id);
ALTER TABLE message ADD INDEX idx_message_receiver_id (receiver_id);
ALTER TABLE message ADD INDEX idx_message_create_time (create_time);
ALTER TABLE message ADD INDEX idx_message_sender_receiver (sender_id, receiver_id);
ALTER TABLE message ADD INDEX idx_message_content_type (content_type);
ALTER TABLE message ADD INDEX idx_message_status (status);

-- group_message 表索引
ALTER TABLE group_message ADD INDEX idx_group_message_group_id (group_id);
ALTER TABLE group_message ADD INDEX idx_group_message_sender_id (sender_id);
ALTER TABLE group_message ADD INDEX idx_group_message_create_time (create_time);
ALTER TABLE group_message ADD INDEX idx_group_message_content_type (content_type);

-- group_member 表索引
ALTER TABLE group_member ADD INDEX idx_group_member_group_id (group_id);
ALTER TABLE group_member ADD INDEX idx_group_member_user_id (user_id);
ALTER TABLE group_member ADD INDEX idx_group_member_group_user (group_id, user_id);

-- friendship 表索引
ALTER TABLE friendship ADD INDEX idx_friendship_user_id (user_id);
ALTER TABLE friendship ADD INDEX idx_friendship_friend_id (friend_id);
ALTER TABLE friendship ADD INDEX idx_friendship_status (status);

-- user 表索引（username和email可能已有唯一索引）
ALTER TABLE user ADD INDEX idx_user_status (status);
