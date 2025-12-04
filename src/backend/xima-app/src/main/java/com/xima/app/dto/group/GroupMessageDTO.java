package com.xima.app.dto.group;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群消息DTO
 */
@Data
public class GroupMessageDTO {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderUsername;  // 发送者用户名
    private String senderName;      // 发送者昵称
    private String senderRemark;    // 好友备注（如果是好友）
    private String senderAvatar;
    private String content;
    private String contentType;
    private Boolean recalled;           // 是否已撤回
    private LocalDateTime createTime;
    
    /**
     * 获取显示名称（优先显示好友备注）
     */
    public String getDisplayName() {
        return senderRemark != null && !senderRemark.isEmpty() ? senderRemark : senderName;
    }
}
