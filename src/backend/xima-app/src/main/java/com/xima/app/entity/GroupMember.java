package com.xima.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群成员实体
 */
@Data
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private String nickname;
    private MemberRole role;
    private Boolean muted;
    private LocalDateTime joinTime;
    
    public enum MemberRole {
        OWNER,   // 群主
        ADMIN,   // 管理员
        MEMBER   // 普通成员
    }
}
