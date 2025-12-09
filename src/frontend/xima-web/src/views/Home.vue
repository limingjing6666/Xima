<template>
  <div class="home-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <router-link to="/chat" class="sidebar-logo">
        <span>Xima</span>
      </router-link>
      
      <nav class="sidebar-nav">
        <el-tooltip content="消息" placement="right" :show-after="300" :disabled="isMobile">
          <router-link to="/chat" class="nav-item" :class="{ active: activeMenu === '/chat' }">
            <el-icon><ChatDotRound /></el-icon>
            <span class="nav-label">消息</span>
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
        </el-tooltip>
        
        <el-tooltip content="通讯录" placement="right" :show-after="300" :disabled="isMobile">
          <router-link to="/contacts" class="nav-item" :class="{ active: activeMenu === '/contacts' }">
            <el-icon><User /></el-icon>
            <span class="nav-label">通讯录</span>
          </router-link>
        </el-tooltip>
        
        <el-tooltip content="设置" placement="right" :show-after="300" :disabled="isMobile">
          <router-link to="/settings" class="nav-item" :class="{ active: activeMenu === '/settings' }">
            <el-icon><Setting /></el-icon>
            <span class="nav-label">设置</span>
          </router-link>
        </el-tooltip>
      </nav>
      
      <!-- 桌面端：用户头像和弹出菜单 -->
      <div class="sidebar-footer" ref="userMenuRef" v-if="!isMobile">
        <el-tooltip content="个人中心" placement="right" :show-after="300">
          <div class="user-avatar-wrapper" @click="showUserMenu = !showUserMenu">
            <div class="user-avatar">
              <el-avatar :size="36" :src="userAvatarUrl">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="online-dot"></span>
            </div>
          </div>
        </el-tooltip>
        
        <transition name="fade">
          <div v-if="showUserMenu" class="user-menu">
            <!-- 顶部背景装饰 -->
            <div class="user-menu-bg">
              <div class="bg-pattern"></div>
              <div class="close-btn" @click="showUserMenu = false">
                <el-icon><Close /></el-icon>
              </div>
            </div>
            
            <!-- 用户信息区 -->
            <div class="user-menu-header">
              <div class="avatar-wrapper" @click="navigateTo('/settings')">
                <el-avatar :size="64" :src="userAvatarUrl">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="status-badge online"></span>
                <div class="avatar-edit">
                  <el-icon><Edit /></el-icon>
                </div>
              </div>
              <div class="user-menu-info">
                <div class="user-menu-name">{{ userStore.userInfo?.nickname }}</div>
                <div class="user-menu-username">@{{ userStore.userInfo?.username }}</div>
              </div>
              <div class="user-menu-status-tag">
                <span class="status-dot"></span>
                在线
              </div>
            </div>
            
            <!-- 菜单项 -->
            <div class="user-menu-list">
              <div class="menu-item" @click="navigateTo('/settings')">
                <el-icon><User /></el-icon>
                <span>个人资料</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="menu-item" @click="showNotificationSettings">
                <el-icon><Bell /></el-icon>
                <span>通知设置</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="menu-item" @click="showSecuritySettings">
                <el-icon><Lock /></el-icon>
                <span>隐私与安全</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
              <div class="menu-item" @click="showAbout">
                <el-icon><InfoFilled /></el-icon>
                <span>关于 Xima</span>
                <el-icon class="arrow"><ArrowRight /></el-icon>
              </div>
            </div>
            
            <!-- 退出按钮 -->
            <div class="user-menu-footer">
              <div class="logout-btn" @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Lock, ArrowRight, Close, Edit, InfoFilled, UserFilled } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import config from '@/config'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const activeMenu = computed(() => route.path)
const unreadCount = computed(() => chatStore.totalUnreadCount)
const showUserMenu = ref(false)
const userMenuRef = ref(null)

// 检测是否移动端
const isMobile = ref(window.innerWidth <= 767)

// 点击外部关闭用户菜单
const handleClickOutside = (event) => {
  if (userMenuRef.value && !userMenuRef.value.contains(event.target)) {
    showUserMenu.value = false
  }
}

// 处理头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) {
    return avatar
  }
  const path = avatar.startsWith('/') ? avatar : '/' + avatar
  // 原生 App：拼接服务器地址
  if (config.isNative()) {
    return config.getResourceUrl(path)
  }
  // Web 环境：直接返回路径
  return path
}

// 计算用户头像URL
const userAvatarUrl = computed(() => getAvatarUrl(userStore.userInfo?.avatar))

// 通知设置状态（需要在使用前定义）
const notificationEnabled = ref(localStorage.getItem('notificationEnabled') !== 'false')
const soundEnabled = ref(localStorage.getItem('soundEnabled') !== 'false')

// 消息提示音
let notificationSound = null

// 初始化提示音
const initNotificationSound = () => {
  notificationSound = new Audio()
  // 使用 Web Audio API 生成简单的提示音
  const audioContext = new (window.AudioContext || window.webkitAudioContext)()
  const oscillator = audioContext.createOscillator()
  const gainNode = audioContext.createGain()
  
  oscillator.connect(gainNode)
  gainNode.connect(audioContext.destination)
  
  oscillator.frequency.value = 800
  oscillator.type = 'sine'
  gainNode.gain.value = 0.3
  
  // 创建一个可重复播放的音频
  notificationSound = {
    play: () => {
      try {
        const ctx = new (window.AudioContext || window.webkitAudioContext)()
        const osc = ctx.createOscillator()
        const gain = ctx.createGain()
        
        osc.connect(gain)
        gain.connect(ctx.destination)
        
        osc.frequency.value = 800
        osc.type = 'sine'
        gain.gain.setValueAtTime(0.3, ctx.currentTime)
        gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.3)
        
        osc.start(ctx.currentTime)
        osc.stop(ctx.currentTime + 0.3)
      } catch (e) {
        console.log('无法播放提示音:', e)
      }
    }
  }
}

// 播放消息提示音
const playNotificationSound = () => {
  if (notificationSound && soundEnabled.value) {
    notificationSound.play()
  }
}

// 请求浏览器通知权限
const requestNotificationPermission = async () => {
  if ('Notification' in window && Notification.permission === 'default') {
    await Notification.requestPermission()
  }
}

// 发送浏览器通知
const sendBrowserNotification = (title, body, icon) => {
  if (!notificationEnabled.value) return
  if ('Notification' in window && Notification.permission === 'granted') {
    // 只在页面不可见时发送通知
    if (document.hidden) {
      const notification = new Notification(title, {
        body: body,
        icon: icon || '/favicon.ico',
        tag: 'xima-message',
        renotify: true
      })
      
      notification.onclick = () => {
        window.focus()
        notification.close()
      }
      
      // 5秒后自动关闭
      setTimeout(() => notification.close(), 5000)
    }
  }
}

// WebSocket连接
let ws = null

const connectWebSocket = () => {
  const token = userStore.token
  if (!token) return
  
  const wsUrl = `${config.getWsBaseUrl()}?token=${token}`
  ws = new WebSocket(wsUrl)
  
  ws.onopen = () => {
    console.log('WebSocket connected')
  }
  
  ws.onmessage = (event) => {
    const message = JSON.parse(event.data)
    // 忽略系统消息
    if (message.type === 'SYSTEM') return
    
    // 处理错误消息（如禁言提示）
    if (message.type === 'ERROR') {
      ElMessage.error(message.content || '操作失败')
      return
    }
    
    // 处理被踢下线消息
    if (message.type === 'KICK') {
      ElMessage.warning({
        message: message.content || '您的账号在其他设备登录',
        duration: 5000
      })
      // 关闭WebSocket连接
      if (ws) {
        ws.close()
        ws = null
      }
      // 登出并跳转到登录页
      userStore.logout()
      return
    }
    
    // 处理好友状态变化消息
    if (message.type === 'STATUS') {
      chatStore.updateFriendStatus(message.senderId, message.content)
      return
    }
    
    // 处理消息撤回
    if (message.type === 'RECALL') {
      chatStore.recallMessage(message.id, message.groupId, message.content)
      return
    }
    
    // 处理群聊消息
    if (message.type === 'GROUP_CHAT') {
      // 忽略自己发送的消息（但群消息需要显示）
      // 播放提示音和发送通知（非自己发送的消息）
      if (message.senderId !== userStore.userInfo?.id) {
        playNotificationSound()
        sendBrowserNotification(
          message.groupName || '群消息',
          `${message.senderName}: ${message.content?.substring(0, 50) || '[消息]'}`,
          message.senderAvatar
        )
      }
      chatStore.receiveGroupMessage(message)
      return
    }
    
    // 自己发送的私聊消息 - 添加到消息列表（带有ID）
    if (message.senderId === userStore.userInfo?.id) {
      chatStore.addSentMessage(message)
      return
    }
    
    // 收到好友消息，播放提示音和发送通知
    playNotificationSound()
    sendBrowserNotification(
      message.senderName || '新消息',
      message.content?.substring(0, 50) || '[消息]',
      message.senderAvatar
    )
    
    chatStore.receiveMessage(message)
  }
  
  ws.onclose = () => {
    console.log('WebSocket disconnected')
    // 只有在用户仍然登录状态时才尝试重连
    if (userStore.token) {
      setTimeout(connectWebSocket, 3000)
    }
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
}

const handleLogout = () => {
  if (ws) {
    ws.close()
  }
  userStore.logout()
}

// 导航到指定页面
const navigateTo = (path) => {
  showUserMenu.value = false
  router.push(path)
}

// 显示通知设置
const showNotificationSettings = () => {
  showUserMenu.value = false
  ElMessageBox({
    title: '通知设置',
    message: `
      <div style="padding: 8px 0;">
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 16px; background: #f8f9fa; border-radius: 10px; margin-bottom: 10px;">
          <div>
            <div style="font-weight: 600; color: #1a1a2e; font-size: 14px;">消息通知</div>
            <div style="font-size: 12px; color: #9ca3af; margin-top: 4px;">接收新消息时显示桌面通知</div>
          </div>
          <input type="checkbox" id="notif-toggle" ${notificationEnabled.value ? 'checked' : ''} style="width: 20px; height: 20px; cursor: pointer; accent-color: #1a1a2e;">
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 16px; background: #f8f9fa; border-radius: 10px;">
          <div>
            <div style="font-weight: 600; color: #1a1a2e; font-size: 14px;">提示音</div>
            <div style="font-size: 12px; color: #9ca3af; margin-top: 4px;">收到消息时播放提示音</div>
          </div>
          <input type="checkbox" id="sound-toggle" ${soundEnabled.value ? 'checked' : ''} style="width: 20px; height: 20px; cursor: pointer; accent-color: #1a1a2e;">
        </div>
      </div>
    `,
    dangerouslyUseHTMLString: true,
    showCancelButton: true,
    confirmButtonText: '保存',
    cancelButtonText: '取消',
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        const notifEl = document.getElementById('notif-toggle')
        const soundEl = document.getElementById('sound-toggle')
        if (notifEl && soundEl) {
          notificationEnabled.value = notifEl.checked
          soundEnabled.value = soundEl.checked
          localStorage.setItem('notificationEnabled', notifEl.checked.toString())
          localStorage.setItem('soundEnabled', soundEl.checked.toString())
          ElMessage.success('设置已保存')
        }
      }
      done()
    }
  })
}

// 显示隐私与安全设置
const showSecuritySettings = () => {
  showUserMenu.value = false
  ElMessageBox({
    title: '隐私与安全',
    message: `
      <div style="padding: 8px 0;">
        <div style="padding: 16px; background: #f8f9fa; border-radius: 10px; margin-bottom: 16px;">
          <div style="font-weight: 600; color: #1a1a2e; margin-bottom: 12px; font-size: 14px;">账号信息</div>
          <div style="font-size: 13px; color: #6b7280;">
            <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e5e7eb;">
              <span style="color: #9ca3af;">用户名</span>
              <span style="color: #1a1a2e; font-weight: 500;">${userStore.userInfo?.username || '-'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 8px 0;">
              <span style="color: #9ca3af;">邮箱</span>
              <span style="color: #1a1a2e; font-weight: 500;">${userStore.userInfo?.email || '未绑定'}</span>
            </div>
          </div>
        </div>
        <div id="change-password-btn" style="display: flex; align-items: center; justify-content: space-between; padding: 16px; background: #f8f9fa; border-radius: 10px; cursor: pointer; transition: background 0.2s;" onmouseover="this.style.background='#f0f0f0'" onmouseout="this.style.background='#f8f9fa'">
          <div>
            <div style="font-weight: 600; color: #1a1a2e; font-size: 14px;">修改密码</div>
            <div style="font-size: 12px; color: #9ca3af; margin-top: 4px;">定期更换密码保护账号安全</div>
          </div>
          <span style="color: #9ca3af;">›</span>
        </div>
      </div>
    `,
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭',
    showCancelButton: false
  }).then(() => {}).catch(() => {})
  
  // 绑定修改密码按钮事件
  setTimeout(() => {
    const btn = document.getElementById('change-password-btn')
    if (btn) {
      btn.onclick = () => {
        ElMessageBox.close()
        showChangePassword()
      }
    }
  }, 100)
}

// 显示修改密码弹窗
const showChangePassword = () => {
  ElMessageBox({
    title: '修改密码',
    message: `
      <div style="padding: 8px 0;">
        <div style="margin-bottom: 16px;">
          <label style="display: block; font-size: 13px; color: #6b7280; margin-bottom: 6px;">原密码</label>
          <input type="password" id="old-password" placeholder="请输入原密码" style="width: 100%; padding: 10px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box;" onfocus="this.style.borderColor='#1a1a2e'" onblur="this.style.borderColor='#e5e7eb'">
        </div>
        <div style="margin-bottom: 16px;">
          <label style="display: block; font-size: 13px; color: #6b7280; margin-bottom: 6px;">新密码</label>
          <input type="password" id="new-password" placeholder="请输入新密码（至少6位）" style="width: 100%; padding: 10px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box;" onfocus="this.style.borderColor='#1a1a2e'" onblur="this.style.borderColor='#e5e7eb'">
        </div>
        <div>
          <label style="display: block; font-size: 13px; color: #6b7280; margin-bottom: 6px;">确认新密码</label>
          <input type="password" id="confirm-password" placeholder="请再次输入新密码" style="width: 100%; padding: 10px 12px; border: 1px solid #e5e7eb; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box;" onfocus="this.style.borderColor='#1a1a2e'" onblur="this.style.borderColor='#e5e7eb'">
        </div>
      </div>
    `,
    dangerouslyUseHTMLString: true,
    showCancelButton: true,
    confirmButtonText: '确认修改',
    cancelButtonText: '取消',
    beforeClose: async (action, instance, done) => {
      if (action === 'confirm') {
        const oldPwd = document.getElementById('old-password')?.value
        const newPwd = document.getElementById('new-password')?.value
        const confirmPwd = document.getElementById('confirm-password')?.value
        
        if (!oldPwd) {
          ElMessage.warning('请输入原密码')
          return
        }
        if (!newPwd || newPwd.length < 6) {
          ElMessage.warning('新密码长度不能少于6位')
          return
        }
        if (newPwd !== confirmPwd) {
          ElMessage.warning('两次输入的密码不一致')
          return
        }
        
        instance.confirmButtonLoading = true
        try {
          const res = await userApi.changePassword(oldPwd, newPwd)
          if (res.code === 200) {
            ElMessage.success('密码修改成功')
            done()
          } else {
            ElMessage.error(res.message || '修改失败')
          }
        } catch (e) {
          ElMessage.error('修改失败')
        } finally {
          instance.confirmButtonLoading = false
        }
      } else {
        done()
      }
    }
  })
}

// 显示关于信息
const showAbout = () => {
  showUserMenu.value = false
  ElMessageBox.alert(
    `<div style="text-align: center;">
      <div style="font-size: 48px; margin-bottom: 16px;">💬</div>
      <h3 style="margin: 0 0 8px; color: #1a1a2e;">Xima 即时通讯</h3>
      <p style="color: #6b7280; margin: 0 0 16px;">版本 1.0.0</p>
      <p style="color: #9ca3af; font-size: 12px; margin: 0;">
        一款简洁、高效的即时通讯应用<br/>
        支持私聊、群聊、文件传输等功能
      </p>
    </div>`,
    '关于 Xima',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '知道了',
      center: true
    }
  )
}

// 从后端加载聊天背景设置
const loadChatBackground = async () => {
  try {
    const res = await userApi.getChatBackground()
    if (res.code === 200 && res.data) {
      // 同步到 localStorage
      localStorage.setItem('chatBackground', res.data)
      // 触发 storage 事件让其他页面更新
      window.dispatchEvent(new Event('storage'))
    }
  } catch (error) {
    console.error('加载聊天背景失败:', error)
  }
}

onMounted(() => {
  initNotificationSound()
  connectWebSocket()
  chatStore.loadFriendList()
  chatStore.loadGroupList()
  // 从后端加载聊天背景设置（跨浏览器同步）
  loadChatBackground()
  // 添加点击外部关闭菜单的监听
  document.addEventListener('click', handleClickOutside)
  // 请求浏览器通知权限
  requestNotificationPermission()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
  // 移除监听
  document.removeEventListener('click', handleClickOutside)
})

// 暴露给子组件使用
const sendMessage = (message) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify(message))
  }
}

// 提供给子组件
import { provide } from 'vue'
provide('sendMessage', sendMessage)
</script>

<style lang="scss" scoped>
.home-container {
  display: flex;
  height: 100vh;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
}

.sidebar {
  width: 72px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  z-index: 10;
}

.sidebar-logo {
  width: 48px;
  height: 48px;
  background: #1a1a2e;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  transition: all 0.2s ease;
  text-decoration: none;
  cursor: pointer;
  
  &:hover {
    background: #2d2d44;
    transform: scale(1.05);
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  span {
    font-size: 13px;
    font-weight: 700;
    color: #fff;
    letter-spacing: -0.5px;
  }
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding-top: 8px;
}

.nav-item {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  color: #9ca3af;
  text-decoration: none;
  transition: all 0.2s ease;
  
  .el-icon {
    font-size: 22px;
  }
  
  .nav-label {
    display: none;
  }
  
  &:hover {
    color: #1a1a2e;
    background: #f5f5f5;
  }
  
  &.active {
    color: #fff;
    background: #1a1a2e;
  }
  
  .badge {
    position: absolute;
    top: 4px;
    right: 4px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    background: #dc2626;
    border-radius: 9px;
    font-size: 10px;
    font-weight: 600;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.sidebar-footer {
  margin-top: auto;
  padding-bottom: 8px;
  position: relative;
  
  .user-avatar {
    position: relative;
    cursor: pointer;
    transition: transform 0.2s ease;
    
    :deep(.el-avatar) {
      border: 2px solid #fff;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
    
    &:hover {
      transform: scale(1.05);
    }
    
    .online-dot {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 10px;
      height: 10px;
      background: #059669;
      border: 2px solid #fff;
      border-radius: 50%;
    }
  }
}

.user-menu {
  position: absolute;
  bottom: 50px;
  left: 76px;
  width: 280px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  z-index: 100;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.user-menu-bg {
  height: 60px;
  background: #1a1a2e;
  position: relative;
  
  .bg-pattern {
    display: none;
  }
  
  .close-btn {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #fff;
    transition: all 0.2s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.2);
    }
    
    .el-icon {
      font-size: 14px;
    }
  }
}

.user-menu-header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 0 20px 16px;
  margin-top: -32px;
  position: relative;
  border-bottom: 1px solid #f0f0f0;
  
  .avatar-wrapper {
    position: relative;
    cursor: pointer;
    
    :deep(.el-avatar) {
      border: 3px solid #fff;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
    
    .status-badge {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 14px;
      height: 14px;
      border-radius: 50%;
      border: 2px solid #fff;
      
      &.online {
        background: #10b981;
      }
      
      &.offline {
        background: #9ca3af;
      }
    }
    
    .avatar-edit {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: #1a1a2e;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid #fff;
      
      .el-icon {
        font-size: 10px;
        color: #fff;
      }
    }
  }
}

.user-menu-info {
  flex: 1;
  overflow: hidden;
  padding-top: 36px;
}

.user-menu-name {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-menu-username {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 2px;
}

.user-menu-status-tag {
  position: absolute;
  top: 40px;
  right: 20px;
  font-size: 11px;
  color: #10b981;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #ecfdf5;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
  
  .status-dot {
    width: 6px;
    height: 6px;
    background: #10b981;
    border-radius: 50%;
  }
}

.user-menu-list {
  padding: 8px 16px 16px;
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 14px 0;
    cursor: pointer;
    transition: all 0.2s;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      .arrow {
        transform: translateX(3px);
        color: #1a1a2e;
      }
    }
    
    > .el-icon {
      font-size: 20px;
      color: #1a1a2e;
    }
    
    span {
      flex: 1;
      font-size: 15px;
      color: #1a1a2e;
      font-weight: 500;
    }
    
    .arrow {
      font-size: 14px;
      color: #c0c0c0;
      transition: all 0.2s;
    }
  }
}

.user-menu-footer {
  padding: 0 16px 16px;
  
  .logout-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px;
    border-radius: 8px;
    background: #1a1a2e;
    color: #fff;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #2d2d44;
    }
    
    .el-icon {
      font-size: 16px;
    }
    
    span {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-10px) scale(0.95);
}

// 移动端响应式样式
@media (max-width: 767px) {
  .sidebar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100% !important;
    height: 65px;
    flex-direction: row;
    justify-content: space-evenly;
    align-items: center;
    padding: 8px 0;
    padding-bottom: calc(8px + env(safe-area-inset-bottom, 0px));
    border-right: none;
    border-top: 1px solid #e5e7eb;
    z-index: 100;
  }
  
  .sidebar-logo {
    display: none;
  }
  
  .sidebar-nav {
    flex-direction: row;
    gap: 0;
    padding: 0;
    width: 100%;
    justify-content: space-evenly;
  }
  
  .nav-item {
    flex: 1;
    height: auto;
    flex-direction: column;
    padding: 4px 0;
    gap: 2px;
    
    .el-icon {
      font-size: 22px;
    }
    
    .nav-label {
      display: block !important;
      font-size: 10px;
    }
    
    .badge {
      top: -2px;
      right: 8px;
    }
  }
  
  // 移动端隐藏 sidebar-footer
  .sidebar-footer {
    display: none !important;
  }
  
  .user-menu {
    position: fixed;
    bottom: 70px;
    left: 50%;
    transform: translateX(-50%);
    
    &::before {
      display: none;
    }
  }
  
  .main-content {
    padding-bottom: 60px;
  }
}
</style>
