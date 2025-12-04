<template>
  <div class="register-container">
    <!-- 左侧插画区 -->
    <div class="register-left">
      <div class="illustration">
        <!-- 可爱的几何图形插画 - 不同布局 -->
        <div class="shape shape-blue"></div>
        <div class="shape shape-pink"></div>
        <div class="shape shape-green"></div>
        <div class="shape shape-yellow"></div>
      </div>
    </div>
    
    <!-- 右侧注册区 -->
    <div class="register-right">
      <div class="register-box">
        <!-- Logo -->
        <div class="logo">
          <span class="logo-text">Xima</span>
        </div>
        
        <div class="register-header">
          <h1>Create account</h1>
          <p>填写信息开始使用 Xima</p>
        </div>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          class="register-form"
          @submit.prevent="handleRegister"
        >
          <div class="form-group">
            <label>用户名</label>
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
            />
          </div>
          
          <div class="form-group">
            <label>邮箱</label>
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱地址"
              size="large"
            />
          </div>
          
          <div class="form-group">
            <label>验证码</label>
            <div class="code-input-wrapper">
              <el-input
                v-model="registerForm.code"
                placeholder="请输入6位验证码"
                size="large"
                maxlength="6"
              />
              <el-button
                type="primary"
                size="large"
                :loading="sendingCode"
                :disabled="countdown > 0"
                class="send-code-btn"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
              </el-button>
            </div>
          </div>
          
          <div class="form-group">
            <label>密码</label>
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请设置密码"
              size="large"
              show-password
            />
          </div>
          
          <div class="form-group">
            <label>确认密码</label>
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              show-password
              @keyup.enter="handleRegister"
            />
          </div>
          
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const registerForm = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})

// 发送验证码
const handleSendCode = async () => {
  // 先验证邮箱格式
  const qqEmailRegex = /^[1-9]\d{4,10}@qq\.com$/
  if (!registerForm.email) {
    ElMessage.error('请先输入邮箱地址')
    return
  }
  if (!qqEmailRegex.test(registerForm.email)) {
    ElMessage.error('只支持QQ邮箱')
    return
  }
  
  sendingCode.value = true
  try {
    const res = await authApi.sendCode(registerForm.email)
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请查收邮件')
      // 开始倒计时
      countdown.value = 60
      countdownTimer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(countdownTimer)
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// QQ邮箱格式验证
const validateQQEmail = (rule, value, callback) => {
  const qqEmailRegex = /^[1-9]\d{4,10}@qq\.com$/
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!qqEmailRegex.test(value)) {
    callback(new Error('只支持QQ邮箱'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { validator: validateQQEmail, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须是6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 注册（包含验证码）
        const registerResult = await userStore.register({
          username: registerForm.username,
          email: registerForm.email,
          code: registerForm.code,
          password: registerForm.password
        })
        if (registerResult.success) {
          // 注册成功后自动登录
          const loginResult = await userStore.login({
            username: registerForm.username,
            password: registerForm.password
          })
          if (loginResult.success) {
            router.push('/')
          } else {
            // 自动登录失败，跳转到登录页
            router.push('/login')
          }
        } else {
          ElMessage.error(registerResult.message || '注册失败')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  display: flex;
  background: #fff;
}

// 左侧插画区
.register-left {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.illustration {
  position: relative;
  width: 400px;
  height: 400px;
  
  .shape {
    position: absolute;
    transition: transform 0.3s ease;
  }
  
  // 主体圆形 - 蓝色
  .shape-blue {
    width: 180px;
    height: 180px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    top: 40px;
    left: 50%;
    transform: translateX(-50%);
    box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
    
    // 眼睛
    &::before, &::after {
      content: '';
      position: absolute;
      width: 12px;
      height: 12px;
      background: #fff;
      border-radius: 50%;
      top: 60px;
    }
    &::before { left: 55px; }
    &::after { right: 55px; }
  }
  
  // 粉色方块
  .shape-pink {
    width: 100px;
    height: 120px;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    border-radius: 16px;
    top: 100px;
    left: calc(50% - 160px);
    box-shadow: 0 15px 30px rgba(245, 87, 108, 0.3);
    
    // 眼睛
    &::before {
      content: '';
      position: absolute;
      width: 30px;
      height: 8px;
      background: #fff;
      top: 35px;
      left: 50%;
      transform: translateX(-50%);
    }
  }
  
  // 绿色三角形
  .shape-green {
    width: 0;
    height: 0;
    border-left: 60px solid transparent;
    border-right: 60px solid transparent;
    border-bottom: 100px solid #4facfe;
    bottom: 80px;
    left: calc(50% - 60px);
    filter: drop-shadow(0 15px 20px rgba(79, 172, 254, 0.3));
    
    &::before {
      content: '';
      position: absolute;
      width: 8px;
      height: 8px;
      background: #fff;
      border-radius: 50%;
      top: 50px;
      left: -4px;
    }
  }
  
  // 黄色椭圆
  .shape-yellow {
    width: 100px;
    height: 80px;
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    border-radius: 50%;
    bottom: 100px;
    right: calc(50% - 170px);
    box-shadow: 0 15px 30px rgba(253, 160, 133, 0.3);
    
    // 笑脸
    &::before {
      content: '';
      position: absolute;
      width: 30px;
      height: 15px;
      border: 3px solid #1a1a2e;
      border-top: none;
      border-radius: 0 0 30px 30px;
      top: 35px;
      left: 50%;
      transform: translateX(-50%);
    }
  }
}

// 右侧注册区
.register-right {
  width: 520px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 60px;
  padding-top: 80px;
  background: #fff;
  overflow-y: auto;
}

.register-box {
  width: 100%;
  max-width: 380px;
}

.logo {
  margin-bottom: 32px;
  
  .logo-text {
    font-size: 32px;
    font-weight: 800;
    color: #0a0a0a;
    letter-spacing: -1px;
  }
}

.register-header {
  margin-bottom: 32px;
  
  h1 {
    font-size: 32px;
    font-weight: 700;
    color: #0a0a0a;
    margin: 0 0 8px;
    letter-spacing: -0.5px;
  }
  
  p {
    color: #666;
    font-size: 14px;
    margin: 0;
  }
}

.register-form {
  .form-row {
    display: flex;
    gap: 16px;
    
    .form-group {
      flex: 1;
    }
  }
  
  .form-group {
    margin-bottom: 18px;
    
    label {
      display: block;
      font-size: 13px;
      font-weight: 600;
      color: #0a0a0a;
      margin-bottom: 8px;
      letter-spacing: 0.3px;
    }
    
    :deep(.el-input__wrapper) {
      padding: 0 16px;
      height: 48px;
      border-radius: 10px;
      box-shadow: none;
      border: 2px solid #e5e5e5;
      background: #fafafa;
      transition: all 0.2s;
      
      &:hover {
        border-color: #ccc;
        background: #fff;
      }
      
      &.is-focus {
        border-color: #0a0a0a;
        background: #fff;
        box-shadow: none;
      }
    }
    
    :deep(.el-input__inner) {
      font-size: 14px;
      color: #0a0a0a;
      
      &::placeholder {
        color: #999;
      }
    }
  }
  
  // 验证码输入框样式
  .code-input-wrapper {
    display: flex;
    gap: 12px;
    
    :deep(.el-input) {
      flex: 1;
    }
    
    .send-code-btn {
      height: 48px;
      padding: 0 20px;
      border-radius: 10px;
      font-size: 13px;
      font-weight: 600;
      background: #0a0a0a;
      border: none;
      color: #fff;
      white-space: nowrap;
      transition: all 0.2s;
      
      &:hover:not(:disabled) {
        background: #333;
      }
      
      &:disabled {
        background: #ccc;
        color: #666;
        cursor: not-allowed;
      }
    }
  }
}

.register-btn {
  width: 100%;
  height: 48px;
  margin-top: 12px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: #0a0a0a;
  border: none;
  color: #fff;
  transition: all 0.2s;
  letter-spacing: 2px;
  
  &:hover {
    background: #333;
  }
  
  &:active {
    transform: scale(0.98);
  }
}

.register-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #666;
  
  a {
    color: #0a0a0a;
    text-decoration: none;
    font-weight: 600;
    margin-left: 4px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 响应式
@media (max-width: 900px) {
  .register-left {
    display: none;
  }
  
  .register-right {
    width: 100%;
  }
}
</style>
