#!/bin/bash
# Xima 数据备份脚本

set -e

APP_DIR="/opt/xima"
BACKUP_DIR="/opt/xima/backups"
DATE=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo "  Xima 数据备份"
echo "=========================================="

# 创建备份目录
mkdir -p $BACKUP_DIR

cd $APP_DIR

# 备份MySQL数据库
echo "[1/3] 备份 MySQL 数据库..."
docker exec xima-mysql mysqldump -u xima -pxima123456 xima > $BACKUP_DIR/mysql_$DATE.sql
gzip $BACKUP_DIR/mysql_$DATE.sql
echo "MySQL 备份完成: $BACKUP_DIR/mysql_$DATE.sql.gz"

# 备份Redis数据
echo "[2/3] 备份 Redis 数据..."
docker exec xima-redis redis-cli BGSAVE
sleep 2
docker cp xima-redis:/data/dump.rdb $BACKUP_DIR/redis_$DATE.rdb
echo "Redis 备份完成: $BACKUP_DIR/redis_$DATE.rdb"

# 备份上传文件
echo "[3/3] 备份上传文件..."
if docker volume inspect xima_uploads_data &> /dev/null; then
    docker run --rm -v xima_uploads_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/uploads_$DATE.tar.gz -C /data .
    echo "上传文件备份完成: $BACKUP_DIR/uploads_$DATE.tar.gz"
fi

# 清理7天前的备份
echo ""
echo "清理旧备份..."
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*.rdb" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo ""
echo "=========================================="
echo "  备份完成！"
echo "=========================================="
echo "备份文件位置: $BACKUP_DIR"
ls -lh $BACKUP_DIR | grep $DATE
