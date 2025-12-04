package com.xima.app.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    // 用户名 -> WebSocket会话
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // 用户ID -> 用户名
    private final Map<Long, String> userIdToUsername = new ConcurrentHashMap<>();

    /**
     * 添加会话（如果已有旧会话，先踢出）
     */
    public WebSocketSession addSession(String username, WebSocketSession session) {
        WebSocketSession oldSession = sessions.put(username, session);
        log.info("用户 {} 已连接, 当前在线用户数: {}", username, sessions.size());
        return oldSession; // 返回旧会话，由调用者处理踢出逻辑
    }

    /**
     * 移除会话
     */
    public void removeSession(String username) {
        sessions.remove(username);
        log.info("用户 {} 已断开, 当前在线用户数: {}", username, sessions.size());
    }

    /**
     * 获取会话
     */
    public WebSocketSession getSession(String username) {
        return sessions.get(username);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(String username) {
        WebSocketSession session = sessions.get(username);
        return session != null && session.isOpen();
    }

    /**
     * 发送消息给指定用户
     */
    public boolean sendMessage(String username, String message) {
        WebSocketSession session = sessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                return true;
            } catch (IOException e) {
                log.error("发送消息失败: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String message) {
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                } catch (IOException e) {
                    log.error("广播消息失败: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        return sessions.size();
    }

    /**
     * 绑定用户ID和用户名
     */
    public void bindUserId(Long userId, String username) {
        userIdToUsername.put(userId, username);
    }

    /**
     * 根据用户ID获取用户名
     */
    public String getUsernameByUserId(Long userId) {
        return userIdToUsername.get(userId);
    }

    /**
     * 解绑用户ID
     */
    public void unbindUserId(Long userId) {
        userIdToUsername.remove(userId);
    }
}
