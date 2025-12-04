<template>
  <div class="forgot-container">
    <!-- 左侧插画区 -->
    <div class="forgot-left">
      <div class="illustration">
        <div class="shape shape-circle"></div>
        <div class="shape shape-square"></div>
        <div class="shape shape-triangle"></div>
        <div class="shape shape-oval"></div>
      </div>
    </div>
    
    <!-- 右侧表单区 -->
    <div class="forgot-right">
      <div class="forgot-box">
        <!-- Logo -->
        <div class="logo">
          <span class="logo-text">Xima</span>
        </div>
        
        <div class="forgot-header">
          <h1>重置密码</h1>
          <p>输入您的邮箱地址，我们将发送验证码</p>
        </div>
        
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          class="forgot-form"
        >
          <!-- 步骤1: 输入邮箱 -->
          <template v-if="step === 1">
            <div class="form-group">
              <label>邮箱</label>
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱地址"
                size="large"
              />
            </div>
            
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="submit-btn"
              @click="handleSendCode"
            >
              {{ loading ? '发送中...' : '发送验证码' }}
            </el-button>
          </template>
          
          <!-- 步骤2: 输入验证码和新密码 -->
          <template v-if="step === 2">
            <div class="form-group">
              <label>验证码</label>
              <div class="code-input-wrapper">
                <el-input
                  v-model="form.code"
                  placeholder="请输入6位验证码"
                  size="large"
                  maxlength="6"
                />
                <el-button
                  class="resend-btn"
                  :disabled="countdown > 0"
                  @click="handleSendCode"
                >
                  {{ countdown > 0 ? `${countdown}s后重发` : '重新发送' }}
                </el-button>
              </div>
            </div>
            
            <div class="form-group">
              <label>新密码</label>
              <el-input
                v-model="form.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                size="large"
                show-password
              />
            </div>
            
            <div class="form-group">
              <label>确认密码</label>
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                size="large"
                show-password
              />
            </div>
            
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="submit-btn"
              @click="handleResetPassword"
            >
              {{ loading ? '重置中...' : '重置密码' }}
            </el-button>
          </template>
        </el-form>
        
        <div class="forgot-footer">
          <router-link to="/login">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const step = ref(1)
const countdown = ref(0)
let countdownTimer = null

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// QQ邮箱验证
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

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { validator: validateQQEmail, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须是6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 发送验证码
const handleSendCode = async () => {
  // 验证邮箱
  const qqEmailRegex = /^[1-9]\d{4,10}@qq\.com$/
  if (!form.email) {
    ElMessage.error('请先输入邮箱地址')
    return
  }
  if (!qqEmailRegex.test(form.email)) {
    ElMessage.error('只支持QQ邮箱')
    return
  }

  loading.value = true
  try {
    const res = await authApi.sendResetCode(form.email)
    if (res.code === 200) {
      ElMessage.success('验证码已发送，请查收邮件')
      step.value = 2
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
    ElMessage.error(error.message || '发送失败')
  } finally {
    loading.value = false
  }
}

// 重置密码
const handleResetPassword = async () => {
  if (!form.code || form.code.length !== 6) {
    ElMessage.error('请输入6位验证码')
    return
  }
  if (!form.newPassword || form.newPassword.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const res = await authApi.resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '重置失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.forgot-container {
  height: 100vh;
  display: flex;
  background: #fff;
}

// 左侧插画区
.forgot-left {
  flex: 1;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
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
  
  .shape-circle {
    width: 160px;
    height: 160px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    top: 50px;
    left: 50%;
    transform: translateX(-50%);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    
    // 锁图标
    &::before {
      content: '🔒';
      position: absolute;
      font-size: 60px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }
  
  .shape-square {
    width: 80px;
    height: 100px;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 12px;
    top: 120px;
    left: calc(50% - 150px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  }
  
  .shape-triangle {
    width: 0;
    height: 0;
    border-left: 50px solid transparent;
    border-right: 50px solid transparent;
    border-bottom: 80px solid rgba(255, 255, 255, 0.6);
    bottom: 100px;
    left: calc(50% - 50px);
    filter: drop-shadow(0 15px 20px rgba(0, 0, 0, 0.1));
  }
  
  .shape-oval {
    width: 80px;
    height: 60px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 50%;
    bottom: 120px;
    right: calc(50% - 150px);
    box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
  }
}

// 右侧表单区
.forgot-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 60px;
  background: #fff;
}

.forgot-box {
  width: 100%;
  max-width: 380px;
}

.logo {
  margin-bottom: 32px;
  
  .logo-text {
    font-size: 32px;
    font-weight: 800;
    color: #e74c3c;
    letter-spacing: -1px;
  }
}

.forgot-header {
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

.forgot-form {
  .form-group {
    margin-bottom: 20px;
    
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
        border-color: #e74c3c;
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
  
  .code-input-wrapper {
    display: flex;
    gap: 12px;
    
    :deep(.el-input) {
      flex: 1;
    }
    
    .resend-btn {
      height: 48px;
      padding: 0 20px;
      border-radius: 10px;
      font-size: 13px;
      font-weight: 600;
      background: #e74c3c;
      border: none;
      color: #fff;
      white-space: nowrap;
      transition: all 0.2s;
      
      &:hover:not(:disabled) {
        background: #c0392b;
      }
      
      &:disabled {
        background: #ccc;
        color: #666;
        cursor: not-allowed;
      }
    }
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 12px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: #e74c3c;
  border: none;
  color: #fff;
  transition: all 0.2s;
  letter-spacing: 2px;
  
  &:hover {
    background: #c0392b;
  }
  
  &:active {
    transform: scale(0.98);
  }
}

.forgot-footer {
  text-align: center;
  margin-top: 28px;
  
  a {
    color: #e74c3c;
    text-decoration: none;
    font-weight: 600;
    font-size: 14px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 响应式
@media (max-width: 900px) {
  .forgot-left {
    display: none;
  }
  
  .forgot-right {
    width: 100%;
  }
}
</style>
