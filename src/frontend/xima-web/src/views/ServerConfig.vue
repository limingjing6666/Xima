<template>
  <div class="server-config">
    <div class="config-card">
      <div class="logo">
        <span class="logo-text">Xima</span>
        <span class="logo-subtitle">即时通讯</span>
      </div>
      
      <h2>服务器配置</h2>
      <p class="hint">请输入您的服务器地址</p>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="服务器地址" prop="serverUrl">
          <el-input 
            v-model="form.serverUrl" 
            placeholder="例如: http://192.168.1.100:8080"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="testing"
            @click="testConnection"
            style="width: 100%"
          >
            {{ testing ? '测试连接中...' : '测试并保存' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="tips">
        <p><el-icon><InfoFilled /></el-icon> 提示：</p>
        <ul>
          <li>确保手机和服务器在同一网络</li>
          <li>服务器地址格式：http://IP:端口</li>
          <li>默认端口为 8080</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link, InfoFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import config from '@/config'

const router = useRouter()
const formRef = ref(null)
const testing = ref(false)

const form = ref({
  serverUrl: ''
})

const rules = {
  serverUrl: [
    { required: true, message: '请输入服务器地址', trigger: 'blur' },
    { 
      pattern: /^https?:\/\/.+/, 
      message: '请输入有效的服务器地址（以 http:// 或 https:// 开头）', 
      trigger: 'blur' 
    }
  ]
}

onMounted(() => {
  // 如果不是原生环境，直接跳转到登录页
  if (!config.isNative()) {
    router.replace('/login')
    return
  }
  
  // 加载已保存的服务器地址，如果没有则使用默认值
  const savedUrl = localStorage.getItem('serverUrl')
  form.value.serverUrl = savedUrl || 'http://81.68.192.124:8080'
})

const testConnection = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  
  testing.value = true
  
  // 移除末尾的斜杠
  let serverUrl = form.value.serverUrl.replace(/\/+$/, '')
  
  try {
    // 测试连接
    const response = await axios.get(`${serverUrl}/api/v1/test/health`, {
      timeout: 5000
    })
    
    if (response.data.code === 200) {
      // 保存服务器地址
      localStorage.setItem('serverUrl', serverUrl)
      ElMessage.success('连接成功！')
      
      // 如果已登录，返回设置页；否则跳转到登录页
      setTimeout(() => {
        const token = localStorage.getItem('token')
        router.replace(token ? '/settings' : '/login')
      }, 500)
    } else {
      ElMessage.error('服务器响应异常')
    }
  } catch (error) {
    console.error('Connection test failed:', error)
    ElMessage.error('连接失败，请检查服务器地址是否正确')
  } finally {
    testing.value = false
  }
}
</script>

<style lang="scss" scoped>
.server-config {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 20px;
}

.config-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 20px;
  padding: 40px 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.logo {
  text-align: center;
  margin-bottom: 30px;
  
  .logo-text {
    display: block;
    font-size: 36px;
    font-weight: 700;
    color: #1a1a2e;
    letter-spacing: 2px;
  }
  
  .logo-subtitle {
    display: block;
    font-size: 14px;
    color: #9ca3af;
    margin-top: 4px;
  }
}

h2 {
  text-align: center;
  color: #1a1a2e;
  margin: 0 0 8px;
  font-size: 20px;
}

.hint {
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
  margin: 0 0 24px;
}

.tips {
  margin-top: 24px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  font-size: 13px;
  color: #64748b;
  
  p {
    margin: 0 0 8px;
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
    color: #475569;
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin: 4px 0;
    }
  }
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: 500;
}
</style>
