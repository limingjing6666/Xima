package com.xima.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    private Long id;
    private Long userId;
    private Long friendId;
    private FriendshipStatus status = FriendshipStatus.PENDING;
    private String remark;  // 好友备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Friendship(Long userId, Long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public Friendship(Long userId, Long friendId, FriendshipStatus status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }
}