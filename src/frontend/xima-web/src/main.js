import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './styles/global.scss'
import config from './config'

const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 初始化主题颜色
const initTheme = () => {
  const theme = localStorage.getItem('themeColor') || 'purple'
  const themeColors = {
    purple: { primary: '#6366f1', light: '#818cf8', dark: '#4f46e5' },
    blue: { primary: '#3b82f6', light: '#60a5fa', dark: '#2563eb' },
    green: { primary: '#22c55e', light: '#4ade80', dark: '#16a34a' },
    orange: { primary: '#f59e0b', light: '#fbbf24', dark: '#d97706' },
    pink: { primary: '#ec4899', light: '#f472b6', dark: '#db2777' },
    red: { primary: '#ef4444', light: '#f87171', dark: '#dc2626' }
  }
  const colors = themeColors[theme]
  if (colors) {
    document.documentElement.style.setProperty('--primary-color', colors.primary)
    document.documentElement.style.setProperty('--primary-light', colors.light)
    document.documentElement.style.setProperty('--primary-dark', colors.dark)
    document.documentElement.style.setProperty('--primary-gradient', 
      `linear-gradient(135deg, ${colors.primary} 0%, ${colors.light} 100%)`)
  }
}
initTheme()

// 检测原生 App 环境，添加特殊样式类
if (config.isNative()) {
  document.body.classList.add('is-native-app')
  
  // 禁用双击缩放
  document.addEventListener('touchstart', (e) => {
    if (e.touches.length > 1) {
      e.preventDefault()
    }
  }, { passive: false })
  
  // 禁用双指缩放
  document.addEventListener('gesturestart', (e) => {
    e.preventDefault()
  })
}

app.mount('#app')
