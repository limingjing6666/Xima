<template>
  <el-dialog
    v-model="visible"
    :show-close="true"
    width="380px"
    class="friend-profile-dialog"
    :modal="true"
    destroy-on-close
  >
    <div class="profile-card">
      <!-- 关闭按钮 -->
      <div class="close-btn" @click="visible = false">
        <el-icon><Close /></el-icon>
      </div>
      
      <!-- 头部背景 -->
      <div class="card-header">
        <div class="header-bg">
          <div class="header-pattern"></div>
        </div>
        <div class="avatar-wrapper">
          <el-avatar :size="90" :src="getAvatarUrl(friend?.avatar)" class="profile-avatar">
            {{ friend?.nickname?.charAt(0) || friend?.username?.charAt(0) }}
          </el-avatar>
          <span class="status-indicator" :class="friend?.status?.toLowerCase()"></span>
        </div>
      </div>
      
      <!-- 用户信息 -->
      <div class="card-body">
        <h3 class="profile-name">{{ friend?.nickname || friend?.username }}</h3>
        <p class="profile-username">ID: {{ friend?.username }}</p>
        
        <!-- 用户信息列表 -->
        <div class="profile-info">
          <div class="info-row">
            <div class="info-label">
              <el-icon><Connection /></el-icon>
              <span>状态</span>
            </div>
            <div class="info-value">
              <span class="status-tag" :class="friend?.status?.toLowerCase()">
                {{ getStatusText(friend?.status) }}
              </span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-label">
              <el-icon><Calendar /></el-icon>
              <span>添加时间</span>
            </div>
            <div class="info-value">{{ formatDate(friend?.createTime) }}</div>
          </div>
          <div class="info-row" v-if="friend?.email">
            <div class="info-label">
              <el-icon><Message /></el-icon>
              <span>邮箱</span>
            </div>
            <div class="info-value">{{ friend?.email }}</div>
          </div>
        </div>
      </div>
      
      <!-- 操作列表 -->
      <div class="card-actions">
        <div class="action-item" @click="handleSetRemark">
          <div class="action-icon">
            <el-icon><Edit /></el-icon>
          </div>
          <span>设置备注</span>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="action-item danger" @click="handleDeleteFriend">
          <div class="action-icon danger">
            <el-icon><Delete /></el-icon>
          </div>
          <span>删除好友</span>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { friendApi } from '@/api/friend'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toast } from '@/utils/toast'
import { Delete, Message, Calendar, Edit, ArrowRight, Close, Connection } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const props = defineProps({
  modelValue: Boolean,
  friend: Object
})

const emit = defineEmits(['update:modelValue', 'deleted', 'updated'])

const chatStore = useChatStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 处理头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http') || avatar.startsWith('/api')) {
    return avatar
  }
  return avatar.startsWith('/') ? `/api${avatar}` : `/api/${avatar}`
}

const getStatusText = (status) => {
  const statusMap = {
    'ONLINE': '在线',
    'OFFLINE': '离线',
    'BUSY': '忙碌',
    'AWAY': '离开'
  }
  return statusMap[status] || '离线'
}

const formatDate = (date) => {
  if (!date) return '未知'
  return dayjs(date).format('YYYY-MM-DD')
}

// 设置备注
const handleSetRemark = async () => {
  if (!props.friend) return
  
  try {
    const { value } = await ElMessageBox.prompt('请输入备注名称', '设置备注', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: props.friend.remark || '',
      inputPlaceholder: '输入备注名称'
    })
    
    if (value !== undefined) {
      const res = await friendApi.setFriendRemark(props.friend.userId, value)
      if (res.code === 200) {
        toast.success('备注设置成功')
        // 刷新好友列表
        await chatStore.loadFriendList()
        emit('updated')
        visible.value = false
      }
    }
  } catch (error) {
    // 用户取消
  }
}

// 删除好友
const handleDeleteFriend = async () => {
  if (!props.friend) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除好友「${props.friend.nickname || props.friend.username}」吗？\n删除后将清除聊天记录`,
      '删除好友',
      { 
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const res = await friendApi.deleteFriend(props.friend.userId)
    if (res.code === 200) {
      toast.success('已删除好友')
      visible.value = false
      emit('deleted', props.friend.userId)
      await chatStore.loadFriendList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除好友失败:', error)
    }
  }
}
</script>

<style lang="scss">
.friend-profile-dialog {
  .el-dialog {
    border-radius: 20px;
    overflow: hidden;
  }
  
  .el-dialog__header {
    display: none;
  }
  
  .el-dialog__body {
    padding: 0;
  }
}
</style>

<style lang="scss" scoped>
.profile-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  background: var(--bg-primary);
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  color: white;
  transition: all 0.2s;
  
  &:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(1.1);
  }
}

.card-header {
  position: relative;
  height: 120px;
  
  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100%;
    background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
    overflow: hidden;
    
    .header-pattern {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-image: radial-gradient(circle at 20% 80%, rgba(255,255,255,0.05) 0%, transparent 50%),
                        radial-gradient(circle at 80% 20%, rgba(255,255,255,0.08) 0%, transparent 50%);
    }
  }
  
  .avatar-wrapper {
    position: absolute;
    bottom: -45px;
    left: 50%;
    transform: translateX(-50%);
    
    .profile-avatar {
      border: 4px solid #fff;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }
    
    .status-indicator {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      border: 3px solid #fff;
      
      &.online {
        background: #059669;
      }
      
      &.offline {
        background: #9ca3af;
      }
    }
  }
}

.card-body {
  padding: 55px 24px 16px;
  text-align: center;
  
  .profile-name {
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 4px;
    letter-spacing: 0.5px;
  }
  
  .profile-username {
    font-size: 13px;
    color: var(--text-muted);
    margin: 0 0 16px;
    font-family: 'SF Mono', Monaco, monospace;
  }
  
  .profile-info {
    background: #f5f5f5;
    border-radius: 14px;
    padding: 4px 0;
    border: 1px solid #e5e7eb;
    
    .info-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px;
      
      &:not(:last-child) {
        border-bottom: 1px solid #e5e7eb;
      }
    }
    
    .info-label {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--text-muted);
      font-size: 13px;
      
      .el-icon {
        font-size: 16px;
      }
    }
    
    .info-value {
      font-size: 13px;
      color: var(--text-primary);
      font-weight: 500;
    }
    
    .status-tag {
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      
      &.online {
        background: rgba(5, 150, 105, 0.1);
        color: #059669;
      }
      
      &.offline {
        background: rgba(156, 163, 175, 0.1);
        color: #9ca3af;
      }
    }
  }
}

.card-actions {
  padding: 8px 16px 20px;
  
  .action-item {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    margin-bottom: 8px;
    background: #f5f5f5;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #e5e7eb;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &:hover {
      background: #e5e7eb;
      transform: translateX(4px);
    }
    
    &.danger {
      &:hover {
        background: rgba(239, 68, 68, 0.08);
      }
      
      span {
        color: #ef4444;
      }
    }
    
    .action-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: #1a1a2e;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      
      .el-icon {
        color: white;
        font-size: 18px;
      }
      
      &.danger {
        background: rgba(239, 68, 68, 0.1);
        
        .el-icon {
          color: #ef4444;
        }
      }
    }
    
    span {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
    }
    
    .action-arrow {
      color: var(--text-muted);
      font-size: 14px;
    }
  }
}
</style>
