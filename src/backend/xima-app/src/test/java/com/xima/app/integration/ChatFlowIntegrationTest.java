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
 * 聊天流程集成测试
 * 测试好友添加->消息发送->消息查询完整流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("需要完整数据库环境，手动运行: mvn test -Dtest=ChatFlowIntegrationTest -Dspring.profiles.active=default")
class ChatFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String getAuthToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("token").asText();
    }

    @Test
    @Order(1)
    @DisplayName("集成测试 - 获取好友列表")
    void getFriendList() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/friends")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("集成测试 - 获取聊天历史")
    void getChatHistory() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/messages/history/2")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(3)
    @DisplayName("集成测试 - 获取离线消息")
    void getOfflineMessages() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/messages/offline")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("集成测试 - 检查好友关系")
    void checkFriendship() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/friends/check/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isBoolean());
    }

    @Test
    @Order(5)
    @DisplayName("集成测试 - 获取好友数量")
    void getFriendCount() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/friends/count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @Order(6)
    @DisplayName("集成测试 - 搜索用户")
    void searchUsers() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/users/search")
                        .header("Authorization", "Bearer " + token)
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(7)
    @DisplayName("集成测试 - 获取用户信息")
    void getUserInfo() throws Exception {
        String token = getAuthToken("testuser", "password123");

        mockMvc.perform(get("/v1/users/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser2"));
    }
}
