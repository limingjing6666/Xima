package com.xima.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.UserStatus;
import com.xima.app.security.JwtTokenProvider;
import com.xima.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .nickname("Test User")
                .status(UserStatus.ONLINE)
                .build();
    }

    @Test
    @DisplayName("获取用户信息接口测试 - 成功")
    @WithMockUser(username = "testuser")
    void getUserById_Success() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("搜索用户接口测试 - 成功")
    @WithMockUser(username = "testuser")
    void searchUsers_Success() throws Exception {
        // Given
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.searchUsers(anyString())).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/v1/users/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("testuser"));
    }

    @Test
    @DisplayName("获取所有用户接口测试 - 需要管理员权限")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_AdminAccess() throws Exception {
        // Given
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取所有用户接口测试 - 普通用户无权限")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAllUsers_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("删除用户接口测试 - 需要管理员权限")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteUser_AdminAccess() throws Exception {
        // When & Then
        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("未认证访问测试")
    void unauthenticated_AccessDenied() throws Exception {
        // When & Then - Spring Security 默认返回 403 而不是 401
        mockMvc.perform(get("/v1/users/1"))
                .andExpect(status().isForbidden());
    }
}
