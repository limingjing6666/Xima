<template>
  <div class="settings-container">
    <div class="settings-header">
      <el-icon v-if="isMobile" class="back-btn" @click="goBack"><ArrowLeft /></el-icon>
      <h2>个人设置</h2>
    </div>
    
    <div class="settings-main">
      <!-- 左侧设置面板 -->
      <div class="settings-panel">
        <!-- 头像设置 -->
        <div class="settings-section">
          <h3>头像</h3>
          <div class="avatar-setting">
            <div class="avatar-preview">
              <el-avatar :size="100" :src="displayAvatarUrl">
                {{ userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-overlay" @click="triggerUpload">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleFileChange"
            />
            <div class="avatar-tips">
              <p>支持 JPG、PNG 格式</p>
              <p>建议尺寸 200x200 像素</p>
            </div>
          </div>
        </div>
        
        <!-- 基本信息 -->
        <div class="settings-section">
          <h3>基本信息</h3>
          <el-form :model="profileForm" label-width="80px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 服务器设置（仅原生 App 显示） -->
        <div class="settings-section" v-if="isNativeApp">
          <h3>服务器设置</h3>
          <div class="server-setting">
            <p class="current-server">当前服务器：{{ currentServer }}</p>
            <el-button @click="goToServerConfig">
              切换服务器
            </el-button>
          </div>
        </div>
        
        <!-- 移动端功能菜单 -->
        <div class="settings-section mobile-menu" v-if="isMobile">
          <h3>更多设置</h3>
          <div class="menu-list">
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
          <div class="logout-section">
            <el-button type="danger" plain @click="handleLogout" class="logout-btn">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </div>
        
        <!-- 聊天背景 -->
        <div class="settings-section" v-if="!isNativeApp">
          <h3>聊天背景</h3>
          <div class="background-setting">
            <div class="background-options">
              <div
                v-for="(bg, index) in backgroundOptions"
                :key="index"
                class="background-item"
                :class="{ active: selectedBackground === bg.value }"
                :style="{ background: bg.preview }"
                @click="selectBackground(bg.value)"
              >
                <el-icon v-if="selectedBackground === bg.value" class="check-icon"><Check /></el-icon>
              </div>
            </div>
            <div class="custom-background">
              <el-button @click="triggerBgUpload" :loading="uploadingBg">
                <el-icon><Picture /></el-icon>
                自定义背景
              </el-button>
              <input
                ref="bgInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleBgChange"
              />
              <div class="bg-tips">
                <p>建议尺寸：1920×1080 像素</p>
                <p>支持 JPG、PNG 格式，最大 5MB</p>
              </div>
            </div>
          </div>
        </div>
        
      </div>
      
      <!-- 右侧预览面板（仅 Web 显示） -->
      <div class="preview-panel" v-if="!isNativeApp">
        <!-- 预览标题 -->
        <div class="preview-title">
          <el-icon><ChatDotRound /></el-icon>
          聊天效果预览
        </div>
        
        <!-- 聊天预览 -->
        <div class="chat-preview">
          <!-- 模拟聊天头部 -->
          <div class="preview-header">
            <el-avatar :size="40" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <div class="preview-header-info">
              <span class="preview-header-name">小助手</span>
              <span class="preview-header-status">
                <span class="status-dot"></span>
                在线
              </span>
            </div>
          </div>
          <!-- 模拟消息列表 -->
          <div class="preview-messages" :style="previewBackgroundStyle">
            <!-- 时间分割 -->
            <div class="preview-time">今天 12:30</div>
            <!-- 对方消息 -->
            <div class="preview-message other">
              <el-avatar :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <div class="preview-bubble other-bubble">你好！欢迎使用 Xima 聊天 👋</div>
            </div>
            <!-- 自己消息 -->
            <div class="preview-message self">
              <div class="preview-bubble self-bubble">你好！这是我的新头像和背景</div>
              <el-avatar :size="36" :src="displayAvatarUrl">
                {{ profileForm.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
            <!-- 对方消息 -->
            <div class="preview-message other">
              <el-avatar :size="36" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <div class="preview-bubble other-bubble">看起来很棒！主题颜色也很好看 ✨</div>
            </div>
            <!-- 自己消息 -->
            <div class="preview-message self">
              <div class="preview-bubble self-bubble">谢谢！我很喜欢这个效果 😊</div>
              <el-avatar :size="36" :src="displayAvatarUrl">
                {{ profileForm.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
          </div>
          <!-- 模拟输入框 -->
          <div class="preview-input">
            <el-icon class="preview-emoji"><ChatLineSquare /></el-icon>
            <div class="preview-input-box">输入消息...</div>
            <el-button type="primary" size="small" round>发送</el-button>
          </div>
        </div>
        
        <!-- 底部提示 -->
        <div class="preview-tips">
          <el-icon><InfoFilled /></el-icon>
          <span>修改设置后可在此实时预览效果</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toast } from '@/utils/toast'
import { Camera, Check, Picture, View, User, ChatDotRound, ChatLineSquare, InfoFilled, ArrowLeft, Bell, Lock, ArrowRight, SwitchButton } from '@element-plus/icons-vue'
import config from '@/config'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 是否移动端
const isMobile = ref(window.innerWidth <= 767)

// 返回上一页
const goBack = () => {
  router.back()
}

// 是否原生 App
const isNativeApp = config.isNative()

// 当前服务器地址
const currentServer = computed(() => {
  return localStorage.getItem('serverUrl') || 'http://81.68.192.124:8080'
})

// 跳转到服务器配置页
const goToServerConfig = () => {
  router.push('/server-config')
}

// 通知设置
const notificationEnabled = ref(localStorage.getItem('notificationEnabled') !== 'false')
const soundEnabled = ref(localStorage.getItem('soundEnabled') !== 'false')

const showNotificationSettings = () => {
  ElMessageBox({
    title: '通知设置',
    message: `
      <div style="padding: 8px 0;">
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background: #f8f9fa; border-radius: 8px; margin-bottom: 8px;">
          <span>消息通知</span>
          <input type="checkbox" id="notif-toggle" ${notificationEnabled.value ? 'checked' : ''} style="width: 18px; height: 18px;">
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background: #f8f9fa; border-radius: 8px;">
          <span>提示音</span>
          <input type="checkbox" id="sound-toggle" ${soundEnabled.value ? 'checked' : ''} style="width: 18px; height: 18px;">
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

// 隐私与安全设置
const showSecuritySettings = () => {
  ElMessageBox({
    title: '隐私与安全',
    message: `
      <div style="padding: 8px 0;">
        <div style="padding: 16px; background: #f8f9fa; border-radius: 10px; margin-bottom: 16px;">
          <div style="font-weight: 600; color: #1a1a2e; margin-bottom: 12px; font-size: 14px;">账号信息</div>
          <div style="font-size: 13px; color: #6b7280;">
            <div style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #e5e7eb;">
              <span style="color: #9ca3af;">用户名</span>
              <span style="color: #1a1a2e; font-weight: 500;">${userInfo.value?.username || '-'}</span>
            </div>
            <div style="display: flex; justify-content: space-between; padding: 8px 0;">
              <span style="color: #9ca3af;">邮箱</span>
              <span style="color: #1a1a2e; font-weight: 500;">${userInfo.value?.email || '未绑定'}</span>
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

// 关于 Xima
const showAbout = () => {
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

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}

// 处理头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http') || avatar.startsWith('data:')) {
    return avatar
  }
  const path = avatar.startsWith('/') ? avatar : '/' + avatar
  if (config.isNative()) {
    return config.getResourceUrl(path)
  }
  return path
}

// 计算头像显示URL
const displayAvatarUrl = computed(() => {
  if (avatarPreview.value) return avatarPreview.value
  return getAvatarUrl(userInfo.value?.avatar)
})

const fileInputRef = ref(null)
const bgInputRef = ref(null)
const avatarPreview = ref('')
const saving = ref(false)
const uploadingBg = ref(false)

const profileForm = reactive({
  username: '',
  nickname: '',
  email: ''
})

// 背景选项 - 只保留黑白两种
const backgroundOptions = [
  { value: 'solid-light', preview: '#ffffff' },
  { value: 'solid-dark', preview: '#1e1e2e' }
]

const selectedBackground = ref(localStorage.getItem('chatBackground') || 'solid-light')

// 预览背景计算属性
const previewBackground = computed(() => {
  const bg = selectedBackground.value
  const backgrounds = {
    'solid-light': '#ffffff',
    'solid-dark': '#1e1e2e'
  }
  // 如果是自定义背景图片URL
  if (bg.startsWith('url(')) {
    return bg
  }
  return backgrounds[bg] || bg
})

// 预览背景样式
const previewBackgroundStyle = computed(() => {
  const bg = selectedBackground.value
  // 如果是自定义背景图片
  if (bg.startsWith('url(')) {
    return {
      backgroundImage: bg,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    }
  }
  // 预设纯色背景
  const backgrounds = {
    'solid-light': '#ffffff',
    'solid-dark': '#1e1e2e'
  }
  return {
    background: backgrounds[bg] || '#fafafa'
  }
})

// 初始化表单
onMounted(() => {
  if (userInfo.value) {
    profileForm.username = userInfo.value.username || ''
    profileForm.nickname = userInfo.value.nickname || ''
    profileForm.email = userInfo.value.email || ''
  }
})

// 触发头像上传
const triggerUpload = () => {
  fileInputRef.value?.click()
}

// 处理头像文件选择
const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 验证文件大小 (最大 2MB)
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  
  try {
    // 上传到服务器
    const res = await userApi.uploadAvatar(file)
    if (res.code === 200) {
      const avatarUrl = res.data
      avatarPreview.value = avatarUrl
      // 更新 userStore
      if (userStore.userInfo) {
        userStore.userInfo.avatar = avatarUrl
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      }
      toast.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  }
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  
  saving.value = true
  try {
    const res = await userApi.updateCurrentUser({
      nickname: profileForm.nickname,
      email: profileForm.email
    })
    
    if (res.code === 200) {
      // 更新本地存储
      if (userStore.userInfo) {
        userStore.userInfo.nickname = profileForm.nickname
        userStore.userInfo.email = profileForm.email
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      }
      toast.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 触发背景上传
const triggerBgUpload = () => {
  bgInputRef.value?.click()
}

// 检查图片尺寸
const checkImageSize = (file) => {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(img.src)
      resolve({ width: img.width, height: img.height })
    }
    img.onerror = () => {
      resolve(null)
    }
    img.src = URL.createObjectURL(file)
  })
}

// 处理背景图片选择
const handleBgChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  
  // 检查图片尺寸
  const size = await checkImageSize(file)
  if (size) {
    const { width, height } = size
    // 建议最小尺寸
    if (width < 800 || height < 600) {
      ElMessage.warning(`图片尺寸较小 (${width}×${height})，建议使用 1920×1080 以上的图片以获得更好的显示效果`)
    }
    // 检查宽高比
    const ratio = width / height
    if (ratio < 1) {
      ElMessage.warning('建议使用横向图片作为聊天背景')
    }
  }
  
  uploadingBg.value = true
  try {
    // 上传到服务器
    const res = await userApi.uploadBackground(file)
    if (res.code === 200) {
      const bgUrl = res.data
      // 处理URL，确保正确格式
      const fullUrl = bgUrl.startsWith('http') || bgUrl.startsWith('/api') 
        ? bgUrl 
        : (bgUrl.startsWith('/') ? `/api${bgUrl}` : `/api/${bgUrl}`)
      const bgValue = `url(${fullUrl})`
      
      // 保存到后端数据库（跨浏览器同步）
      await userApi.updateChatBackground(bgValue)
      
      localStorage.setItem('chatBackground', bgValue)
      selectedBackground.value = bgValue
      // 触发 storage 事件让其他页面更新
      window.dispatchEvent(new Event('storage'))
      toast.success('背景上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('背景上传失败')
  } finally {
    uploadingBg.value = false
    // 清空input，允许重复选择同一文件
    e.target.value = ''
  }
}

// 选择预设背景
const selectBackground = async (value) => {
  selectedBackground.value = value
  localStorage.setItem('chatBackground', value)
  
  // 保存到后端数据库（跨浏览器同步）
  try {
    await userApi.updateChatBackground(value)
  } catch (error) {
    console.error('保存背景设置失败:', error)
  }
  
  // 触发 storage 事件让其他页面更新
  window.dispatchEvent(new Event('storage'))
  toast.success('背景设置成功')
}
</script>

<style lang="scss" scoped>
.settings-container {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
  overflow: hidden;
}

.settings-header {
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 12px;
  
  .back-btn {
    font-size: 22px;
    cursor: pointer;
    color: #1a1a2e;
    
    &:active {
      opacity: 0.7;
    }
  }
  
  h2 {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0;
  }
}

.settings-main {
  display: flex;
  gap: 20px;
  padding: 20px;
  flex: 1;
  overflow: hidden;
}

.settings-panel {
  width: 360px;
  min-width: 360px;
  overflow-y: auto;
  padding-right: 12px;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #e5e7eb;
    border-radius: 3px;
  }
}

.preview-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.theme-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.preview-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
  font-size: 12px;
  color: #9ca3af;
  
  .el-icon {
    color: #1a1a2e;
  }
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #059669;
  border-radius: 50%;
  display: inline-block;
  margin-right: 4px;
}

.preview-title {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 8px;
  
  .el-icon {
    color: #1a1a2e;
  }
}

.chat-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid #e5e7eb;
}

.preview-header-info {
  display: flex;
  flex-direction: column;
}

.preview-header-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.preview-header-status {
  font-size: 12px;
  color: #059669;
}

.preview-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  background: #fafafa;
  background-size: cover;
  background-position: center;
}

.preview-time {
  text-align: center;
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 16px;
  padding: 4px 12px;
  background: rgba(255,255,255,0.8);
  border-radius: 10px;
  display: inline-block;
  margin-left: auto;
  margin-right: auto;
}

.preview-message {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 12px;
  
  &.self {
    flex-direction: row-reverse;
  }
}

.preview-bubble {
  max-width: 65%;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.5;
  
  &.other-bubble {
    background: #fff;
    color: #1a1a2e;
    border-radius: 4px 14px 14px 14px;
    border: 1px solid #e5e7eb;
  }
  
  &.self-bubble {
    background: #1a1a2e;
    color: #fff;
    border-radius: 14px 4px 14px 14px;
  }
}

.preview-input {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e5e7eb;
}

.preview-emoji {
  font-size: 18px;
  color: #9ca3af;
  cursor: pointer;
  
  &:hover {
    color: #1a1a2e;
  }
}

.preview-input-box {
  flex: 1;
  padding: 10px 14px;
  background: #f5f5f5;
  border-radius: 10px;
  font-size: 13px;
  color: #9ca3af;
}

.settings-section {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid #e5e7eb;
  
  h3 {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 16px 0;
    padding-bottom: 10px;
    border-bottom: 1px solid #e5e7eb;
  }
}

.avatar-setting {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  position: relative;
  cursor: pointer;
  
  :deep(.el-avatar) {
    border: 3px solid #e5e7eb;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
  
  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(26, 26, 46, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
  
  .el-icon {
    font-size: 22px;
    margin-bottom: 4px;
  }
  
  span {
    font-size: 12px;
  }
}

.avatar-tips {
  color: #9ca3af;
  font-size: 13px;
  
  p {
    margin: 4px 0;
  }
}

.profile-form {
  max-width: 100%;
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-form-item__label) {
    color: #6b7280;
    font-weight: 500;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: none;
    border: 1px solid #e5e7eb;
    padding: 8px 12px;
    
    &:hover {
      border-color: #d1d5db;
    }
    
    &.is-focus {
      border-color: #1a1a2e;
      box-shadow: none;
    }
    
    &.is-disabled {
      background: #f9fafb;
      border-color: #e5e7eb;
    }
  }
  
  :deep(.el-input__inner) {
    color: #1a1a2e;
    
    &::placeholder {
      color: #9ca3af;
    }
  }
  
  :deep(.el-input__count-inner) {
    color: #9ca3af;
    font-size: 12px;
  }
  
  .el-button {
    border-radius: 8px;
    padding: 10px 28px;
    background: #1a1a2e;
    border: none;
    font-weight: 500;
    
    &:hover {
      background: #2d2d44;
    }
  }
}

.background-setting {
  .background-options {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    margin-bottom: 16px;
  }
  
  .background-item {
    aspect-ratio: 16 / 10;
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    border: 2px solid #e5e7eb;
    transition: all 0.2s;
    
    &:hover {
      border-color: #9ca3af;
    }
    
    &.active {
      border-color: #1a1a2e;
    }
    
    .check-icon {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 20px;
      color: #fff;
      background: #1a1a2e;
      border-radius: 50%;
      padding: 4px;
    }
  }
  
  .custom-background {
    .el-button {
      border-radius: 8px;
      border: 1px solid #e5e7eb;
      color: #1a1a2e;
      
      &:hover {
        background: #f5f5f5;
        border-color: #d1d5db;
      }
    }
  }
  
  .bg-tips {
    margin-top: 12px;
    color: #9ca3af;
    font-size: 12px;
    
    p {
      margin: 4px 0;
    }
  }
}

.theme-setting {
  .theme-options {
    display: flex;
    gap: 10px;
  }
  
  .theme-item {
    width: 44px;
    height: 44px;
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    border: 2px solid transparent;
    transition: all 0.2s;
    
    &:hover {
      transform: scale(1.05);
    }
    
    &.active {
      border-color: #1a1a2e;
    }
    
    .check-icon {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 18px;
      color: #fff;
    }
  }
}

// 移动端响应式样式
@media (max-width: 767px) {
  .settings-container {
    padding-bottom: 80px;
    background: #f5f5f5;
  }
  
  .settings-header {
    padding: 16px 20px !important;
    background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
    color: #fff;
    border-bottom: none !important;
    
    .back-btn {
      color: #fff !important;
    }
    
    h2 {
      font-size: 18px;
      color: #fff;
    }
  }
  
  .settings-main {
    flex-direction: column !important;
    padding: 0 !important;
    gap: 0 !important;
    background: #f5f5f5;
  }
  
  .settings-panel {
    width: 100% !important;
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }
  
  .settings-preview {
    display: none !important;
  }
  
  .settings-section {
    padding: 20px !important;
    border-radius: 16px !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04) !important;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #1a1a2e;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      gap: 8px;
      
      &::before {
        content: '';
        width: 4px;
        height: 16px;
        background: linear-gradient(135deg, #1a1a2e 0%, #4a4a5a 100%);
        border-radius: 2px;
      }
    }
  }
  
  .avatar-setting {
    flex-direction: column;
    align-items: center;
    text-align: center;
    
    .avatar-preview {
      width: 100px;
      height: 100px;
      
      :deep(.el-avatar) {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
    }
    
    .avatar-tips {
      margin-top: 12px;
      
      p {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }
  
  .profile-form {
    :deep(.el-form-item) {
      margin-bottom: 20px;
    }
    
    :deep(.el-form-item__label) {
      width: 70px !important;
      font-weight: 500;
      color: #6b7280;
    }
    
    :deep(.el-input__wrapper) {
      border-radius: 10px;
      padding: 8px 12px;
    }
    
    :deep(.el-button) {
      width: 100%;
      height: 44px;
      border-radius: 12px;
      font-size: 15px;
      font-weight: 500;
    }
  }
  
  .bg-options {
    grid-template-columns: repeat(3, 1fr) !important;
  }
}

// 服务器设置样式
.server-setting {
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .current-server {
    color: var(--text-secondary);
    font-size: 14px;
    word-break: break-all;
  }
}

// 移动端菜单样式
.mobile-menu {
  h3 {
    margin-bottom: 12px !important;
  }
  
  .menu-list {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
  
  .menu-item {
    display: flex;
    align-items: center;
    padding: 18px 16px;
    cursor: pointer;
    transition: all 0.2s;
    border-bottom: 1px solid #f3f4f6;
    background: #fff;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:active {
      background: #f8f9fa;
      transform: scale(0.99);
    }
    
    .el-icon {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: #fff;
      margin-right: 14px;
      border-radius: 10px;
      background: linear-gradient(135deg, #1a1a2e 0%, #3d3d54 100%);
    }
    
    span {
      flex: 1;
      font-size: 15px;
      font-weight: 500;
      color: #1a1a2e;
    }
    
    .arrow {
      width: auto;
      height: auto;
      font-size: 16px;
      color: #c4c4c4;
      background: none;
      margin-right: 0;
    }
  }
  
  .logout-section {
    margin-top: 24px;
    
    .logout-btn {
      width: 100%;
      height: 50px;
      font-size: 16px;
      font-weight: 500;
      border-radius: 14px;
      border: none;
      background: #fff;
      color: #dc2626;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      
      &:hover, &:active {
        background: #fef2f2;
        color: #dc2626;
      }
      
      .el-icon {
        margin-right: 8px;
      }
    }
  }
}
</style>
