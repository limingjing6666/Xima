import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 登录
  async function login(loginData) {
    try {
      const res = await authApi.login(loginData)
      if (res.code === 200) {
        // 检查是否需要确认（账号已在其他设备登录）
        if (res.data.requireConfirm) {
          return { 
            success: false, 
            requireConfirm: true, 
            message: res.data.message 
          }
        }
        
        token.value = res.data.token
        userInfo.value = {
          id: res.data.id,
          username: res.data.username,
          nickname: res.data.nickname,
          email: res.data.email,
          avatar: res.data.avatar,
          roles: res.data.roles
        }
        localStorage.setItem('token', token.value)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        localStorage.removeItem('chatBackground')  // 清理旧背景，让Home页面从后端重新加载
        return { success: true }
      }
      return { success: false, message: res.message }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  // 注册
  async function register(registerData) {
    try {
      const res = await authApi.register(registerData)
      if (res.code === 200) {
        return { success: true }
      }
      return { success: false, message: res.message }
    } catch (error) {
      return { success: false, message: error.message || '注册失败' }
    }
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('chatBackground')  // 清理聊天背景设置
    router.push('/login')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    register,
    logout
  }
})
