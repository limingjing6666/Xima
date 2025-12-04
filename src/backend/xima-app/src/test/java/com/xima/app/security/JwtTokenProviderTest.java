package com.xima.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtTokenProvider 单元测试
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // 设置测试用的secret和expiration
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", 
                "test_jwt_secret_key_for_unit_testing_purposes_only_must_be_long_enough");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
        // 手动调用init方法初始化key
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("生成Token成功")
    void generateToken_Success() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testuser", "password", "test@example.com", "Test User", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("从Token获取用户名成功")
    void getUsernameFromToken_Success() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testuser", "password", "test@example.com", "Test User", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("验证有效Token")
    void validateToken_Valid() {
        // Given
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testuser", "password", "test@example.com", "Test User", null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证无效Token")
    void validateToken_Invalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("根据用户名生成Token")
    void generateTokenByUsername_Success() {
        // When
        String token = jwtTokenProvider.generateToken("testuser");

        // Then
        assertNotNull(token);
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(token));
    }
}
