package com.xima.app.dto.user;

import com.xima.app.entity.User;
import com.xima.app.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息DTO（不包含敏感信息）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private UserStatus status;
    private List<String> roles;
    private LocalDateTime createTime;

    /**
     * 从实体转换为DTO
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .createTime(user.getCreateTime())
                .build();
    }
}
