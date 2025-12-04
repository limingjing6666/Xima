#!/bin/bash
# Xima 一键部署脚本 - CentOS 7

set -e

# 配置变量
APP_NAME="xima"
APP_DIR="/opt/xima"
DOMAIN=""  # 如果有域名，填写在这里

echo "=========================================="
echo "  Xima 一键部署脚本"
echo "=========================================="

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "请使用 root 用户运行此脚本"
    exit 1
fi

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "Docker 未安装，请先运行 ./install-docker.sh"
    exit 1
fi

# 1. 创建应用目录
echo "[1/7] 创建应用目录..."
mkdir -p $APP_DIR
cd $APP_DIR

# 2. 检查项目文件
if [ ! -f "docker-compose.yml" ]; then
    echo "错误: 请先将项目文件上传到 $APP_DIR"
    echo "需要的文件:"
    echo "  - docker-compose.yml"
    echo "  - docker/ 目录"
    echo "  - src/ 目录"
    exit 1
fi

# 3. 配置防火墙
echo "[2/7] 配置防火墙..."
if systemctl is-active --quiet firewalld; then
    firewall-cmd --permanent --add-port=80/tcp
    firewall-cmd --permanent --add-port=443/tcp
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
    echo "防火墙规则已添加"
else
    echo "firewalld 未运行，跳过防火墙配置"
fi

# 4. 创建环境变量文件
echo "[3/7] 创建环境变量文件..."
if [ ! -f ".env" ]; then
    cat > .env <<EOF
# MySQL配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=xima
MYSQL_USER=xima
MYSQL_PASSWORD=xima123456

# JWT配置 (请修改为随机字符串)
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION=86400000

# 邮件配置 (可选)
MAIL_USERNAME=
MAIL_PASSWORD=
EOF
    echo "已创建 .env 文件，请根据需要修改配置"
fi

# 5. 设置目录权限
echo "[4/7] 设置目录权限..."
chmod -R 755 docker/
chmod 644 docker/mysql/conf/my.cnf
chmod 644 docker/redis/redis.conf

# 6. 构建并启动服务
echo "[5/7] 构建 Docker 镜像..."
docker compose build --no-cache

echo "[6/7] 启动服务..."
docker compose up -d

# 7. 等待服务启动
echo "[7/7] 等待服务启动..."
echo "等待 MySQL 启动..."
sleep 10

# 检查服务状态
echo ""
echo "=========================================="
echo "  服务状态"
echo "=========================================="
docker compose ps

# 获取服务器IP
SERVER_IP=$(hostname -I | awk '{print $1}')

echo ""
echo "=========================================="
echo "  部署完成！"
echo "=========================================="
echo ""
echo "访问地址:"
echo "  前端: http://$SERVER_IP"
echo "  后端API: http://$SERVER_IP:8080/api"
echo "  Swagger: http://$SERVER_IP:8080/api/swagger-ui.html"
echo ""
echo "默认账号:"
echo "  用户名: admin / test"
echo "  密码: 123456"
echo ""
echo "常用命令:"
echo "  查看日志: docker compose logs -f"
echo "  重启服务: docker compose restart"
echo "  停止服务: docker compose down"
echo ""
