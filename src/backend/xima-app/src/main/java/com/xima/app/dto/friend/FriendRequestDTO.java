package com.xima.app.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 好友请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDTO {

    @NotNull(message = "好友ID不能为空")
    private Long friendId;

    private String message;  // 验证消息（可选）
}
