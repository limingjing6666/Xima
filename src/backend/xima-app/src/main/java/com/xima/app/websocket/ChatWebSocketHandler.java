package com.xima.app.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.message.ChatMessageDTO;
import com.xima.app.entity.*;
import com.xima.app.mapper.*;
import com.xima.app.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;

/**
 * 聊天WebSocket处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final FriendshipMapper friendshipMapper;
    private final GroupService groupService;
    private final ObjectMapper objectMapper;
    
    // 消息撤回时间限制（2分钟）
    private static final long RECALL_TIME_LIMIT = 2 * 60 * 1000;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            // 添加新会话，获取旧会话
            WebSocketSession oldSession = sessionManager.addSession(username, session);
            
            // 如果存在旧会话，踢出旧设备
            if (oldSession != null && oldSession.isOpen()) {
                try {
                    // 发送踢出通知给旧设备
                    ChatMessageDTO kickMsg = ChatMessageDTO.builder()
                            .type(ChatMessageDTO.MessageType.KICK)
                            .content("您的账号在其他设备登录，您已被迫下线")
                            .timestamp(LocalDateTime.now())
                            .build();
                    oldSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(kickMsg)));
                    // 关闭旧会话
                    oldSession.close();
                    log.info("用户 {} 在其他设备登录，旧会话已被踢出", username);
                } catch (Exception e) {
                    log.error("踢出旧会话失败: {}", e.getMessage());
                }
            }
            
            // 更新用户状态为在线
            User user = userMapper.findByUsername(username);
            if (user != null) {
                userMapper.updateStatus(user.getId(), UserStatus.ONLINE.name());
                sessionManager.bindUserId(user.getId(), username);
                
                // 通知好友该用户上线
                notifyFriendsStatusChange(user.getId(), UserStatus.ONLINE);
            }

            // 发送欢迎消息
            ChatMessageDTO welcomeMsg = ChatMessageDTO.builder()
                    .type(ChatMessageDTO.MessageType.SYSTEM)
                    .content("欢迎 " + username + " 加入聊天")
                    .timestamp(LocalDateTime.now())
                    .build();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMsg)));
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String username = (String) session.getAttributes().get("username");
        String payload = message.getPayload();

        try {
            ChatMessageDTO chatMessage = objectMapper.readValue(payload, ChatMessageDTO.class);
            
            // 获取发送者信息
            User sender = userMapper.findByUsername(username);
            if (sender == null) {
                return;
            }

            chatMessage.setSenderId(sender.getId());
            chatMessage.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            chatMessage.setSenderAvatar(sender.getAvatar());
            chatMessage.setTimestamp(LocalDateTime.now());

            switch (chatMessage.getType()) {
                case CHAT:
                    handleChatMessage(chatMessage, sender);
                    break;
                case GROUP_CHAT:
                    handleGroupChatMessage(chatMessage, sender);
                    break;
                case TYPING:
                    handleTypingMessage(chatMessage);
                    break;
                case READ:
                    handleReadMessage(chatMessage);
                    break;
                case RECALL:
                    handleRecallMessage(chatMessage, sender);
                    break;
                default:
                    log.warn("未知消息类型: {}", chatMessage.getType());
            }
        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChatMessage(ChatMessageDTO chatMessage, User sender) throws Exception {
        // 保存消息到数据库
        Message message = new Message();
        message.setSenderId(sender.getId());
        message.setReceiverId(chatMessage.getReceiverId());
        message.setContent(chatMessage.getContent());
        message.setContentType(chatMessage.getContentType() != null ? 
                chatMessage.getContentType() : MessageContentType.TEXT);
        messageMapper.insert(message);

        chatMessage.setId(message.getId());

        // 发送给接收者
        String receiverUsername = sessionManager.getUsernameByUserId(chatMessage.getReceiverId());
        if (receiverUsername != null) {
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            boolean sent = sessionManager.sendMessage(receiverUsername, messageJson);
            if (!sent) {
                log.info("用户 {} 不在线，消息已保存为离线消息", receiverUsername);
            }
        }

        // 也发送给发送者（确认消息已发送）
        String senderUsername = sessionManager.getUsernameByUserId(sender.getId());
        if (senderUsername != null) {
            sessionManager.sendMessage(senderUsername, objectMapper.writeValueAsString(chatMessage));
        }
    }

    /**
     * 处理群聊消息
     */
    private void handleGroupChatMessage(ChatMessageDTO chatMessage, User sender) throws Exception {
        Long groupId = chatMessage.getGroupId();
        if (groupId == null) {
            log.warn("群消息缺少groupId");
            return;
        }

        // 检查是否是群成员
        if (!groupService.isMember(groupId, sender.getId())) {
            log.warn("用户 {} 不是群 {} 的成员", sender.getId(), groupId);
            return;
        }

        // 保存群消息
        com.xima.app.dto.group.GroupMessageDTO savedMsg;
        try {
            savedMsg = groupService.sendMessage(
                    groupId, 
                    sender.getId(), 
                    chatMessage.getContent(),
                    chatMessage.getContentType() != null ? chatMessage.getContentType() : MessageContentType.TEXT
            );
        } catch (RuntimeException e) {
            // 发送错误消息给发送者
            String senderUsername = sessionManager.getUsernameByUserId(sender.getId());
            if (senderUsername != null) {
                ChatMessageDTO errorMsg = ChatMessageDTO.builder()
                        .type(ChatMessageDTO.MessageType.ERROR)
                        .content(e.getMessage())
                        .groupId(groupId)
                        .timestamp(LocalDateTime.now())
                        .build();
                sessionManager.sendMessage(senderUsername, objectMapper.writeValueAsString(errorMsg));
            }
            return;
        }

        if (savedMsg != null) {
            chatMessage.setId(savedMsg.getId());
            
            // 获取群所有成员ID
            java.util.List<Long> memberIds = groupService.getMemberIds(groupId);
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            
            // 发送给所有在线的群成员
            for (Long memberId : memberIds) {
                String memberUsername = sessionManager.getUsernameByUserId(memberId);
                if (memberUsername != null) {
                    sessionManager.sendMessage(memberUsername, messageJson);
                }
            }
        }
    }

    /**
     * 处理正在输入消息
     */
    private void handleTypingMessage(ChatMessageDTO chatMessage) throws Exception {
        String receiverUsername = sessionManager.getUsernameByUserId(chatMessage.getReceiverId());
        if (receiverUsername != null) {
            sessionManager.sendMessage(receiverUsername, objectMapper.writeValueAsString(chatMessage));
        }
    }

    /**
     * 处理已读回执
     */
    private void handleReadMessage(ChatMessageDTO chatMessage) throws Exception {
        // 更新消息状态为已读
        if (chatMessage.getId() != null) {
            messageMapper.updateStatus(chatMessage.getId(), "READ");
        }
        
        // 通知发送者
        String senderUsername = sessionManager.getUsernameByUserId(chatMessage.getSenderId());
        if (senderUsername != null) {
            sessionManager.sendMessage(senderUsername, objectMapper.writeValueAsString(chatMessage));
        }
    }

    /**
     * 处理消息撤回
     */
    private void handleRecallMessage(ChatMessageDTO chatMessage, User sender) throws Exception {
        Long messageId = chatMessage.getId();
        log.info("收到撤回请求: messageId={}, groupId={}, senderId={}", 
                messageId, chatMessage.getGroupId(), sender.getId());
        
        if (messageId == null) {
            log.warn("撤回消息缺少消息ID");
            sendErrorMessage(sender.getId(), "撤回失败：消息ID为空");
            return;
        }

        Long groupId = chatMessage.getGroupId();
        
        if (groupId != null) {
            // 群消息撤回
            GroupMessage groupMessage = groupMessageMapper.findById(messageId);
            if (groupMessage == null) {
                sendErrorMessage(sender.getId(), "消息不存在");
                return;
            }
            
            // 检查是否是消息发送者
            if (!groupMessage.getSenderId().equals(sender.getId())) {
                sendErrorMessage(sender.getId(), "只能撤回自己发送的消息");
                return;
            }
            
            // 检查撤回时间限制
            if (groupMessage.getCreateTime() != null) {
                long timeDiff = java.time.Duration.between(groupMessage.getCreateTime(), LocalDateTime.now()).toMillis();
                if (timeDiff > RECALL_TIME_LIMIT) {
                    sendErrorMessage(sender.getId(), "消息发送超过2分钟，无法撤回");
                    return;
                }
            }
            
            // 执行撤回
            groupMessageMapper.recallMessage(messageId);
            
            // 通知群内所有成员
            chatMessage.setRecalled(true);
            chatMessage.setContent(sender.getNickname() + " 撤回了一条消息");
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            
            java.util.List<Long> memberIds = groupService.getMemberIds(groupId);
            for (Long memberId : memberIds) {
                String memberUsername = sessionManager.getUsernameByUserId(memberId);
                if (memberUsername != null) {
                    sessionManager.sendMessage(memberUsername, messageJson);
                }
            }
            
            log.info("用户 {} 撤回了群 {} 的消息 {}", sender.getId(), groupId, messageId);
        } else {
            // 私聊消息撤回
            Message message = messageMapper.findById(messageId);
            if (message == null) {
                sendErrorMessage(sender.getId(), "消息不存在");
                return;
            }
            
            // 检查是否是消息发送者
            if (!message.getSenderId().equals(sender.getId())) {
                sendErrorMessage(sender.getId(), "只能撤回自己发送的消息");
                return;
            }
            
            // 检查撤回时间限制
            if (message.getCreateTime() != null) {
                long timeDiff = java.time.Duration.between(message.getCreateTime(), LocalDateTime.now()).toMillis();
                if (timeDiff > RECALL_TIME_LIMIT) {
                    sendErrorMessage(sender.getId(), "消息发送超过2分钟，无法撤回");
                    return;
                }
            }
            
            // 执行撤回
            messageMapper.recallMessage(messageId);
            
            // 通知双方
            chatMessage.setRecalled(true);
            chatMessage.setContent(sender.getNickname() + " 撤回了一条消息");
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            
            // 通知接收者
            String receiverUsername = sessionManager.getUsernameByUserId(message.getReceiverId());
            if (receiverUsername != null) {
                sessionManager.sendMessage(receiverUsername, messageJson);
            }
            
            // 通知发送者
            String senderUsername = sessionManager.getUsernameByUserId(sender.getId());
            if (senderUsername != null) {
                sessionManager.sendMessage(senderUsername, messageJson);
            }
            
            log.info("用户 {} 撤回了私聊消息 {}", sender.getId(), messageId);
        }
    }

    /**
     * 发送错误消息给用户
     */
    private void sendErrorMessage(Long userId, String errorContent) throws Exception {
        String username = sessionManager.getUsernameByUserId(userId);
        if (username != null) {
            ChatMessageDTO errorMsg = ChatMessageDTO.builder()
                    .type(ChatMessageDTO.MessageType.ERROR)
                    .content(errorContent)
                    .timestamp(LocalDateTime.now())
                    .build();
            sessionManager.sendMessage(username, objectMapper.writeValueAsString(errorMsg));
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessionManager.removeSession(username);
            
            // 更新用户状态为离线
            User user = userMapper.findByUsername(username);
            if (user != null) {
                userMapper.updateStatus(user.getId(), UserStatus.OFFLINE.name());
                
                // 通知好友该用户下线
                notifyFriendsStatusChange(user.getId(), UserStatus.OFFLINE);
                
                sessionManager.unbindUserId(user.getId());
            }
        }
    }

    /**
     * 通知好友用户状态变化
     */
    private void notifyFriendsStatusChange(Long userId, UserStatus status) {
        try {
            // 获取用户所有好友的ID
            java.util.List<Long> friendIds = friendshipMapper.findFriendIdsByUserId(userId);
            
            // 构建状态变化消息
            ChatMessageDTO statusMsg = ChatMessageDTO.builder()
                    .type(ChatMessageDTO.MessageType.STATUS)
                    .senderId(userId)
                    .content(status.name())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            String messageJson = objectMapper.writeValueAsString(statusMsg);
            
            // 通知每个在线的好友
            for (Long friendId : friendIds) {
                String friendUsername = sessionManager.getUsernameByUserId(friendId);
                if (friendUsername != null) {
                    sessionManager.sendMessage(friendUsername, messageJson);
                }
            }
            
            log.info("用户 {} 状态变更为 {}，已通知 {} 个好友", userId, status, friendIds.size());
        } catch (Exception e) {
            log.error("通知好友状态变化失败: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }
}
