package com.xima.app.dto.group;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群组DTO
 */
@Data
public class GroupDTO {
    private Long id;
    private String name;
    private String avatar;
    private String description;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
    private Integer maxMembers;
    private Integer memberCount;
    private LocalDateTime createTime;
    
    // 当前用户在群中的角色
    private String myRole;
    
    // 前四个成员的头像列表（用于组合头像）
    private List<String> memberAvatars;
}
