package com.xima.app.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息搜索结果DTO（统一私聊和群聊）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchMessageDTO {
    
    private Long id;
    
    // 消息类型：PRIVATE-私聊，GROUP-群聊
    private String chatType;
    
    // 私聊相关
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String receiverName;
    
    // 群聊相关
    private Long groupId;
    private String groupName;
    
    // 消息内容
    private String content;
    private LocalDateTime timestamp;
}
