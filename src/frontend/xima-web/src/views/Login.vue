<template>
  <div class="login-container">
    <!-- 左侧插画区 -->
    <div class="login-left">
      <div class="illustration">
        <!-- 可爱的几何图形插画 -->
        <div class="shape shape-purple"></div>
        <div class="shape shape-black"></div>
        <div class="shape shape-orange"></div>
        <div class="shape shape-yellow"></div>
      </div>
    </div>
    
    <!-- 右侧登录区 -->
    <div class="login-right">
      <div class="login-box">
        <!-- Logo -->
        <div class="logo">
          <span class="logo-text">Xima</span>
        </div>
        
        <div class="login-header">
          <h1>Welcome back!</h1>
          <p>请输入您的账号信息</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <div class="form-group">
            <label>用户名</label>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
            />
          </div>
          
          <div class="form-group">
            <label>密码</label>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </div>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <router-link to="/forgot-password" class="forgot-link">忘记密码？</router-link>
          </div>
          
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async (forceLogin = false) => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const result = await userStore.login({
          username: loginForm.username,
          password: loginForm.password,
          forceLogin: forceLogin === true
        })
        
        // 如果需要确认（账号已在其他设备登录）
        if (result.requireConfirm) {
          ElMessageBox.confirm(
            result.message || '检测到该账号已在其他设备登录，是否强制登录？',
            '登录确认',
            {
              confirmButtonText: '强制登录',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            // 用户确认强制登录
            handleLogin(true)
          }).catch(() => {
            // 用户取消
          })
          return
        }
        
        if (result.success) {
          router.push('/')
        } else {
          ElMessage.error(result.message || '登录失败')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  background: #fff;
}

// 左侧插画区
.login-left {
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
  
  // 主体方块 - 紫色渐变
  .shape-purple {
    width: 140px;
    height: 220px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    top: 50px;
    left: 50%;
    transform: translateX(-50%);
    box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
    
    // 眼睛
    &::before, &::after {
      content: '';
      position: absolute;
      width: 8px;
      height: 8px;
      background: #fff;
      border-radius: 50%;
      top: 30px;
    }
    &::before { left: 45px; }
    &::after { right: 45px; }
  }
  
  // 蓝色方块
  .shape-black {
    width: 80px;
    height: 140px;
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    border-radius: 12px;
    top: 130px;
    left: calc(50% + 60px);
    box-shadow: 0 15px 30px rgba(79, 172, 254, 0.3);
    
    // 眼睛
    &::before {
      content: '';
      position: absolute;
      width: 20px;
      height: 6px;
      background: #fff;
      top: 25px;
      left: 50%;
      transform: translateX(-50%);
    }
  }
  
  // 橙色半圆
  .shape-orange {
    width: 200px;
    height: 100px;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    border-radius: 100px 100px 0 0;
    bottom: 60px;
    left: calc(50% - 140px);
    box-shadow: 0 15px 30px rgba(245, 87, 108, 0.3);
    
    // 眼睛
    &::before, &::after {
      content: '';
      position: absolute;
      width: 10px;
      height: 10px;
      background: #fff;
      border-radius: 50%;
      top: 35px;
    }
    &::before { left: 60px; }
    &::after { left: 90px; }
    
    // 第三只眼
    &::after {
      left: 120px;
    }
  }
  
  // 黄色椭圆
  .shape-yellow {
    width: 90px;
    height: 70px;
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    border-radius: 50%;
    bottom: 80px;
    right: calc(50% - 160px);
    box-shadow: 0 15px 30px rgba(253, 160, 133, 0.3);
    
    // 嘴巴
    &::before {
      content: '';
      position: absolute;
      width: 25px;
      height: 3px;
      background: #1a1a2e;
      top: 30px;
      left: 50%;
      transform: translateX(-50%);
    }
  }
}

// 右侧登录区
.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 60px;
  background: #fff;
}

.login-box {
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

.login-header {
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

.login-form {
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
  
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    :deep(.el-checkbox__label) {
      font-size: 13px;
      color: #666;
    }
    
    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: #0a0a0a;
      border-color: #0a0a0a;
    }
    
    .forgot-link {
      font-size: 13px;
      color: #666;
      text-decoration: none;
      
      &:hover {
        color: #0a0a0a;
      }
    }
  }
}

.login-btn {
  width: 100%;
  height: 48px;
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

.login-footer {
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
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
}
</style>
