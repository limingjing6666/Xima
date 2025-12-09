import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import appConfig from '@/config'

// 创建axios实例
const request = axios.create({
  timeout: 15000
})

// 请求拦截器 - 动态设置 baseURL
request.interceptors.request.use(
  (config) => {
    // 动态获取 baseURL（每次请求时重新获取，确保使用最新配置）
    config.baseURL = appConfig.getApiBaseUrl()
    
    // 调试日志
    console.log('[Request]', {
      isNative: appConfig.isNative(),
      baseURL: config.baseURL,
      url: config.url,
      fullURL: config.baseURL + config.url
    })
    
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 业务错误处理
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // Token过期或无效
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  (error) => {
    // HTTP错误处理
    let message = '网络错误'
    
    // 调试：显示实际请求的 URL
    const requestUrl = error.config?.baseURL + error.config?.url
    console.error('[Request Error]', requestUrl, error.message)
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else {
      // 网络错误时显示请求的 URL
      message = `网络错误: ${requestUrl}`
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
