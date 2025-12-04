package com.xima.app.service.impl;

import com.xima.app.dto.auth.LoginRequest;
import com.xima.app.dto.auth.LoginResponse;
import com.xima.app.dto.auth.RegisterRequest;
import com.xima.app.dto.auth.ResetPasswordRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.RoleType;
import com.xima.app.entity.User;
import com.xima.app.entity.UserStatus;
import com.xima.app.exception.BusinessException;
import com.xima.app.exception.ErrorCode;
import com.xima.app.mapper.UserMapper;
import com.xima.app.security.JwtTokenProvider;
import com.xima.app.security.UserDetailsImpl;
import com.xima.app.service.AuthService;
import com.xima.app.service.EmailService;
import com.xima.app.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebSocketSessionManager sessionManager;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        // 验证邮箱验证码
        if (!emailService.verifyCode(request.getEmail(), request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userMapper.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // 自动生成昵称: nickname_uuid (取UUID前8位)
        String randomNickname = "nickname_" + UUID.randomUUID().toString().substring(0, 8);
        user.setNickname(randomNickname);
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.OFFLINE);

        userMapper.insert(user);

        // 分配默认角色
        Long roleId = userMapper.findRoleIdByName(RoleType.ROLE_USER.name());
        if (roleId != null) {
            userMapper.insertUserRole(user.getId(), roleId);
        }

        // 重新查询用户（包含角色信息）
        User savedUser = userMapper.findById(user.getId());
        log.info("用户注册成功: {}", savedUser.getUsername());
        
        // 注册成功后删除验证码
        emailService.removeCode(request.getEmail());

        return UserDTO.fromEntity(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 先进行认证（验证用户名密码）
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        // 密码验证通过后，检查用户是否已在线（WebSocket连接中）
        boolean isOnline = sessionManager.isOnline(request.getUsername());
        
        // 如果用户已在线且不是强制登录，返回需要确认的响应
        if (isOnline && !Boolean.TRUE.equals(request.getForceLogin())) {
            return LoginResponse.builder()
                    .requireConfirm(true)
                    .message("检测到该账号已在其他设备登录，是否强制登录？")
                    .build();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 获取用户信息
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 更新用户状态为在线
        userMapper.updateStatus(userDetails.getId(), UserStatus.ONLINE.name());

        log.info("用户登录成功: {}", userDetails.getUsername());

        return LoginResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .nickname(userDetails.getNickname())
                .email(userDetails.getEmail())
                .avatar(userDetails.getAvatar())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public String refreshToken(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            return jwtTokenProvider.generateToken(username);
        }
        throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 验证邮箱验证码
        if (!emailService.verifyCode(request.getEmail(), request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 查找用户
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.update(user);

        // 删除验证码
        emailService.removeCode(request.getEmail());

        log.info("用户密码重置成功: {}", user.getUsername());
    }
}
