# 构建阶段
FROM docker.1panel.live/library/maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 配置阿里云 Maven 镜像
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"\n\
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n\
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\n\
  <mirrors>\n\
    <mirror>\n\
      <id>aliyun</id>\n\
      <mirrorOf>central</mirrorOf>\n\
      <name>Aliyun Maven Mirror</name>\n\
      <url>https://maven.aliyun.com/repository/public</url>\n\
    </mirror>\n\
  </mirrors>\n\
</settings>' > /root/.m2/settings.xml

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM docker.1panel.live/library/openjdk:17-jdk-slim
WORKDIR /app

# 安装 curl 用于健康检查
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

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
