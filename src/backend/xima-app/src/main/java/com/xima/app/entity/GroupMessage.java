package com.xima.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群消息实体
 */
@Data
public class GroupMessage {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String content;
    private MessageContentType contentType = MessageContentType.TEXT;
    private Boolean recalled = false;  // 是否已撤回
    private LocalDateTime createTime;
}
