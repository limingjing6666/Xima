package com.xima.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String chatBackground;  // 聊天背景设置
    private UserStatus status = UserStatus.OFFLINE;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Set<Role> roles = new HashSet<>();

    // 构造函数，用于注册
    public User(String username, String password, String nickname, String email) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
    }
} 