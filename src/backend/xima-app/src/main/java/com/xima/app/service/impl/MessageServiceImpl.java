package com.xima.app.service.impl;

import com.xima.app.dto.group.GroupMessageDTO;
import com.xima.app.dto.message.ChatMessageDTO;
import com.xima.app.dto.message.SearchMessageDTO;
import com.xima.app.entity.ChatGroup;
import com.xima.app.entity.Message;
import com.xima.app.entity.MessageStatus;
import com.xima.app.entity.User;
import com.xima.app.exception.BusinessException;
import com.xima.app.exception.ErrorCode;
import com.xima.app.mapper.GroupMapper;
import com.xima.app.mapper.GroupMemberMapper;
import com.xima.app.mapper.GroupMessageMapper;
import com.xima.app.mapper.MessageMapper;
import com.xima.app.mapper.UserMapper;
import com.xima.app.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMapper groupMapper;

    @Override
    public List<ChatMessageDTO> getChatHistory(Long userId1, Long userId2, int page, int size) {
        int offset = page * size;
        List<Message> messages = messageMapper.findChatHistory(userId1, userId2, offset, size);
        return convertToDTO(messages);
    }

    @Override
    public List<ChatMessageDTO> getOfflineMessages(Long userId) {
        List<Message> messages = messageMapper.findOfflineMessages(userId);
        // 更新状态为已送达
        messageMapper.updateStatusToDelivered(userId);
        return convertToDTO(messages);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageMapper.findById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        messageMapper.updateStatus(messageId, MessageStatus.READ.name());
    }

    @Override
    public long getUnreadCount(Long receiverId, Long senderId) {
        return messageMapper.countUnreadMessages(receiverId, senderId);
    }

    @Override
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageMapper.findById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        // 只能删除自己发送的消息
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        messageMapper.deleteById(messageId);
    }

    @Override
    public List<ChatMessageDTO> searchMessages(Long userId, String keyword, int page, int size) {
        int offset = page * size;
        List<Message> messages = messageMapper.searchMessages(userId, keyword, offset, size);
        return convertToDTO(messages);
    }
    
    @Override
    public List<SearchMessageDTO> searchAllMessages(Long userId, String keyword, int page, int size) {
        List<SearchMessageDTO> results = new ArrayList<>();
        int offset = page * size;
        
        // 1. 搜索私聊消息
        List<Message> privateMessages = messageMapper.searchMessages(userId, keyword, offset, size);
        for (Message msg : privateMessages) {
            User sender = userMapper.findById(msg.getSenderId());
            User receiver = userMapper.findById(msg.getReceiverId());
            
            results.add(SearchMessageDTO.builder()
                    .id(msg.getId())
                    .chatType("PRIVATE")
                    .senderId(msg.getSenderId())
                    .senderName(sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : null)
                    .senderAvatar(sender != null ? sender.getAvatar() : null)
                    .receiverId(msg.getReceiverId())
                    .receiverName(receiver != null ? (receiver.getNickname() != null ? receiver.getNickname() : receiver.getUsername()) : null)
                    .content(msg.getContent())
                    .timestamp(msg.getCreateTime())
                    .build());
        }
        
        // 2. 搜索用户所在群的消息
        List<Long> groupIds = groupMemberMapper.findGroupIdsByUserId(userId);
        for (Long groupId : groupIds) {
            List<GroupMessageDTO> groupMessages = groupMessageMapper.searchMessages(groupId, keyword, 0, size);
            ChatGroup group = groupMapper.findById(groupId);
            
            for (GroupMessageDTO msg : groupMessages) {
                results.add(SearchMessageDTO.builder()
                        .id(msg.getId())
                        .chatType("GROUP")
                        .senderId(msg.getSenderId())
                        .senderName(msg.getSenderName())
                        .senderAvatar(msg.getSenderAvatar())
                        .groupId(groupId)
                        .groupName(group != null ? group.getName() : null)
                        .content(msg.getContent())
                        .timestamp(msg.getCreateTime())
                        .build());
            }
        }
        
        // 3. 按时间倒序排序，取前size条
        results.sort(Comparator.comparing(SearchMessageDTO::getTimestamp).reversed());
        if (results.size() > size) {
            results = results.subList(0, size);
        }
        
        return results;
    }

    /**
     * 将消息实体列表转换为DTO列表
     */
    private List<ChatMessageDTO> convertToDTO(List<Message> messages) {
        List<ChatMessageDTO> result = new ArrayList<>();
        for (Message message : messages) {
            User sender = userMapper.findById(message.getSenderId());
            User receiver = userMapper.findById(message.getReceiverId());

            result.add(ChatMessageDTO.builder()
                    .id(message.getId())
                    .type(ChatMessageDTO.MessageType.CHAT)
                    .senderId(message.getSenderId())
                    .senderName(sender != null ? 
                            (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : null)
                    .senderAvatar(sender != null ? sender.getAvatar() : null)
                    .receiverId(message.getReceiverId())
                    .receiverName(receiver != null ? 
                            (receiver.getNickname() != null ? receiver.getNickname() : receiver.getUsername()) : null)
                    .content(message.getContent())
                    .contentType(message.getContentType())
                    .timestamp(message.getCreateTime())
                    .recalled(message.getRecalled())
                    .build());
        }
        return result;
    }
}
