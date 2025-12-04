package com.xima.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.friend.FriendDTO;
import com.xima.app.dto.friend.FriendRequestDTO;
import com.xima.app.dto.friend.FriendRequestInfoDTO;
import com.xima.app.entity.UserStatus;
import com.xima.app.service.FriendshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.xima.app.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FriendshipController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class FriendshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FriendshipService friendshipService;

    private FriendDTO testFriend;
    private FriendRequestInfoDTO testRequest;

    @BeforeEach
    void setUp() {
        testFriend = FriendDTO.builder()
                .userId(2L)
                .username("friend")
                .nickname("Friend User")
                .status(UserStatus.ONLINE)
                .build();

        testRequest = FriendRequestInfoDTO.builder()
                .requestId(1L)
                .fromUserId(2L)
                .fromUsername("requester")
                .fromNickname("Requester User")
                .build();
    }

    @Test
    @DisplayName("发送好友请求 - 成功")
    @WithUserDetails("testuser")
    void sendFriendRequest_Success() throws Exception {
        // Given
        FriendRequestDTO request = new FriendRequestDTO();
        request.setFriendId(2L);
        doNothing().when(friendshipService).sendFriendRequest(anyLong(), eq(2L));

        // When & Then
        mockMvc.perform(post("/v1/friends/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(friendshipService).sendFriendRequest(anyLong(), eq(2L));
    }

    @Test
    @DisplayName("接受好友请求 - 成功")
    @WithUserDetails("testuser")
    void acceptFriendRequest_Success() throws Exception {
        // Given
        doNothing().when(friendshipService).acceptFriendRequest(anyLong(), eq(1L));

        // When & Then
        mockMvc.perform(post("/v1/friends/request/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(friendshipService).acceptFriendRequest(anyLong(), eq(1L));
    }

    @Test
    @DisplayName("拒绝好友请求 - 成功")
    @WithUserDetails("testuser")
    void rejectFriendRequest_Success() throws Exception {
        // Given
        doNothing().when(friendshipService).rejectFriendRequest(anyLong(), eq(1L));

        // When & Then
        mockMvc.perform(post("/v1/friends/request/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(friendshipService).rejectFriendRequest(anyLong(), eq(1L));
    }

    @Test
    @DisplayName("删除好友 - 成功")
    @WithUserDetails("testuser")
    void deleteFriend_Success() throws Exception {
        // Given
        doNothing().when(friendshipService).deleteFriend(anyLong(), eq(2L));

        // When & Then
        mockMvc.perform(delete("/v1/friends/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(friendshipService).deleteFriend(anyLong(), eq(2L));
    }

    @Test
    @DisplayName("获取好友列表 - 成功")
    @WithUserDetails("testuser")
    void getFriendList_Success() throws Exception {
        // Given
        List<FriendDTO> friends = Arrays.asList(testFriend);
        when(friendshipService.getFriendList(anyLong())).thenReturn(friends);

        // When & Then
        mockMvc.perform(get("/v1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].username").value("friend"));
    }

    @Test
    @DisplayName("获取好友列表 - 空列表")
    @WithUserDetails("testuser")
    void getFriendList_Empty() throws Exception {
        // Given
        when(friendshipService.getFriendList(anyLong())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/v1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("获取待处理请求 - 成功")
    @WithUserDetails("testuser")
    void getPendingRequests_Success() throws Exception {
        // Given
        List<FriendRequestInfoDTO> requests = Arrays.asList(testRequest);
        when(friendshipService.getPendingRequests(anyLong())).thenReturn(requests);

        // When & Then
        mockMvc.perform(get("/v1/friends/requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fromUsername").value("requester"));
    }

    @Test
    @DisplayName("获取已发送请求 - 成功")
    @WithUserDetails("testuser")
    void getSentRequests_Success() throws Exception {
        // Given
        List<FriendRequestInfoDTO> requests = Arrays.asList(testRequest);
        when(friendshipService.getSentRequests(anyLong())).thenReturn(requests);

        // When & Then
        mockMvc.perform(get("/v1/friends/requests/sent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("检查好友关系 - 是好友")
    @WithUserDetails("testuser")
    void checkFriendship_IsFriend() throws Exception {
        // Given
        when(friendshipService.isFriend(anyLong(), eq(2L))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/v1/friends/check/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("检查好友关系 - 不是好友")
    @WithUserDetails("testuser")
    void checkFriendship_NotFriend() throws Exception {
        // Given
        when(friendshipService.isFriend(anyLong(), eq(2L))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/v1/friends/check/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("获取好友数量 - 成功")
    @WithUserDetails("testuser")
    void getFriendCount_Success() throws Exception {
        // Given
        when(friendshipService.getFriendCount(anyLong())).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/v1/friends/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    @DisplayName("设置好友备注 - 成功")
    @WithUserDetails("testuser")
    void setFriendRemark_Success() throws Exception {
        // Given
        Map<String, String> body = new HashMap<>();
        body.put("remark", "Best Friend");
        doNothing().when(friendshipService).setFriendRemark(anyLong(), eq(2L), eq("Best Friend"));

        // When & Then
        mockMvc.perform(put("/v1/friends/2/remark")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(friendshipService).setFriendRemark(anyLong(), eq(2L), eq("Best Friend"));
    }

    @Test
    @DisplayName("未认证访问 - 拒绝")
    void unauthenticated_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/friends"))
                .andExpect(status().isForbidden());
    }
}
