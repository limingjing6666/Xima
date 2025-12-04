package com.xima.app.dto.message;

import com.xima.app.entity.MessageContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    /**
     * 消息类型
     */
    public enum MessageType {
        CHAT,           // 私聊消息
        GROUP_CHAT,     // 群聊消息
        JOIN,           // 用户加入
        LEAVE,          // 用户离开
        TYPING,         // 正在输入
        READ,           // 已读回执
        SYSTEM,         // 系统消息
        STATUS,         // 状态变化（上线/下线）
        ERROR,          // 错误消息
        KICK,           // 被踢下线（账号在其他设备登录）
        RECALL,         // 消息撤回
        NOTIFICATION    // 通知消息
    }

    private Long id;
    private MessageType type;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;       // 私聊接收者ID
    private String receiverName;
    private Long groupId;          // 群ID（群聊时使用）
    private String groupName;      // 群名称
    private String content;
    private MessageContentType contentType;
    private LocalDateTime timestamp;
    private Boolean recalled;           // 是否已撤回
}
