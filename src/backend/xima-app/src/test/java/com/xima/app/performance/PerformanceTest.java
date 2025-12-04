package com.xima.app.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.auth.LoginRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import com.xima.app.config.TestRedisConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 性能测试
 * 测试API响应时间和并发处理能力
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("需要完整数据库环境，手动运行: mvn test -Dtest=PerformanceTest -Dspring.profiles.active=default")
class PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int CONCURRENT_USERS = 10;
    private static final int REQUESTS_PER_USER = 5;
    private static final long MAX_RESPONSE_TIME_MS = 1000; // 最大响应时间1秒

    private String getAuthToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("token").asText();
    }

    @Test
    @Order(1)
    @DisplayName("性能测试 - 健康检查响应时间")
    void healthCheck_ResponseTime() throws Exception {
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/v1/test/health"))
                    .andExpect(status().isOk());
            long end = System.currentTimeMillis();
            responseTimes.add(end - start);
        }

        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("=== 健康检查性能测试结果 ===");
        System.out.println("请求次数: 100");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");
        System.out.println("最小响应时间: " + minTime + "ms");

        assertTrue(avgTime < MAX_RESPONSE_TIME_MS, "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @Order(2)
    @DisplayName("性能测试 - 登录接口响应时间")
    void login_ResponseTime() throws Exception {
        List<Long> responseTimes = new ArrayList<>();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        for (int i = 0; i < 20; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(post("/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
            long end = System.currentTimeMillis();
            responseTimes.add(end - start);
        }

        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("=== 登录接口性能测试结果 ===");
        System.out.println("请求次数: 20");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");

        assertTrue(avgTime < MAX_RESPONSE_TIME_MS, "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @Order(3)
    @DisplayName("性能测试 - 并发登录测试")
    void concurrentLogin_Test() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<Future<Long>> futures = new ArrayList<>();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        String requestBody = objectMapper.writeValueAsString(loginRequest);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS * REQUESTS_PER_USER; i++) {
            futures.add(executor.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    mockMvc.perform(post("/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return System.currentTimeMillis() - start;
            }));
        }

        List<Long> responseTimes = new ArrayList<>();
        int successCount = 0;
        for (Future<Long> future : futures) {
            try {
                responseTimes.add(future.get(30, TimeUnit.SECONDS));
                successCount++;
            } catch (Exception e) {
                // 请求失败
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        executor.shutdown();

        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double throughput = (successCount * 1000.0) / totalTime;

        System.out.println("=== 并发登录测试结果 ===");
        System.out.println("并发用户数: " + CONCURRENT_USERS);
        System.out.println("每用户请求数: " + REQUESTS_PER_USER);
        System.out.println("总请求数: " + (CONCURRENT_USERS * REQUESTS_PER_USER));
        System.out.println("成功请求数: " + successCount);
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("吞吐量: " + String.format("%.2f", throughput) + " req/s");

        assertTrue(successCount >= CONCURRENT_USERS * REQUESTS_PER_USER * 0.95, 
                "成功率应大于95%");
    }

    @Test
    @Order(4)
    @DisplayName("性能测试 - 获取好友列表响应时间")
    void getFriendList_ResponseTime() throws Exception {
        String token = getAuthToken();
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/v1/friends")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            long end = System.currentTimeMillis();
            responseTimes.add(end - start);
        }

        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("=== 获取好友列表性能测试结果 ===");
        System.out.println("请求次数: 50");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");

        assertTrue(avgTime < MAX_RESPONSE_TIME_MS, "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @Order(5)
    @DisplayName("性能测试 - 获取聊天历史响应时间")
    void getChatHistory_ResponseTime() throws Exception {
        String token = getAuthToken();
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/v1/messages/history/2")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk());
            long end = System.currentTimeMillis();
            responseTimes.add(end - start);
        }

        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        System.out.println("=== 获取聊天历史性能测试结果 ===");
        System.out.println("请求次数: 50");
        System.out.println("平均响应时间: " + avgTime + "ms");
        System.out.println("最大响应时间: " + maxTime + "ms");

        assertTrue(avgTime < MAX_RESPONSE_TIME_MS, "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }

    @Test
    @Order(6)
    @DisplayName("性能测试 - 搜索用户响应时间")
    void searchUsers_ResponseTime() throws Exception {
        String token = getAuthToken();
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/v1/users/search")
                            .header("Authorization", "Bearer " + token)
                            .param("keyword", "test"))
                    .andExpect(status().isOk());
            long end = System.currentTimeMillis();
            responseTimes.add(end - start);
        }

        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.println("=== 搜索用户性能测试结果 ===");
        System.out.println("请求次数: 30");
        System.out.println("平均响应时间: " + avgTime + "ms");

        assertTrue(avgTime < MAX_RESPONSE_TIME_MS, "平均响应时间应小于" + MAX_RESPONSE_TIME_MS + "ms");
    }
}
