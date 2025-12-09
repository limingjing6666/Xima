# Xima CentOS 7 云服务器部署指南

## 1. 服务器要求

| 配置项 | 最低要求 | 推荐配置 |
|--------|----------|----------|
| CPU | 1核 | 2核+ |
| 内存 | 2GB | 4GB+ |
| 磁盘 | 20GB | 50GB+ |
| 系统 | CentOS 7.6+ | CentOS 7.9 |
| 带宽 | 1Mbps | 5Mbps+ |

## 2. 部署步骤

### 2.1 上传项目文件

**方式一：使用 scp 上传**
```bash
# 在本地执行，将项目打包上传
cd Xima
tar -czf xima.tar.gz --exclude=node_modules --exclude=target --exclude=.git .
scp xima.tar.gz root@你的服务器IP:/opt/
```

**方式二：使用 Git 克隆**
```bash
# 在服务器上执行
cd /opt
git clone https://github.com/你的用户名/Xima.git xima
```

### 2.2 服务器端操作

```bash
# 1. SSH登录服务器
ssh root@你的服务器IP

# 2. 解压项目（如果使用scp上传）
cd /opt
tar -xzf xima.tar.gz -C xima
cd xima

# 3. 安装 Docker (已配置阿里云镜像源 + 国内加速器)
chmod +x deploy/*.sh
./deploy/install-docker.sh

# 4. 部署应用
./deploy/deploy.sh
```

### 2.3 验证部署

```bash
# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f

# 测试健康检查
curl http://localhost:8080/api/v1/test/health
```

## 3. 防火墙配置

### 3.1 firewalld（推荐）
```bash
# 开放端口
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --reload

# 查看已开放端口
firewall-cmd --list-ports
```

### 3.2 iptables
```bash
# 开放端口
iptables -I INPUT -p tcp --dport 80 -j ACCEPT
iptables -I INPUT -p tcp --dport 443 -j ACCEPT
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
service iptables save
```

### 3.3 云服务器安全组

在云服务商控制台配置安全组规则：

| 端口 | 协议 | 说明 |
|------|------|------|
| 80 | TCP | HTTP |
| 443 | TCP | HTTPS |
| 8080 | TCP | 后端API |
| 22 | TCP | SSH（限制IP） |

## 4. 域名配置（可选）

### 4.1 DNS解析
在域名服务商添加 A 记录，指向服务器IP

### 4.2 修改Nginx配置
```bash
# 编辑配置文件
vim docker/nginx/conf.d/default.conf

# 修改 server_name
server_name your-domain.com;
```

### 4.3 配置SSL证书（Let's Encrypt）
```bash
# 安装certbot
yum install -y epel-release
yum install -y certbot

# 获取证书
certbot certonly --standalone -d your-domain.com

# 证书位置
# /etc/letsencrypt/live/your-domain.com/fullchain.pem
# /etc/letsencrypt/live/your-domain.com/privkey.pem

# 复制证书到项目目录
cp /etc/letsencrypt/live/your-domain.com/fullchain.pem docker/nginx/ssl/cert.pem
cp /etc/letsencrypt/live/your-domain.com/privkey.pem docker/nginx/ssl/key.pem

# 重启nginx
docker compose restart nginx
```

## 5. 常用运维命令

### 5.1 服务管理
```bash
cd /opt/xima

# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 重启某个服务
docker compose restart backend

# 查看日志
docker compose logs -f backend
docker compose logs -f --tail=100 mysql

# 进入容器
docker exec -it xima-backend sh
docker exec -it xima-mysql mysql -u xima -p
```

### 5.2 更新部署
```bash
cd /opt/xima

# 拉取最新代码
git pull

# 重新构建并部署
docker compose down
docker compose build --no-cache
docker compose up -d
```

### 5.3 数据备份
```bash
# 手动备份
./deploy/backup.sh

# 设置定时备份（每天凌晨3点）
crontab -e
# 添加以下行：
0 3 * * * /opt/xima/deploy/backup.sh >> /var/log/xima-backup.log 2>&1
```

### 5.4 查看资源使用
```bash
# 查看容器资源使用
docker stats

# 查看磁盘使用
df -h
docker system df
```

## 6. 故障排查

### 6.1 服务无法启动
```bash
# 查看详细日志
docker compose logs backend

# 检查端口占用
netstat -tlnp | grep 8080

# 检查Docker状态
systemctl status docker
```

### 6.2 数据库连接失败
```bash
# 检查MySQL容器
docker compose logs mysql

# 测试连接
docker exec xima-mysql mysql -u xima -pxima123456 -e "SELECT 1"

# 检查网络
docker network ls
docker network inspect xima_xima-network
```

### 6.3 内存不足
```bash
# 查看内存使用
free -h

# 添加swap（如果内存不足）
dd if=/dev/zero of=/swapfile bs=1M count=2048
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile swap swap defaults 0 0' >> /etc/fstab
```

### 6.4 重置部署
```bash
cd /opt/xima

# 停止并删除所有容器和数据
docker compose down -v

# 清理Docker缓存
docker system prune -a

# 重新部署
./deploy/deploy.sh
```

## 7. 性能优化

### 7.1 系统优化
```bash
# 编辑 /etc/sysctl.conf
cat >> /etc/sysctl.conf <<EOF
# 网络优化
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65535
net.ipv4.tcp_fin_timeout = 30
net.ipv4.tcp_keepalive_time = 1200

# 文件描述符
fs.file-max = 65535
EOF

sysctl -p
```

### 7.2 Docker优化
```bash
# 限制日志大小（已在daemon.json配置）
# 定期清理无用镜像
docker image prune -a
```

## 8. 监控建议

### 8.1 简单监控脚本
```bash
#!/bin/bash
# /opt/xima/deploy/monitor.sh

# 检查服务状态
if ! curl -s http://localhost:8080/api/v1/test/health | grep -q "running"; then
    echo "$(date): Backend service is down!" >> /var/log/xima-monitor.log
    docker compose restart backend
fi
```

### 8.2 设置监控定时任务
```bash
# 每5分钟检查一次
*/5 * * * * /opt/xima/deploy/monitor.sh
```

## 9. 安全建议

1. **修改默认密码** - 部署后立即修改 MySQL、Redis 密码
2. **限制SSH访问** - 使用密钥登录，禁用密码登录
3. **定期更新** - 保持系统和Docker更新
4. **备份数据** - 设置自动备份
5. **监控日志** - 定期检查异常访问
