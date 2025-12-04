package com.xima.app.dto.friend;

import com.xima.app.entity.FriendshipStatus;
import com.xima.app.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {

    private Long friendshipId;
    private Long userId;
    private String username;
    private String nickname;
    private String remark;  // 好友备注
    private String avatar;
    private UserStatus status;
    private FriendshipStatus friendshipStatus;
    private LocalDateTime createTime;
}
