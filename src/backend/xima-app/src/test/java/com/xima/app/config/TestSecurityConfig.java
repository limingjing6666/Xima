package com.xima.app.config;

import com.xima.app.security.UserDetailsImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

/**
 * 测试用安全配置
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return username -> new UserDetailsImpl(
                1L,
                username,
                "password",
                "test@example.com",
                "Test User",
                null,
                Collections.emptyList()
        );
    }
}
