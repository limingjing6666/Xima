package com.xima.app.dto.friend;

import com.xima.app.entity.FriendshipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友请求信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestInfoDTO {

    private Long requestId;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String fromAvatar;
    private FriendshipStatus status;
    private LocalDateTime createTime;
}
