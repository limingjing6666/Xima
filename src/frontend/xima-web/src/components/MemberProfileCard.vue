<template>
  <el-dialog
    v-model="visible"
    :show-close="true"
    width="380px"
    class="member-profile-dialog"
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
          <el-avatar :size="90" :src="getAvatarUrl(member?.avatar || member?.senderAvatar)" class="profile-avatar">
            {{ (member?.nickname || member?.senderName)?.charAt(0) }}
          </el-avatar>
          <span class="status-indicator" :class="member?.status?.toLowerCase()"></span>
        </div>
      </div>
      
      <!-- 用户信息 -->
      <div class="card-body">
        <div class="name-with-role">
          <h3 class="profile-name">{{ member?.nickname || member?.senderName }}</h3>
          <span class="role-badge" :class="getRoleClass(member?.role)" v-if="member?.role && member?.role !== 'MEMBER'">
            {{ getRoleText(member?.role) }}
          </span>
        </div>
        <p class="profile-username">ID: {{ member?.username || member?.senderUsername }}</p>
        
        <!-- 用户信息列表 -->
        <div class="profile-info">
          <div class="info-row" v-if="member?.status">
            <div class="info-label">
              <el-icon><Connection /></el-icon>
              <span>状态</span>
            </div>
            <div class="info-value">
              <span class="status-tag" :class="member?.status?.toLowerCase()">
                {{ getStatusText(member?.status) }}
              </span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-label">
              <el-icon><User /></el-icon>
              <span>关系</span>
            </div>
            <div class="info-value">
              <span class="relation-tag" :class="isFriend ? 'friend' : 'stranger'">
                {{ isFriend ? '好友' : '非好友' }}
              </span>
            </div>
          </div>
          <!-- 禁言状态 -->
          <div class="info-row" v-if="member?.muted">
            <div class="info-label">
              <el-icon><MuteNotification /></el-icon>
              <span>禁言</span>
            </div>
            <div class="info-value">
              <span class="muted-tag">禁言中</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作列表 -->
      <div class="card-actions">
        <!-- 已是好友的操作 -->
        <template v-if="isFriend">
          <div class="action-item" @click="handleSendMessage">
            <div class="action-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <span>发送消息</span>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
          <div class="action-item" @click="handleSetRemark">
            <div class="action-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <span>设置备注</span>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
        </template>
        
        <!-- 非好友的操作 -->
        <template v-else>
          <div class="action-item primary" @click="handleAddFriend" :class="{ disabled: addingFriend }">
            <div class="action-icon primary">
              <el-icon><Plus /></el-icon>
            </div>
            <span>{{ addingFriend ? '发送中...' : '添加好友' }}</span>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
        </template>
        
        <!-- 群管理操作（仅群主/管理员可见） -->
        <template v-if="canManage">
          <div class="action-divider"></div>
          <div class="action-section-title">群管理</div>
          
          <!-- 群主专属操作 -->
          <template v-if="myRole === 'OWNER'">
            <div class="action-item" @click="handleSetAdmin" v-if="member?.role === 'MEMBER'">
              <div class="action-icon admin">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span>设为管理员</span>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="action-item" @click="handleRemoveAdmin" v-if="member?.role === 'ADMIN'">
              <div class="action-icon">
                <el-icon><User /></el-icon>
              </div>
              <span>取消管理员</span>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="action-item" @click="handleTransferOwner">
              <div class="action-icon owner">
                <el-icon><Key /></el-icon>
              </div>
              <span>转让群主</span>
              <el-icon class="action-arrow"><ArrowRight /></el-icon>
            </div>
          </template>
          
          <!-- 禁言操作 -->
          <div class="action-item" @click="handleToggleMute">
            <div class="action-icon" :class="member?.muted ? 'unmute' : 'mute'">
              <el-icon><Bell v-if="member?.muted" /><MuteNotification v-else /></el-icon>
            </div>
            <span>{{ member?.muted ? '解除禁言' : '禁言' }}</span>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
          
          <!-- 踢出群聊 -->
          <div class="action-item danger" @click="handleKickMember">
            <div class="action-icon danger">
              <el-icon><Delete /></el-icon>
            </div>
            <span>移出群聊</span>
            <el-icon class="action-arrow"><ArrowRight /></el-icon>
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useChatStore } from '@/stores/chat'
import { friendApi } from '@/api/friend'
import { groupApi } from '@/api/group'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toast } from '@/utils/toast'
import { Close, Connection, User, Edit, ArrowRight, ChatDotRound, Plus, UserFilled, Key, Bell, MuteNotification, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean,
  member: Object,
  groupId: Number,  // 群ID
  myRole: String    // 当前用户在群中的角色
})

const emit = defineEmits(['update:modelValue', 'sendMessage', 'remarkUpdated', 'memberUpdated'])

const chatStore = useChatStore()
const addingFriend = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 检查是否是好友
const isFriend = computed(() => {
  if (!props.member) return false
  const memberId = props.member.userId || props.member.senderId
  return chatStore.friendList.some(f => f.userId === memberId)
})

// 获取好友信息
const friendInfo = computed(() => {
  if (!props.member) return null
  const memberId = props.member.userId || props.member.senderId
  return chatStore.friendList.find(f => f.userId === memberId)
})

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
  return statusMap[status] || '未知'
}

// 发送消息
const handleSendMessage = () => {
  if (friendInfo.value) {
    chatStore.setCurrentChat(friendInfo.value)
    visible.value = false
    emit('sendMessage', friendInfo.value)
  }
}

// 设置备注
const handleSetRemark = async () => {
  if (!friendInfo.value) return
  
  try {
    const { value } = await ElMessageBox.prompt('请输入好友备注', '设置备注', {
      inputValue: friendInfo.value.remark || '',
      inputPlaceholder: '请输入备注名称',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const res = await friendApi.setFriendRemark(friendInfo.value.userId, value)
    if (res.code === 200) {
      toast.success('备注设置成功')
      await chatStore.loadFriendList()
      emit('remarkUpdated')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置备注失败:', error)
    }
  }
}

// 添加好友
const handleAddFriend = async () => {
  if (!props.member || addingFriend.value) return
  
  const memberId = props.member.userId || props.member.senderId
  
  try {
    addingFriend.value = true
    const res = await friendApi.sendFriendRequest(memberId)
    if (res.code === 200) {
      toast.success('好友请求已发送')
      visible.value = false
    } else {
      // 显示后端返回的具体错误信息
      toast.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('添加好友失败:', error)
    // 显示具体错误信息
    const errorMsg = error.response?.data?.message || error.message || '添加好友失败'
    toast.error(errorMsg)
  } finally {
    addingFriend.value = false
  }
}

// 是否可以管理该成员
const canManage = computed(() => {
  if (!props.groupId || !props.myRole) return false
  // 群主可以管理所有人
  if (props.myRole === 'OWNER') return true
  // 管理员只能管理普通成员
  if (props.myRole === 'ADMIN' && props.member?.role === 'MEMBER') return true
  return false
})

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    'OWNER': '群主',
    'ADMIN': '管理员',
    'MEMBER': '成员'
  }
  return roleMap[role] || '成员'
}

// 获取角色样式类
const getRoleClass = (role) => {
  return {
    'role-owner': role === 'OWNER',
    'role-admin': role === 'ADMIN'
  }
}

// 设为管理员
const handleSetAdmin = async () => {
  if (!props.groupId || !props.member) return
  try {
    const res = await groupApi.setAdmin(props.groupId, props.member.userId, true)
    if (res.code === 200) {
      toast.success('已设为管理员')
      emit('memberUpdated')
      visible.value = false
    } else {
      toast.error(res.message || '操作失败')
    }
  } catch (error) {
    toast.error('操作失败')
  }
}

// 取消管理员
const handleRemoveAdmin = async () => {
  if (!props.groupId || !props.member) return
  try {
    const res = await groupApi.setAdmin(props.groupId, props.member.userId, false)
    if (res.code === 200) {
      toast.success('已取消管理员')
      emit('memberUpdated')
      visible.value = false
    } else {
      toast.error(res.message || '操作失败')
    }
  } catch (error) {
    toast.error('操作失败')
  }
}

// 转让群主
const handleTransferOwner = async () => {
  if (!props.groupId || !props.member) return
  
  try {
    await ElMessageBox.confirm(
      `确定要将群主转让给 ${props.member.nickname || props.member.senderName} 吗？`,
      '转让群主',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const res = await groupApi.transferOwner(props.groupId, props.member.userId)
    if (res.code === 200) {
      toast.success('群主已转让')
      emit('memberUpdated')
      visible.value = false
      chatStore.loadGroupList()
    } else {
      toast.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      toast.error('操作失败')
    }
  }
}

// 禁言/解除禁言
const handleToggleMute = async () => {
  if (!props.groupId || !props.member) return
  const muted = !props.member.muted
  
  try {
    const res = await groupApi.setMuted(props.groupId, props.member.userId, muted)
    if (res.code === 200) {
      toast.success(muted ? '已禁言' : '已解除禁言')
      emit('memberUpdated')
      visible.value = false
    } else {
      toast.error(res.message || '操作失败')
    }
  } catch (error) {
    toast.error('操作失败')
  }
}

// 踢出群聊
const handleKickMember = async () => {
  if (!props.groupId || !props.member) return
  
  try {
    await ElMessageBox.confirm(
      `确定要将 ${props.member.nickname || props.member.senderName} 移出群聊吗？`,
      '移出群聊',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const res = await groupApi.removeMember(props.groupId, props.member.userId)
    if (res.code === 200) {
      toast.success('已移出群聊')
      emit('memberUpdated')
      visible.value = false
    } else {
      toast.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      toast.error('操作失败')
    }
  }
}
</script>

<style lang="scss">
.member-profile-dialog {
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
  background: #fff;
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
    color: #1a1a2e;
    margin: 0 0 4px;
    letter-spacing: 0.5px;
  }
  
  .profile-username {
    font-size: 13px;
    color: #9ca3af;
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
      color: #9ca3af;
      font-size: 13px;
      
      .el-icon {
        font-size: 16px;
      }
    }
    
    .info-value {
      font-size: 13px;
      color: #1a1a2e;
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
    
    .relation-tag {
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
      
      &.friend {
        background: rgba(5, 150, 105, 0.1);
        color: #059669;
      }
      
      &.stranger {
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
    
    &.primary {
      background: #1a1a2e;
      border-color: #1a1a2e;
      
      span {
        color: #fff;
      }
      
      .action-arrow {
        color: rgba(255, 255, 255, 0.6);
      }
      
      &:hover {
        background: #2d2d44;
      }
    }
    
    &.disabled {
      opacity: 0.6;
      cursor: not-allowed;
      
      &:hover {
        transform: none;
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
      
      &.primary {
        background: rgba(255, 255, 255, 0.2);
      }
    }
    
    span {
      flex: 1;
      font-size: 14px;
      font-weight: 500;
      color: #1a1a2e;
    }
    
    .action-arrow {
      color: #9ca3af;
      font-size: 14px;
    }
    
    &.danger {
      border-color: #fee2e2;
      background: #fef2f2;
      
      span {
        color: #dc2626;
      }
      
      &:hover {
        background: #fee2e2;
      }
    }
  }
  
  .action-divider {
    height: 1px;
    background: #e5e7eb;
    margin: 12px 0;
  }
  
  .action-section-title {
    font-size: 12px;
    color: #9ca3af;
    font-weight: 500;
    margin-bottom: 8px;
    padding-left: 4px;
  }
  
  .action-icon {
    &.admin {
      background: #2563eb;
    }
    
    &.owner {
      background: #d97706;
    }
    
    &.mute {
      background: #dc2626;
    }
    
    &.unmute {
      background: #059669;
    }
    
    &.danger {
      background: #dc2626;
    }
  }
}

.name-with-role {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 4px;
  
  .profile-name {
    margin: 0;
  }
  
  .role-badge {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 10px;
    font-weight: 500;
    
    &.role-owner {
      background: #fef3c7;
      color: #d97706;
    }
    
    &.role-admin {
      background: #dbeafe;
      color: #2563eb;
    }
  }
}

.muted-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #fee2e2;
  color: #dc2626;
}
</style>
