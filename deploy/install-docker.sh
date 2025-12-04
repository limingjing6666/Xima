#!/bin/bash
# CentOS 7 Docker 安装脚本

set -e

echo "=========================================="
echo "  Xima - CentOS 7 Docker 安装脚本"
echo "=========================================="

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "请使用 root 用户运行此脚本"
    exit 1
fi

# 1. 卸载旧版本Docker
echo "[1/6] 卸载旧版本 Docker..."
yum remove -y docker \
    docker-client \
    docker-client-latest \
    docker-common \
    docker-latest \
    docker-latest-logrotate \
    docker-logrotate \
    docker-engine 2>/dev/null || true

# 2. 安装依赖
echo "[2/6] 安装依赖包..."
yum install -y yum-utils device-mapper-persistent-data lvm2

# 3. 添加Docker仓库
echo "[3/6] 添加 Docker 仓库..."
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 如果官方源太慢，可以使用阿里云镜像
# yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 4. 安装Docker
echo "[4/6] 安装 Docker CE..."
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 5. 配置Docker
echo "[5/6] 配置 Docker..."
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<EOF
{
    "registry-mirrors": [
        "https://mirror.ccs.tencentyun.com",
        "https://docker.mirrors.ustc.edu.cn",
        "https://hub-mirror.c.163.com"
    ],
    "log-driver": "json-file",
    "log-opts": {
        "max-size": "100m",
        "max-file": "3"
    },
    "storage-driver": "overlay2"
}
EOF

# 6. 启动Docker
echo "[6/6] 启动 Docker 服务..."
systemctl daemon-reload
systemctl start docker
systemctl enable docker

# 验证安装
echo ""
echo "=========================================="
echo "  Docker 安装完成！"
echo "=========================================="
docker --version
docker compose version

echo ""
echo "下一步: 运行 ./deploy.sh 部署应用"
