<template>
  <div class="contacts-container">
    <!-- 页面头部 -->
    <div class="contacts-header">
      <div class="header-left">
        <h2>通讯录</h2>
        <span class="friend-count">{{ activeTab === 'friends' ? filteredFriendList.length + ' 位好友' : filteredGroupList.length + ' 个群聊' }}</span>
      </div>
      <div class="header-right">
        <div class="search-box">
          <el-icon class="search-icon"><Search /></el-icon>
          <input 
            v-model="filterKeyword" 
            type="text" 
            :placeholder="activeTab === 'friends' ? '搜索好友...' : '搜索群聊...'" 
            class="search-input"
          />
          <el-icon v-if="filterKeyword" class="clear-icon" @click="filterKeyword = ''"><Close /></el-icon>
        </div>
        <el-tooltip :content="activeTab === 'friends' ? '添加好友' : '创建群聊'" placement="bottom">
          <div class="add-friend-btn" @click="handleAddAction">
            <el-icon><Plus /></el-icon>
          </div>
        </el-tooltip>
      </div>
    </div>
    
    <!-- 分类标签 -->
    <div class="tabs-section">
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'friends' }"
        @click="activeTab = 'friends'"
      >
        <el-icon><User /></el-icon>
        <span>好友</span>
        <span class="tab-count">{{ friendList.length }}</span>
      </div>
      <div 
        class="tab-item" 
        :class="{ active: activeTab === 'groups' }"
        @click="activeTab = 'groups'"
      >
        <el-icon><ChatLineSquare /></el-icon>
        <span>群聊</span>
        <span class="tab-count">{{ groupList.length }}</span>
      </div>
    </div>
    
    <!-- 好友请求区域 -->
    <div class="requests-section" v-if="activeTab === 'friends' && friendRequests.length > 0">
      <div class="section-header">
        <el-icon><Bell /></el-icon>
        <span>好友请求</span>
        <el-badge :value="friendRequests.length" class="request-badge" />
      </div>
      <div class="requests-list">
        <div
          v-for="request in friendRequests"
          :key="request.requestId"
          class="request-card"
        >
          <el-avatar :size="48" :src="getAvatarUrl(request.fromAvatar)">
            {{ request.fromNickname?.charAt(0) || request.fromUsername?.charAt(0) }}
          </el-avatar>
          <div class="request-info">
            <div class="request-name">{{ request.fromNickname || request.fromUsername }}</div>
            <div class="request-desc">请求添加你为好友</div>
          </div>
          <div class="request-actions">
            <el-button type="primary" size="small" round @click="acceptRequest(request.requestId)">
              接受
            </el-button>
            <el-button size="small" round @click="rejectRequest(request.requestId)">
              忽略
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 好友列表区域 -->
    <div class="friends-section" v-if="activeTab === 'friends'">
      <div class="friends-grid" v-if="filteredFriendList.length > 0">
        <div
          v-for="friend in filteredFriendList"
          :key="friend.userId"
          class="friend-card"
          @click="selectContact(friend)"
        >
          <div class="friend-avatar-wrapper">
            <el-avatar 
              :size="56" 
              :src="getAvatarUrl(friend.avatar)"
              @click.stop="showFriendProfile(friend, $event)"
            >
              {{ friend.nickname?.charAt(0) || friend.username?.charAt(0) }}
            </el-avatar>
            <span class="status-dot" :class="friend.status?.toLowerCase()"></span>
          </div>
          <div class="friend-info">
            <div class="friend-name">{{ friend.nickname || friend.username }}</div>
            <div class="friend-status">{{ getStatusText(friend.status) }}</div>
          </div>
          <div class="friend-action">
            <el-icon><ChatDotRound /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div class="empty-state" v-else>
        <div class="empty-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
        <h3>还没有好友</h3>
        <p>点击右上角添加按钮，搜索并添加好友</p>
        <el-button type="primary" round @click="showAddFriend = true">
          <el-icon><Plus /></el-icon>
          添加好友
        </el-button>
      </div>
    </div>
    
    <!-- 群聊列表区域 -->
    <div class="friends-section" v-if="activeTab === 'groups'">
      <div class="friends-grid" v-if="filteredGroupList.length > 0">
        <div
          v-for="group in filteredGroupList"
          :key="group.id"
          class="friend-card group-card"
          @click="selectGroup(group)"
        >
          <div class="friend-avatar-wrapper">
            <!-- 群组合头像 -->
            <div class="group-avatar-grid" v-if="group.memberAvatars && group.memberAvatars.length > 0">
              <el-avatar 
                v-for="(avatar, idx) in group.memberAvatars.slice(0, 4)" 
                :key="idx"
                :size="27" 
                :src="getAvatarUrl(avatar)"
                class="grid-avatar"
              >
                {{ group.name?.charAt(0) }}
              </el-avatar>
            </div>
            <el-avatar 
              v-else
              :size="56" 
              :src="getAvatarUrl(group.avatar)"
              class="group-avatar"
            >
              {{ group.name?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="friend-info">
            <div class="friend-name">{{ group.name }}</div>
            <div class="friend-status">{{ group.memberCount }} 位成员</div>
          </div>
          <div class="friend-action">
            <el-icon><ChatDotRound /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 群聊空状态 -->
      <div class="empty-state" v-else>
        <div class="empty-icon group-empty-icon">
          <el-icon><ChatLineSquare /></el-icon>
        </div>
        <h3>还没有群聊</h3>
        <p>创建一个群聊，邀请好友一起聊天吧</p>
        <el-button type="primary" round @click="showCreateGroup = true">
          <el-icon><Plus /></el-icon>
          创建群聊
        </el-button>
      </div>
    </div>
    
    <!-- 添加好友对话框 -->
    <el-dialog v-model="showAddFriend" title="添加好友" width="420px" class="add-friend-dialog">
      <div class="search-area">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入用户名或昵称"
          size="large"
          clearable
          @keyup.enter="searchUsers"
        >
          <template #append>
            <el-button @click="searchUsers">查找</el-button>
          </template>
        </el-input>
      </div>
      
      <div class="result-area">
        <template v-if="searchResults.length > 0">
          <div
            v-for="user in searchResults"
            :key="user.id"
            class="user-card"
            :class="{ 'is-friend': isFriend(user.id) }"
          >
            <el-avatar :size="50" :src="user.avatar">
              {{ user.nickname?.charAt(0) || user.username?.charAt(0) }}
            </el-avatar>
            <div class="user-info">
              <div class="user-name">{{ user.nickname || user.username }}</div>
              <div class="user-account">账号：{{ user.username }}</div>
            </div>
            <el-button
              v-if="!isFriend(user.id)"
              type="primary"
              @click="sendFriendRequest(user.id)"
            >
              添加
            </el-button>
            <el-tag v-else type="info" effect="plain">已是好友</el-tag>
          </div>
        </template>
        
        <div v-else class="empty-area">
          <div class="empty-icon">
            <el-icon><Search /></el-icon>
          </div>
          <p v-if="searched">未找到相关用户</p>
          <p v-else>搜索用户名或昵称添加好友</p>
        </div>
      </div>
    </el-dialog>
    
    <!-- 好友信息卡片 -->
    <FriendProfileCard 
      v-model="showProfileCard" 
      :friend="profileFriend"
    />
    
    <!-- 创建群聊对话框 -->
    <CreateGroupDialog 
      v-model="showCreateGroup" 
      @created="handleGroupCreated"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { friendApi } from '@/api/friend'
import { userApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toast } from '@/utils/toast'
import { Plus, Bell, ChatDotRound, UserFilled, Search, Close, User, ChatLineSquare } from '@element-plus/icons-vue'
import FriendProfileCard from '@/components/FriendProfileCard.vue'
import CreateGroupDialog from '@/components/CreateGroupDialog.vue'

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()

const activeTab = ref('friends')
const showAddFriend = ref(false)
const showCreateGroup = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const friendRequests = ref([])
const showProfileCard = ref(false)
const profileFriend = ref(null)
const filterKeyword = ref('')

const friendList = computed(() => chatStore.friendList)
const groupList = computed(() => chatStore.groupList)

// 过滤好友列表
const filteredFriendList = computed(() => {
  if (!filterKeyword.value.trim()) {
    return friendList.value
  }
  const keyword = filterKeyword.value.toLowerCase().trim()
  return friendList.value.filter(friend => {
    const nickname = (friend.nickname || '').toLowerCase()
    const username = (friend.username || '').toLowerCase()
    const remark = (friend.remark || '').toLowerCase()
    return nickname.includes(keyword) || username.includes(keyword) || remark.includes(keyword)
  })
})

// 过滤群聊列表
const filteredGroupList = computed(() => {
  if (!filterKeyword.value.trim()) {
    return groupList.value
  }
  const keyword = filterKeyword.value.toLowerCase().trim()
  return groupList.value.filter(group => {
    const name = (group.name || '').toLowerCase()
    return name.includes(keyword)
  })
})

// 处理添加按钮点击
const handleAddAction = () => {
  if (activeTab.value === 'friends') {
    showAddFriend.value = true
  } else {
    showCreateGroup.value = true
  }
}

// 选择群聊
const selectGroup = (group) => {
  chatStore.setCurrentGroupChat(group)
  router.push('/chat')
}

// 群聊创建成功
const handleGroupCreated = (group) => {
  chatStore.setCurrentGroupChat(group)
  router.push('/chat')
}

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

const selectContact = (contact) => {
  // 直接跳转到聊天页面
  chatStore.setCurrentChat(contact)
  router.push('/chat')
}

// 显示好友信息卡片
const showFriendProfile = (friend, event) => {
  event?.stopPropagation()
  profileFriend.value = friend
  showProfileCard.value = true
}

const isFriend = (userId) => {
  return friendList.value.some(f => f.userId === userId)
}

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) return
  
  try {
    const res = await userApi.searchUsers(searchKeyword.value)
    if (res.code === 200) {
      // 过滤掉自己
      searchResults.value = (res.data || []).filter(
        u => u.id !== userStore.userInfo?.id
      )
      searched.value = true
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  }
}

const sendFriendRequest = async (friendId) => {
  try {
    const res = await friendApi.sendFriendRequest(friendId)
    if (res.code === 200) {
      toast.success('好友请求已发送')
      // 关闭弹窗并重置搜索
      showAddFriend.value = false
      searchKeyword.value = ''
      searchResults.value = []
      searched.value = false
    }
  } catch (error) {
    console.error('发送好友请求失败:', error)
  }
}

const loadFriendRequests = async () => {
  try {
    const res = await friendApi.getFriendRequests()
    if (res.code === 200) {
      friendRequests.value = res.data || []
    }
  } catch (error) {
    console.error('加载好友请求失败:', error)
  }
}

const acceptRequest = async (friendshipId) => {
  try {
    const res = await friendApi.acceptFriendRequest(friendshipId)
    if (res.code === 200) {
      toast.success('已接受好友请求')
      await loadFriendRequests()
      await chatStore.loadFriendList()
    }
  } catch (error) {
    console.error('接受好友请求失败:', error)
  }
}

const rejectRequest = async (friendshipId) => {
  try {
    const res = await friendApi.rejectFriendRequest(friendshipId)
    if (res.code === 200) {
      toast.success('已拒绝好友请求')
      await loadFriendRequests()
    }
  } catch (error) {
    console.error('拒绝好友请求失败:', error)
  }
}

onMounted(() => {
  loadFriendRequests()
})
</script>

<style lang="scss" scoped>
.contacts-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background: #fafafa;
  overflow: hidden;
}

.contacts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  background: #fff;
  
  .header-left {
    display: flex;
    align-items: baseline;
    gap: 12px;
    
    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #1a1a2e;
      margin: 0;
    }
    
    .friend-count {
      font-size: 14px;
      color: #9ca3af;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .search-box {
    position: relative;
    display: flex;
    align-items: center;
    
    .search-icon {
      position: absolute;
      left: 12px;
      color: #9ca3af;
      font-size: 16px;
    }
    
    .search-input {
      width: 200px;
      height: 36px;
      padding: 0 36px;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      background: #f5f5f5;
      color: #1a1a2e;
      font-size: 14px;
      outline: none;
      transition: all 0.2s ease;
      
      &::placeholder {
        color: #9ca3af;
      }
      
      &:focus {
        border-color: #1a1a2e;
        background: #fff;
      }
    }
    
    .clear-icon {
      position: absolute;
      right: 12px;
      color: #9ca3af;
      font-size: 14px;
      cursor: pointer;
      
      &:hover {
        color: #6b7280;
      }
    }
  }
  
  .add-friend-btn {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #fff;
    background: #1a1a2e;
    transition: all 0.2s ease;
    
    &:hover {
      background: #2d2d44;
    }
    
    &:active {
      transform: scale(0.95);
    }
    
    .el-icon {
      font-size: 20px;
    }
  }
}

// 分类标签
.tabs-section {
  display: flex;
  gap: 8px;
  padding: 0 24px 16px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  
  .tab-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: #6b7280;
    background: #f5f5f5;
    transition: all 0.2s ease;
    
    .el-icon {
      font-size: 16px;
    }
    
    .tab-count {
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 8px;
      background: rgba(0, 0, 0, 0.05);
    }
    
    &:hover {
      color: #1a1a2e;
      background: #e5e7eb;
    }
    
    &.active {
      color: #fff;
      background: #1a1a2e;
      
      .tab-count {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}

// 好友请求区域
.requests-section {
  padding: 16px 24px;
  background: #f5f5f5;
  border-bottom: 1px solid #e5e7eb;
  
  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    color: #1a1a2e;
    font-weight: 600;
    
    .el-icon {
      font-size: 18px;
    }
  }
  
  .requests-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .request-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background: #fff;
    border-radius: 10px;
    border: 1px solid #e5e7eb;
    
    .request-info {
      .request-name {
        font-weight: 500;
        color: #1a1a2e;
      }
      .request-desc {
        font-size: 12px;
        color: #9ca3af;
      }
    }
    
    .request-actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 好友列表区域
.friends-section {
  flex: 1;
  padding: 20px 24px;
  overflow-y: auto;
}

.friends-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.friend-card {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #e5e7eb;
  
  &:hover {
    background: #f5f5f5;
    
    .friend-action {
      opacity: 1;
      transform: translateX(0);
    }
  }
  
  .friend-avatar-wrapper {
    position: relative;
    
    :deep(.el-avatar) {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      cursor: pointer;
      transition: transform 0.2s;
      
      &:hover {
        transform: scale(1.05);
      }
    }
    
    .status-dot {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      border: 2px solid #fff;
      background: #9ca3af;
      
      &.online {
        background: #059669;
      }
      
      &.busy {
        background: #d97706;
      }
      
      &.away {
        background: #6b7280;
      }
    }
  }
  
  .friend-info {
    flex: 1;
    margin-left: 14px;
    
    .friend-name {
      font-size: 15px;
      font-weight: 600;
      color: #1a1a2e;
      margin-bottom: 2px;
    }
    
    .friend-status {
      font-size: 13px;
      color: #9ca3af;
    }
  }
  
  .friend-action {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: #1a1a2e;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transform: translateX(10px);
    transition: all 0.2s ease;
    
    .el-icon {
      font-size: 16px;
    }
  }
  
  // 群聊卡片样式
  &.group-card {
    .group-avatar {
      background: #1a1a2e;
    }
    
    // 群组合头像（2x2网格）
    .group-avatar-grid {
      width: 56px;
      height: 56px;
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      grid-template-rows: repeat(2, 1fr);
      gap: 1px;
      background: #e5e7eb;
      border-radius: 10px;
      overflow: hidden;
      
      .grid-avatar {
        width: 100% !important;
        height: 100% !important;
        border-radius: 0 !important;
      }
      
      :deep(.el-avatar) {
        border: none !important;
        box-shadow: none !important;
        
        &:hover {
          transform: none;
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  
  .empty-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
    
    .el-icon {
      font-size: 36px;
      color: #9ca3af;
    }
  }
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: #9ca3af;
    margin: 0 0 20px;
  }
  
  .el-button {
    padding: 10px 20px;
  }
  
  .group-empty-icon {
    background: #f5f5f5;
    
    .el-icon {
      color: #6b7280;
    }
  }
}

// 添加好友对话框
.add-friend-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    
    .el-dialog__header {
      padding: 18px 24px;
      border-bottom: 1px solid #eee;
      
      .el-dialog__title {
        font-size: 17px;
        font-weight: 600;
        color: #333;
      }
    }
    
    .el-dialog__body {
      padding: 20px 24px;
    }
  }
  
  .search-area {
    margin-bottom: 20px;
    
    :deep(.el-input-group__append) {
      background: var(--primary-color);
      border-color: var(--primary-color);
      
      .el-button {
        color: #fff;
        font-weight: 500;
      }
    }
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
    }
  }
  
  .result-area {
    min-height: 150px;
    max-height: 320px;
    overflow-y: auto;
  }
  
  .user-card {
    display: flex;
    align-items: center;
    padding: 14px 16px;
    margin-bottom: 10px;
    border: 1px solid #eee;
    border-radius: 10px;
    transition: all 0.2s;
    
    &:hover {
      border-color: var(--primary-color);
      background: rgba(124, 58, 237, 0.02);
    }
    
    &:last-child {
      margin-bottom: 0;
    }
    
    &.is-friend {
      background: #fafafa;
      
      &:hover {
        border-color: #eee;
        background: #fafafa;
      }
    }
    
    :deep(.el-avatar) {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-weight: 600;
      font-size: 18px;
    }
    
    .user-info {
      flex: 1;
      margin-left: 14px;
      
      .user-name {
        font-size: 15px;
        font-weight: 600;
        color: #333;
        margin-bottom: 4px;
      }
      
      .user-account {
        font-size: 13px;
        color: #888;
      }
    }
    
    .el-button {
      padding: 8px 20px;
      border-radius: 6px;
    }
    
    .el-tag {
      font-size: 12px;
    }
  }
  
  .empty-area {
    text-align: center;
    padding: 50px 20px;
    
    .empty-icon {
      width: 64px;
      height: 64px;
      margin: 0 auto 16px;
      background: #f5f5f5;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .el-icon {
        font-size: 28px;
        color: #ccc;
      }
    }
    
    p {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }
}

// 对话框通用样式
:deep(.el-dialog) {
  border-radius: 20px;
  
  .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid var(--border-light);
  }
  
  .el-dialog__body {
    padding: 24px;
  }
  
  .el-input__wrapper {
    border-radius: 12px;
  }
}

// 移动端响应式样式
@media (max-width: 767px) {
  .contacts-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 12px;
    padding: 16px !important;
    
    .header-left h2 {
      font-size: 20px;
    }
    
    .header-right {
      width: 100%;
      
      .search-box {
        flex: 1;
      }
    }
  }
  
  .tabs-section {
    padding: 0 16px 12px !important;
    
    .tab-item {
      padding: 6px 12px;
      font-size: 13px;
    }
  }
  
  .requests-section {
    padding: 12px 16px !important;
    
    .request-card {
      width: 100%;
    }
  }
  
  .friends-section {
    padding: 16px !important;
    
    .friends-grid {
      grid-template-columns: 1fr !important;
    }
    
    .friend-card {
      padding: 12px !important;
    }
  }
}
</style>
