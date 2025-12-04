package com.xima.app.service.impl;

import com.xima.app.dto.user.UpdateUserRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.User;
import com.xima.app.entity.UserStatus;
import com.xima.app.exception.BusinessException;
import com.xima.app.exception.ErrorCode;
import com.xima.app.mapper.UserMapper;
import com.xima.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return UserDTO.fromEntity(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return UserDTO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新昵称
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }

        // 更新邮箱
        if (StringUtils.hasText(request.getEmail())) {
            // 检查邮箱是否被其他用户使用
            User existingUser = userMapper.findByEmail(request.getEmail());
            if (existingUser != null && !existingUser.getId().equals(id)) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        // 更新头像
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        // 更新密码
        if (StringUtils.hasText(request.getNewPassword())) {
            if (!StringUtils.hasText(request.getPassword())) {
                throw new BusinessException("修改密码需要提供原密码");
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_ERROR);
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        userMapper.update(user);
        log.info("用户信息更新成功: {}", user.getUsername());

        return UserDTO.fromEntity(userMapper.findById(id));
    }

    @Override
    public void updateUserStatus(Long id, UserStatus status) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.updateStatus(id, status.name());
        log.info("用户状态更新: {} -> {}", user.getUsername(), status);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userMapper.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(id);
        log.info("用户删除成功: {}", user.getUsername());
    }

    @Override
    public List<UserDTO> searchUsers(String keyword) {
        // 简单实现：根据用户名模糊搜索
        return userMapper.findAll().stream()
                .filter(user -> user.getUsername().contains(keyword) ||
                        (user.getNickname() != null && user.getNickname().contains(keyword)))
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateAvatar(Long id, String avatarUrl) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.updateAvatar(id, avatarUrl);
        log.info("用户头像更新成功: {}", user.getUsername());
    }

    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.update(user);
        log.info("用户密码修改成功: {}", user.getUsername());
        return true;
    }

    @Override
    @Transactional
    public void updateChatBackground(Long id, String chatBackground) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userMapper.updateChatBackground(id, chatBackground);
        log.info("用户聊天背景更新成功: {}", user.getUsername());
    }

    @Override
    public String getChatBackground(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user.getChatBackground();
    }
}
