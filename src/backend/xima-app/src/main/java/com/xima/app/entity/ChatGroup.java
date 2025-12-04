package com.xima.app.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群组实体
 */
@Data
public class ChatGroup {
    private Long id;
    private String name;
    private String avatar;
    private String description;
    private Long ownerId;
    private Integer maxMembers;
    private Integer memberCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
