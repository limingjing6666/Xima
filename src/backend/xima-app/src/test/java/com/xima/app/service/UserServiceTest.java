package com.xima.app.service;

import com.xima.app.dto.user.UpdateUserRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.User;
import com.xima.app.entity.UserStatus;
import com.xima.app.exception.BusinessException;
import com.xima.app.mapper.UserMapper;
import com.xima.app.service.impl.UserServiceImpl;
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
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setNickname("Test User");
        testUser.setStatus(UserStatus.ONLINE);
    }

    @Test
    @DisplayName("根据ID获取用户成功")
    void getUserById_Success() {
        // Given
        when(userMapper.findById(1L)).thenReturn(testUser);

        // When
        UserDTO result = userService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("根据ID获取用户失败 - 用户不存在")
    void getUserById_NotFound() {
        // Given
        when(userMapper.findById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("更新用户信息成功")
    void updateUser_Success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setNickname("New Nickname");
        request.setAvatar("new-avatar.jpg");

        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.update(any(User.class))).thenReturn(1);

        // When
        UserDTO result = userService.updateUser(1L, request);

        // Then
        assertNotNull(result);
        verify(userMapper).update(any(User.class));
    }

    @Test
    @DisplayName("更新用户状态成功")
    void updateUserStatus_Success() {
        // Given
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.updateStatus(eq(1L), anyString())).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> userService.updateUserStatus(1L, UserStatus.BUSY));
        verify(userMapper).updateStatus(1L, UserStatus.BUSY.name());
    }

    @Test
    @DisplayName("搜索用户成功")
    void searchUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userMapper.findAll()).thenReturn(users);

        // When
        List<UserDTO> result = userService.searchUsers("test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    @DisplayName("获取所有用户成功")
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userMapper.findAll()).thenReturn(users);

        // When
        List<UserDTO> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("删除用户成功")
    void deleteUser_Success() {
        // Given
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        // When & Then
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除用户失败 - 用户不存在")
    void deleteUser_NotFound() {
        // Given
        when(userMapper.findById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(BusinessException.class, () -> userService.deleteUser(999L));
        verify(userMapper, never()).deleteById(anyLong());
    }
}
