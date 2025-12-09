import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import config from '@/config'

const routes = [
  {
    path: '/server-config',
    name: 'ServerConfig',
    component: () => import('@/views/ServerConfig.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true },
    redirect: '/chat',
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/Chat.vue')
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('@/views/Contacts.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 原生 App 环境：服务器配置页面只能从设置页面进入
  if (config.isNative() && to.path === '/server-config') {
    // 只有从设置页面来的才允许访问
    if (from.path === '/settings') {
      next()
      return
    }
    // 其他情况：已登录去首页，未登录去登录页
    next(userStore.isLoggedIn ? '/' : '/login')
    return
  }
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else if (!to.meta.requiresAuth && userStore.isLoggedIn && (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password')) {
    next('/')
  } else {
    next()
  }
})

export default router
