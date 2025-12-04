package com.xima.app.controller;

import com.xima.app.common.Result;
import com.xima.app.dto.auth.LoginRequest;
import com.xima.app.dto.auth.LoginResponse;
import com.xima.app.dto.auth.RegisterRequest;
import com.xima.app.dto.auth.ResetPasswordRequest;
import com.xima.app.dto.auth.SendCodeRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.service.AuthService;
import com.xima.app.service.EmailService;
import com.xima.app.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token刷新等接口")
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Operation(summary = "用户注册", description = "新用户注册账号")
    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return Result.success("注册成功", user);
    }

    @Operation(summary = "用户登录", description = "用户登录获取JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "刷新Token", description = "使用旧Token获取新Token")
    @PostMapping("/refresh")
    public Result<String> refreshToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        String newToken = authService.refreshToken(token);
        return Result.success("Token刷新成功", newToken);
    }

    @Operation(summary = "发送验证码", description = "发送邮箱验证码用于注册")
    @PostMapping("/send-code")
    public Result<String> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        String email = request.getEmail();
        
        // 检查邮箱是否已被注册
        if (userMapper.findByEmail(email) != null) {
            return Result.error(400, "该邮箱已被注册");
        }
        
        try {
            // 生成并发送验证码
            String code = emailService.generateCode();
            emailService.saveCode(email, code);
            emailService.sendVerificationCode(email, code);
            
            return Result.success("验证码已发送到您的邮箱，5分钟内有效");
        } catch (Exception e) {
            return Result.error(500, "邮件发送失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发送重置密码验证码", description = "发送邮箱验证码用于重置密码")
    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendCodeRequest request) {
        String email = request.getEmail();
        
        // 检查邮箱是否已注册
        if (userMapper.findByEmail(email) == null) {
            return Result.error(400, "该邮箱未注册");
        }
        
        try {
            // 生成并发送验证码
            String code = emailService.generateCode();
            emailService.saveCode(email, code);
            emailService.sendResetPasswordCode(email, code);
            
            return Result.success("验证码已发送到您的邮箱，5分钟内有效");
        } catch (Exception e) {
            return Result.error(500, "邮件发送失败: " + e.getMessage());
        }
    }

    @Operation(summary = "重置密码", description = "通过邮箱验证码重置密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Result.success("密码重置成功，请使用新密码登录");
    }
}
