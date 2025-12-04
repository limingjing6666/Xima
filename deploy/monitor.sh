#!/bin/bash
# Xima 服务监控脚本

APP_DIR="/opt/xima"
LOG_FILE="/var/log/xima-monitor.log"

cd $APP_DIR

# 检查后端服务
check_backend() {
    if curl -s --max-time 10 http://localhost:8080/api/v1/test/health | grep -q "running"; then
        return 0
    else
        return 1
    fi
}

# 检查MySQL
check_mysql() {
    if docker exec xima-mysql mysqladmin ping -h localhost -u root -proot123456 &> /dev/null; then
        return 0
    else
        return 1
    fi
}

# 检查Redis
check_redis() {
    if docker exec xima-redis redis-cli ping | grep -q "PONG"; then
        return 0
    else
        return 1
    fi
}

# 主检查逻辑
echo "$(date '+%Y-%m-%d %H:%M:%S') - 开始健康检查" >> $LOG_FILE

# 检查后端
if ! check_backend; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') - [ERROR] Backend 服务异常，尝试重启..." >> $LOG_FILE
    docker compose restart backend
    sleep 30
    if check_backend; then
        echo "$(date '+%Y-%m-%d %H:%M:%S') - [INFO] Backend 重启成功" >> $LOG_FILE
    else
        echo "$(date '+%Y-%m-%d %H:%M:%S') - [CRITICAL] Backend 重启失败！" >> $LOG_FILE
    fi
fi

# 检查MySQL
if ! check_mysql; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') - [ERROR] MySQL 服务异常，尝试重启..." >> $LOG_FILE
    docker compose restart mysql
    sleep 30
fi

# 检查Redis
if ! check_redis; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') - [ERROR] Redis 服务异常，尝试重启..." >> $LOG_FILE
    docker compose restart redis
    sleep 10
fi

# 检查磁盘空间
DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | tr -d '%')
if [ "$DISK_USAGE" -gt 85 ]; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') - [WARNING] 磁盘使用率: ${DISK_USAGE}%" >> $LOG_FILE
fi

# 检查内存使用
MEM_USAGE=$(free | awk '/Mem:/ {printf "%.0f", $3/$2 * 100}')
if [ "$MEM_USAGE" -gt 90 ]; then
    echo "$(date '+%Y-%m-%d %H:%M:%S') - [WARNING] 内存使用率: ${MEM_USAGE}%" >> $LOG_FILE
fi
