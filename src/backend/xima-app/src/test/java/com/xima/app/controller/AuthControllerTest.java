package com.xima.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.auth.LoginRequest;
import com.xima.app.dto.auth.LoginResponse;
import com.xima.app.dto.auth.RegisterRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

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
    }

    @Test
    @DisplayName("用户注册接口测试 - 成功")
    void register_Success() throws Exception {
        // Given
        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .nickname("Test User")
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("用户注册接口测试 - 参数校验失败")
    void register_ValidationFailed() throws Exception {
        // Given - 空用户名
        registerRequest.setUsername("");

        // When & Then
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("用户登录接口测试 - 成功")
    void login_Success() throws Exception {
        // Given
        LoginResponse loginResponse = LoginResponse.builder()
                .token("jwt-token")
                .username("testuser")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("用户登录接口测试 - 参数校验失败")
    void login_ValidationFailed() throws Exception {
        // Given - 空密码
        loginRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("刷新Token接口测试 - 成功")
    void refreshToken_Success() throws Exception {
        // Given
        when(authService.refreshToken(any(String.class))).thenReturn("new-jwt-token");

        // When & Then
        mockMvc.perform(post("/v1/auth/refresh")
                        .header("Authorization", "Bearer old-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("new-jwt-token"));
    }
}
