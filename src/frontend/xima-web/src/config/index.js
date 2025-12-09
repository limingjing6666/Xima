/**
 * 应用配置
 * 支持 Web 和 Capacitor (Android/iOS) 环境
 */

// 检测是否在 Capacitor 原生环境中运行
const isNative = () => {
  return window.Capacitor !== undefined && window.Capacitor.isNativePlatform()
}

// 默认服务器地址
const DEFAULT_SERVER = 'http://81.68.192.124:8080'

// 获取 API 基础地址
const getApiBaseUrl = () => {
  // 原生 App 环境：使用配置的服务器地址
  if (isNative()) {
    // 从本地存储获取服务器地址，如果没有则使用默认值
    const savedServer = localStorage.getItem('serverUrl')
    const baseUrl = savedServer || DEFAULT_SERVER
    return baseUrl + '/api'  // 添加 /api 后缀
  }
  
  // Web 环境：使用相对路径（通过 Vite 代理）
  return '/api'
}

// 获取 WebSocket 地址
const getWsBaseUrl = () => {
  if (isNative()) {
    const savedServer = localStorage.getItem('serverUrl')
    const serverUrl = savedServer || DEFAULT_SERVER
    // 将 http 转换为 ws
    return serverUrl.replace(/^http/, 'ws') + '/api/ws/chat'
  }
  
  // Web 环境
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/api/ws/chat`
}

// 获取资源完整 URL（图片、文件等）
const getResourceUrl = (path) => {
  if (!path) return ''
  
  // 如果已经是完整 URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  
  // 原生 App 环境：拼接服务器地址
  if (isNative()) {
    const savedServer = localStorage.getItem('serverUrl')
    const baseUrl = savedServer || DEFAULT_SERVER
    // 确保路径以 / 开头
    const normalizedPath = path.startsWith('/') ? path : '/' + path
    return baseUrl + normalizedPath
  }
  
  // Web 环境：返回相对路径
  return path
}

export default {
  isNative,
  getApiBaseUrl,
  getWsBaseUrl,
  getResourceUrl,
  
  // App 信息
  appName: 'Xima',
  appVersion: '1.0.0'
}
