package com.xima.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private MessageContentType contentType = MessageContentType.TEXT;
    private MessageStatus status = MessageStatus.SENT;
    private Boolean recalled = false;  // 是否已撤回
    private LocalDateTime createTime;

    public Message(Long senderId, Long receiverId, String content, MessageContentType contentType) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.contentType = contentType;
    }
}