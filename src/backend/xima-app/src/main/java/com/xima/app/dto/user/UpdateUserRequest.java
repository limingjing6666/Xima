package com.xima.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    @Size(min = 6, max = 40, message = "密码长度必须在6-40之间")
    private String password;

    @Size(min = 6, max = 40, message = "新密码长度必须在6-40之间")
    private String newPassword;
}
