package com.xima.app.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // 使用内存缓存（生产环境可替换为Redis）
        return new ConcurrentMapCacheManager(
                "users",           // 用户信息缓存
                "groups",          // 群组信息缓存
                "groupMembers",    // 群成员缓存
                "friendships"      // 好友关系缓存
        );
    }
}
