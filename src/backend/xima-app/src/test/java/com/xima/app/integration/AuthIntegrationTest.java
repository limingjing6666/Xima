package com.xima.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.auth.LoginRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import com.xima.app.config.TestRedisConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证流程集成测试
 * 测试完整的注册->登录->访问受保护资源流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("需要完整数据库环境，手动运行: mvn test -Dtest=AuthIntegrationTest -Dspring.profiles.active=default")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;

    @Test
    @Order(1)
    @DisplayName("集成测试 - 健康检查")
    void healthCheck() throws Exception {
        mockMvc.perform(get("/v1/test/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("Xima Chat Application is running!"));
    }

    @Test
    @Order(2)
    @DisplayName("集成测试 - 用户登录获取Token")
    void login_Success() throws Exception {
        // Given - 使用初始化数据中的测试用户
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // When & Then
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andReturn();

        // 保存Token供后续测试使用
        String response = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("data").get("token").asText();
    }

    @Test
    @Order(3)
    @DisplayName("集成测试 - 使用Token访问受保护资源")
    void accessProtectedResource_WithToken() throws Exception {
        // 先登录获取token
        if (authToken == null) {
            login_Success();
        }

        // When & Then - 访问用户信息接口
        mockMvc.perform(get("/v1/users/me")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @Order(4)
    @DisplayName("集成测试 - 无Token访问受保护资源被拒绝")
    void accessProtectedResource_WithoutToken() throws Exception {
        mockMvc.perform(get("/v1/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    @DisplayName("集成测试 - 无效Token访问被拒绝")
    void accessProtectedResource_WithInvalidToken() throws Exception {
        mockMvc.perform(get("/v1/users/me")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("集成测试 - 登录失败（错误密码）")
    void login_WrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(7)
    @DisplayName("集成测试 - 登录失败（用户不存在）")
    void login_UserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
