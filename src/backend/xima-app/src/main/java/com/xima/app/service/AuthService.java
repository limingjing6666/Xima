package com.xima.app.service;

import com.xima.app.dto.auth.LoginRequest;
import com.xima.app.dto.auth.LoginResponse;
import com.xima.app.dto.auth.RegisterRequest;
import com.xima.app.dto.auth.ResetPasswordRequest;
import com.xima.app.dto.user.UserDTO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    UserDTO register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新Token
     */
    String refreshToken(String token);

    /**
     * 重置密码
     */
    void resetPassword(ResetPasswordRequest request);
}
