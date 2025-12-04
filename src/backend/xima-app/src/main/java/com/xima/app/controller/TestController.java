package com.xima.app.controller;

import com.xima.app.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器（无需认证）
 */
@Tag(name = "系统测试", description = "健康检查和测试接口")
@RestController
@RequestMapping("/v1/test")
public class TestController {

    @Operation(summary = "健康检查", description = "检查应用是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Xima Chat Application is running!");
    }

    @Operation(summary = "测试接口", description = "简单的测试接口")
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello, Xima!");
    }
}
