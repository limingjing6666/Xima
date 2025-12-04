package com.xima.app.service.impl;

import com.xima.app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String CODE_PREFIX = "email:code:";
    private static final long CODE_EXPIRE_MINUTES = 5; // 验证码5分钟有效

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("【Xima即时通讯】邮箱验证码");
            
            String content = buildEmailContent(code);
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("验证码邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: {}", to, e);
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    @Override
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    @Override
    public void saveCode(String email, String code) {
        String key = CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("验证码已保存: {} -> {}", email, code);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode == null) {
            return false;
        }
        return savedCode.equals(code);
    }

    @Override
    public void removeCode(String email) {
        String key = CODE_PREFIX + email;
        redisTemplate.delete(key);
    }

    @Override
    public void sendResetPasswordCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("【Xima即时通讯】重置密码验证码");
            
            String content = buildResetPasswordEmailContent(code);
            helper.setText(content, true);
            
            mailSender.send(message);
            log.info("重置密码验证码邮件发送成功: {}", to);
        } catch (MessagingException e) {
            log.error("重置密码验证码邮件发送失败: {}", to, e);
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 构建注册验证码邮件HTML内容
     */
    private String buildEmailContent(String code) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>" +
            "body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; padding: 20px; }" +
            ".container { max-width: 500px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%); padding: 30px; text-align: center; }" +
            ".header h1 { color: #fff; margin: 0; font-size: 24px; }" +
            ".content { padding: 30px; text-align: center; }" +
            ".code { font-size: 36px; font-weight: bold; color: #1a1a2e; letter-spacing: 8px; padding: 20px; background: #f8f9fa; border-radius: 8px; margin: 20px 0; }" +
            ".tip { color: #666; font-size: 14px; margin-top: 20px; }" +
            ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"container\">" +
            "<div class=\"header\">" +
            "<h1>Xima 即时通讯</h1>" +
            "</div>" +
            "<div class=\"content\">" +
            "<p>您好！您正在注册 Xima 账号，请使用以下验证码完成验证：</p>" +
            "<div class=\"code\">" + code + "</div>" +
            "<p class=\"tip\">验证码有效期为 5 分钟，请勿将验证码告知他人。</p>" +
            "<p class=\"tip\">如果这不是您的操作，请忽略此邮件。</p>" +
            "</div>" +
            "<div class=\"footer\">" +
            "<p>此邮件由系统自动发送，请勿回复</p>" +
            "<p>© 2024 Xima 即时通讯</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }

    /**
     * 构建重置密码验证码邮件HTML内容
     */
    private String buildResetPasswordEmailContent(String code) {
        return "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>" +
            "body { font-family: 'Microsoft YaHei', Arial, sans-serif; background: #f5f5f5; padding: 20px; }" +
            ".container { max-width: 500px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }" +
            ".header { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); padding: 30px; text-align: center; }" +
            ".header h1 { color: #fff; margin: 0; font-size: 24px; }" +
            ".content { padding: 30px; text-align: center; }" +
            ".code { font-size: 36px; font-weight: bold; color: #e74c3c; letter-spacing: 8px; padding: 20px; background: #fdf2f2; border-radius: 8px; margin: 20px 0; }" +
            ".tip { color: #666; font-size: 14px; margin-top: 20px; }" +
            ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"container\">" +
            "<div class=\"header\">" +
            "<h1>Xima 即时通讯</h1>" +
            "</div>" +
            "<div class=\"content\">" +
            "<p>您好！您正在重置 Xima 账号密码，请使用以下验证码完成验证：</p>" +
            "<div class=\"code\">" + code + "</div>" +
            "<p class=\"tip\">验证码有效期为 5 分钟，请勿将验证码告知他人。</p>" +
            "<p class=\"tip\">如果这不是您的操作，请立即检查账号安全。</p>" +
            "</div>" +
            "<div class=\"footer\">" +
            "<p>此邮件由系统自动发送，请勿回复</p>" +
            "<p>© 2024 Xima 即时通讯</p>" +
            "</div>" +
            "</div>" +
            "</body>" +
            "</html>";
    }
}
