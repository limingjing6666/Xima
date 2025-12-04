package com.xima.app.service;

import com.xima.app.dto.message.ChatMessageDTO;
import com.xima.app.entity.Message;
import com.xima.app.entity.MessageContentType;
import com.xima.app.entity.MessageStatus;
import com.xima.app.entity.User;
import com.xima.app.exception.BusinessException;
import com.xima.app.mapper.GroupMapper;
import com.xima.app.mapper.GroupMemberMapper;
import com.xima.app.mapper.GroupMessageMapper;
import com.xima.app.mapper.MessageMapper;
import com.xima.app.mapper.UserMapper;
import com.xima.app.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GroupMessageMapper groupMessageMapper;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User sender;
    private User receiver;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setNickname("Sender User");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");
        receiver.setNickname("Receiver User");

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setSenderId(1L);
        testMessage.setReceiverId(2L);
        testMessage.setContent("Hello!");
        testMessage.setContentType(MessageContentType.TEXT);
        testMessage.setStatus(MessageStatus.SENT);
        testMessage.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取聊天历史成功")
    void getChatHistory_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.findChatHistory(1L, 2L, 0, 20)).thenReturn(messages);
        when(userMapper.findById(1L)).thenReturn(sender);
        when(userMapper.findById(2L)).thenReturn(receiver);

        // When
        List<ChatMessageDTO> result = messageService.getChatHistory(1L, 2L, 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello!", result.get(0).getContent());
        assertEquals("Sender User", result.get(0).getSenderName());
    }

    @Test
    @DisplayName("获取聊天历史 - 空结果")
    void getChatHistory_Empty() {
        // Given
        when(messageMapper.findChatHistory(1L, 2L, 0, 20)).thenReturn(Collections.emptyList());

        // When
        List<ChatMessageDTO> result = messageService.getChatHistory(1L, 2L, 0, 20);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取离线消息成功")
    void getOfflineMessages_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.findOfflineMessages(2L)).thenReturn(messages);
        when(userMapper.findById(1L)).thenReturn(sender);
        when(userMapper.findById(2L)).thenReturn(receiver);

        // When
        List<ChatMessageDTO> result = messageService.getOfflineMessages(2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(messageMapper).updateStatusToDelivered(2L);
    }

    @Test
    @DisplayName("标记消息已读成功")
    void markAsRead_Success() {
        // Given
        when(messageMapper.findById(1L)).thenReturn(testMessage);

        // When & Then
        assertDoesNotThrow(() -> messageService.markAsRead(1L));
        verify(messageMapper).updateStatus(1L, MessageStatus.READ.name());
    }

    @Test
    @DisplayName("标记消息已读失败 - 消息不存在")
    void markAsRead_MessageNotFound() {
        // Given
        when(messageMapper.findById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> messageService.markAsRead(999L));
    }

    @Test
    @DisplayName("获取未读消息数成功")
    void getUnreadCount_Success() {
        // Given
        when(messageMapper.countUnreadMessages(2L, 1L)).thenReturn(5L);

        // When
        long count = messageService.getUnreadCount(2L, 1L);

        // Then
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("删除消息成功")
    void deleteMessage_Success() {
        // Given
        when(messageMapper.findById(1L)).thenReturn(testMessage);

        // When & Then
        assertDoesNotThrow(() -> messageService.deleteMessage(1L, 1L));
        verify(messageMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除消息失败 - 消息不存在")
    void deleteMessage_MessageNotFound() {
        // Given
        when(messageMapper.findById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> messageService.deleteMessage(1L, 999L));
    }

    @Test
    @DisplayName("删除消息失败 - 无权限")
    void deleteMessage_AccessDenied() {
        // Given
        when(messageMapper.findById(1L)).thenReturn(testMessage);

        // When & Then - user 3 trying to delete message sent by user 1
        assertThrows(BusinessException.class, () -> messageService.deleteMessage(3L, 1L));
        verify(messageMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("搜索消息成功")
    void searchMessages_Success() {
        // Given
        List<Message> messages = Arrays.asList(testMessage);
        when(messageMapper.searchMessages(1L, "Hello", 0, 20)).thenReturn(messages);
        when(userMapper.findById(1L)).thenReturn(sender);
        when(userMapper.findById(2L)).thenReturn(receiver);

        // When
        List<ChatMessageDTO> result = messageService.searchMessages(1L, "Hello", 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello!", result.get(0).getContent());
    }

    @Test
    @DisplayName("搜索消息 - 无结果")
    void searchMessages_NoResults() {
        // Given
        when(messageMapper.searchMessages(1L, "xyz", 0, 20)).thenReturn(Collections.emptyList());

        // When
        List<ChatMessageDTO> result = messageService.searchMessages(1L, "xyz", 0, 20);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
