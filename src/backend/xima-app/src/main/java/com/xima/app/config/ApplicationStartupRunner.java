package com.xima.app.config;

import com.xima.app.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时执行的初始化任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartupRunner implements ApplicationRunner {

    private final UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        // 重置所有用户状态为离线
        int count = userMapper.resetAllUsersStatus();
        log.info("应用启动：已重置 {} 个用户状态为离线", count);
    }
}
