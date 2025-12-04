package com.xima.app.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
    
    /**
     * 是否强制登录（踢掉已登录的设备）
     */
    private Boolean forceLogin;
    
    public Boolean getForceLogin() {
        return forceLogin != null ? forceLogin : false;
    }
}
