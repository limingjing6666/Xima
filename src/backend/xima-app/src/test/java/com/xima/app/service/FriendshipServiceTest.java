package com.xima.app.service;

import com.xima.app.dto.friend.FriendDTO;
import com.xima.app.entity.Friendship;
import com.xima.app.entity.FriendshipStatus;
import com.xima.app.entity.User;
import com.xima.app.entity.UserStatus;
import com.xima.app.exception.BusinessException;
import com.xima.app.mapper.FriendshipMapper;
import com.xima.app.mapper.UserMapper;
import com.xima.app.service.impl.FriendshipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FriendshipService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipMapper friendshipMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    private User user1;
    private User user2;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setNickname("User One");
        user1.setStatus(UserStatus.ONLINE);

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setNickname("User Two");
        user2.setStatus(UserStatus.ONLINE);

        friendship = new Friendship();
        friendship.setId(1L);
        friendship.setUserId(1L);
        friendship.setFriendId(2L);
        friendship.setStatus(FriendshipStatus.PENDING);
    }

    @Test
    @DisplayName("发送好友请求成功")
    void sendFriendRequest_Success() {
        // Given
        when(userMapper.findById(2L)).thenReturn(user2);
        when(friendshipMapper.existsFriendship(1L, 2L)).thenReturn(false);
        when(friendshipMapper.insert(any(Friendship.class))).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> friendshipService.sendFriendRequest(1L, 2L));
        verify(friendshipMapper).insert(any(Friendship.class));
    }

    @Test
    @DisplayName("发送好友请求失败 - 不能添加自己")
    void sendFriendRequest_CannotAddSelf() {
        // When & Then
        assertThrows(BusinessException.class, () -> friendshipService.sendFriendRequest(1L, 1L));
        verify(friendshipMapper, never()).insert(any(Friendship.class));
    }

    @Test
    @DisplayName("发送好友请求失败 - 用户不存在")
    void sendFriendRequest_UserNotFound() {
        // Given
        when(userMapper.findById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> friendshipService.sendFriendRequest(1L, 999L));
    }

    @Test
    @DisplayName("发送好友请求失败 - 已存在请求")
    void sendFriendRequest_AlreadyExists() {
        // Given
        when(userMapper.findById(2L)).thenReturn(user2);
        when(friendshipMapper.existsFriendship(1L, 2L)).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> friendshipService.sendFriendRequest(1L, 2L));
    }

    @Test
    @DisplayName("接受好友请求成功")
    void acceptFriendRequest_Success() {
        // Given
        when(friendshipMapper.findById(1L)).thenReturn(friendship);
        when(friendshipMapper.updateStatus(eq(1L), anyString())).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> friendshipService.acceptFriendRequest(2L, 1L));
        verify(friendshipMapper).updateStatus(1L, FriendshipStatus.ACCEPTED.name());
    }

    @Test
    @DisplayName("接受好友请求失败 - 请求不存在")
    void acceptFriendRequest_NotFound() {
        // Given
        when(friendshipMapper.findById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> friendshipService.acceptFriendRequest(2L, 999L));
    }

    @Test
    @DisplayName("接受好友请求失败 - 无权限")
    void acceptFriendRequest_AccessDenied() {
        // Given
        when(friendshipMapper.findById(1L)).thenReturn(friendship);

        // When & Then - user3 trying to accept request meant for user2
        assertThrows(BusinessException.class, () -> friendshipService.acceptFriendRequest(3L, 1L));
    }

    @Test
    @DisplayName("拒绝好友请求成功")
    void rejectFriendRequest_Success() {
        // Given
        when(friendshipMapper.findById(1L)).thenReturn(friendship);
        when(friendshipMapper.updateStatus(eq(1L), anyString())).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> friendshipService.rejectFriendRequest(2L, 1L));
        verify(friendshipMapper).updateStatus(1L, FriendshipStatus.REJECTED.name());
    }

    @Test
    @DisplayName("删除好友成功")
    void deleteFriend_Success() {
        // Given
        when(friendshipMapper.deleteByUserIdAndFriendId(1L, 2L)).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> friendshipService.deleteFriend(1L, 2L));
        verify(friendshipMapper).deleteByUserIdAndFriendId(1L, 2L);
    }

    @Test
    @DisplayName("获取好友列表成功")
    void getFriendList_Success() {
        // Given
        Friendship acceptedFriendship = new Friendship();
        acceptedFriendship.setId(1L);
        acceptedFriendship.setUserId(1L);
        acceptedFriendship.setFriendId(2L);
        acceptedFriendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipMapper.findFriendsByUserId(1L)).thenReturn(Arrays.asList(acceptedFriendship));
        when(userMapper.findById(2L)).thenReturn(user2);

        // When
        List<FriendDTO> result = friendshipService.getFriendList(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user2", result.get(0).getUsername());
    }

    @Test
    @DisplayName("检查好友关系 - 是好友")
    void isFriend_True() {
        // Given
        Friendship acceptedFriendship = new Friendship();
        acceptedFriendship.setStatus(FriendshipStatus.ACCEPTED);
        when(friendshipMapper.findByUserIdAndFriendId(1L, 2L)).thenReturn(acceptedFriendship);

        // When
        boolean result = friendshipService.isFriend(1L, 2L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查好友关系 - 不是好友")
    void isFriend_False() {
        // Given
        when(friendshipMapper.findByUserIdAndFriendId(1L, 2L)).thenReturn(null);
        when(friendshipMapper.findByUserIdAndFriendId(2L, 1L)).thenReturn(null);

        // When
        boolean result = friendshipService.isFriend(1L, 2L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("获取好友数量成功")
    void getFriendCount_Success() {
        // Given
        when(friendshipMapper.countFriends(1L)).thenReturn(5L);

        // When
        long count = friendshipService.getFriendCount(1L);

        // Then
        assertEquals(5L, count);
    }
}
