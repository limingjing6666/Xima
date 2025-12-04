package com.xima.app.service;

import com.xima.app.dto.message.ChatMessageDTO;
import com.xima.app.dto.message.SearchMessageDTO;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 获取聊天历史记录
     */
    List<ChatMessageDTO> getChatHistory(Long userId1, Long userId2, int page, int size);

    /**
     * 获取离线消息
     */
    List<ChatMessageDTO> getOfflineMessages(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);

    /**
     * 获取未读消息数
     */
    long getUnreadCount(Long receiverId, Long senderId);

    /**
     * 删除消息
     */
    void deleteMessage(Long userId, Long messageId);

    /**
     * 搜索消息（私聊）
     */
    List<ChatMessageDTO> searchMessages(Long userId, String keyword, int page, int size);
    
    /**
     * 搜索所有消息（私聊+群聊）
     */
    List<SearchMessageDTO> searchAllMessages(Long userId, String keyword, int page, int size);
}
