package com.xima.app.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private List<String> roles;
    
    /**
     * 是否需要用户确认（账号已在其他设备登录）
     */
    @Builder.Default
    private Boolean requireConfirm = false;
    
    /**
     * 提示消息
     */
    private String message;
}
