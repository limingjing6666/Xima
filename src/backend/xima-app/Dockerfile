# 构建阶段
FROM maven:3.8.6-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim
WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads/avatars /app/uploads/backgrounds /app/uploads/chat/images /app/uploads/chat/files

# 复制jar包
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/test/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
