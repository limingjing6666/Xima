package com.xima.app.service;

import com.xima.app.dto.user.UpdateUserRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.UserStatus;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据ID获取用户
     */
    UserDTO getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    UserDTO getUserByUsername(String username);

    /**
     * 更新用户信息
     */
    UserDTO updateUser(Long id, UpdateUserRequest request);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long id, UserStatus status);

    /**
     * 获取所有用户
     */
    List<UserDTO> getAllUsers();

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 搜索用户
     */
    List<UserDTO> searchUsers(String keyword);

    /**
     * 更新用户头像
     */
    void updateAvatar(Long id, String avatarUrl);

    /**
     * 修改密码
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 更新聊天背景
     */
    void updateChatBackground(Long id, String chatBackground);

    /**
     * 获取聊天背景
     */
    String getChatBackground(Long id);
}
