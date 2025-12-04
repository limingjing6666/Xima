package com.xima.app.service;

/**
 * 邮件服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);

    /**
     * 生成验证码
     * @return 6位数字验证码
     */
    String generateCode();

    /**
     * 保存验证码到缓存
     * @param email 邮箱
     * @param code 验证码
     */
    void saveCode(String email, String code);

    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String email, String code);

    /**
     * 删除验证码
     * @param email 邮箱
     */
    void removeCode(String email);

    /**
     * 发送重置密码验证码邮件
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendResetPasswordCode(String to, String code);
}
