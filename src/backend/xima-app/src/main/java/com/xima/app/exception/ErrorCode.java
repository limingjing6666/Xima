package com.xima.app.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误 1000-1999
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(1000, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),

    // 用户相关错误 2000-2999
    USER_NOT_FOUND(2000, "用户不存在"),
    USER_ALREADY_EXISTS(2001, "用户已存在"),
    USERNAME_ALREADY_EXISTS(2002, "用户名已被使用"),
    EMAIL_ALREADY_EXISTS(2003, "邮箱已被使用"),
    PASSWORD_ERROR(2004, "密码错误"),
    USER_DISABLED(2005, "用户已被禁用"),

    // 认证相关错误 3000-3999
    UNAUTHORIZED(3000, "未登录或登录已过期"),
    TOKEN_INVALID(3001, "Token无效"),
    TOKEN_EXPIRED(3002, "Token已过期"),
    ACCESS_DENIED(3003, "无权限访问"),
    INVALID_VERIFICATION_CODE(3004, "验证码错误或已过期"),

    // 好友相关错误 4000-4999
    FRIEND_REQUEST_EXISTS(4000, "好友请求已存在"),
    ALREADY_FRIENDS(4001, "已经是好友关系"),
    FRIEND_REQUEST_NOT_FOUND(4002, "好友请求不存在"),
    CANNOT_ADD_SELF(4003, "不能添加自己为好友"),

    // 消息相关错误 5000-5999
    MESSAGE_SEND_FAILED(5000, "消息发送失败"),
    MESSAGE_NOT_FOUND(5001, "消息不存在"),
    RECEIVER_OFFLINE(5002, "接收者不在线");

    private final int code;
    private final String message;
}
