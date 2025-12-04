package com.xima.app.controller;

import com.xima.app.dto.message.ChatMessageDTO;
import com.xima.app.dto.message.SearchMessageDTO;
import com.xima.app.entity.MessageContentType;
import com.xima.app.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.xima.app.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MessageController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    private ChatMessageDTO testMessage;
    private SearchMessageDTO testSearchMessage;

    @BeforeEach
    void setUp() {
        testMessage = ChatMessageDTO.builder()
                .id(1L)
                .senderId(1L)
                .senderName("Sender")
                .receiverId(2L)
                .receiverName("Receiver")
                .content("Hello!")
                .contentType(MessageContentType.TEXT)
                .timestamp(LocalDateTime.now())
                .build();

        testSearchMessage = SearchMessageDTO.builder()
                .id(1L)
                .chatType("PRIVATE")
                .senderId(1L)
                .senderName("Sender")
                .content("Hello!")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("获取聊天历史 - 成功")
    @WithUserDetails("testuser")
    void getChatHistory_Success() throws Exception {
        // Given
        List<ChatMessageDTO> messages = Arrays.asList(testMessage);
        when(messageService.getChatHistory(anyLong(), eq(2L), eq(0), eq(20))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/v1/messages/history/2")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].content").value("Hello!"));
    }

    @Test
    @DisplayName("获取聊天历史 - 空结果")
    @WithUserDetails("testuser")
    void getChatHistory_Empty() throws Exception {
        // Given
        when(messageService.getChatHistory(anyLong(), anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/v1/messages/history/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("获取离线消息 - 成功")
    @WithUserDetails("testuser")
    void getOfflineMessages_Success() throws Exception {
        // Given
        List<ChatMessageDTO> messages = Arrays.asList(testMessage);
        when(messageService.getOfflineMessages(anyLong())).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/v1/messages/offline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("标记消息已读 - 成功")
    @WithUserDetails("testuser")
    void markAsRead_Success() throws Exception {
        // Given
        doNothing().when(messageService).markAsRead(1L);

        // When & Then
        mockMvc.perform(post("/v1/messages/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService).markAsRead(1L);
    }

    @Test
    @DisplayName("获取未读消息数 - 成功")
    @WithUserDetails("testuser")
    void getUnreadCount_Success() throws Exception {
        // Given
        when(messageService.getUnreadCount(anyLong(), eq(2L))).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/v1/messages/unread/count/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("删除消息 - 成功")
    @WithUserDetails("testuser")
    void deleteMessage_Success() throws Exception {
        // Given
        doNothing().when(messageService).deleteMessage(anyLong(), eq(1L));

        // When & Then
        mockMvc.perform(delete("/v1/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("搜索消息 - 成功")
    @WithUserDetails("testuser")
    void searchMessages_Success() throws Exception {
        // Given
        List<SearchMessageDTO> messages = Arrays.asList(testSearchMessage);
        when(messageService.searchAllMessages(anyLong(), eq("Hello"), eq(0), eq(20)))
                .thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/v1/messages/search")
                        .param("keyword", "Hello")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].content").value("Hello!"));
    }

    @Test
    @DisplayName("未认证访问 - 拒绝")
    void unauthenticated_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/messages/history/2"))
                .andExpect(status().isForbidden());
    }
}
