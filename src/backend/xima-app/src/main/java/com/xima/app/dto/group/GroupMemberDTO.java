package com.xima.app.dto.group;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 群成员DTO
 */
@Data
public class GroupMemberDTO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String groupNickname;  // 群内昵称
    private String role;           // OWNER/ADMIN/MEMBER
    private Boolean muted;
    private LocalDateTime joinTime;
    private String status;         // 在线状态
}
