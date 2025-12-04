package com.xima.app.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 发送验证码请求DTO
 */
@Data
public class SendCodeRequest {

    @NotBlank(message = "QQ邮箱不能为空")
    @Pattern(regexp = "^[1-9]\\d{4,10}@qq\\.com$", message = "请输入正确的QQ邮箱格式")
    private String email;
}
