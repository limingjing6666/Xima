package com.xima.app.service;

import com.xima.app.dto.auth.LoginRequest;
import com.xima.app.dto.auth.LoginResponse;
import com.xima.app.dto.auth.RegisterRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.Role;
import com.xima.app.entity.RoleType;
import com.xima.app.entity.User;
import com.xima.app.exception.BusinessException;
import com.xima.app.mapper.UserMapper;
import com.xima.app.security.JwtTokenProvider;
import com.xima.app.security.UserDetailsImpl;
import com.xima.app.service.impl.AuthServiceImpl;
import com.xima.app.service.EmailService;
import com.xima.app.websocket.WebSocketSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("123456789@qq.com");
        registerRequest.setNickname("Test User");
        registerRequest.setCode("123456");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setNickname("Test User");
        
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleType.ROLE_USER);
        roles.add(role);
        testUser.setRoles(roles);
    }

    @Test
    @DisplayName("用户注册成功")
    void register_Success() {
        // Given
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.findRoleIdByName(anyString())).thenReturn(1L);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(userMapper.findById(any())).thenReturn(testUser);

        // When
        UserDTO result = authService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册失败 - 用户名已存在")
    void register_UsernameExists() {
        // Given
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册失败 - 邮箱已存在")
    void register_EmailExists() {
        // Given
        when(emailService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.register(registerRequest));
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("用户登录成功")
    void login_Success() {
        // Given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(1L);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getNickname()).thenReturn("Test User");
        when(userDetails.getEmail()).thenReturn("test@example.com");
        when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());
        
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");
        when(sessionManager.isOnline(anyString())).thenReturn(false);

        // When
        LoginResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("刷新Token成功")
    void refreshToken_Success() {
        // Given
        String oldToken = "old-token";
        when(jwtTokenProvider.validateToken(oldToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(oldToken)).thenReturn("testuser");
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("new-token");

        // When
        String newToken = authService.refreshToken(oldToken);

        // Then
        assertEquals("new-token", newToken);
    }

    @Test
    @DisplayName("刷新Token失败 - Token无效")
    void refreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThrows(BusinessException.class, () -> authService.refreshToken(invalidToken));
    }
}
