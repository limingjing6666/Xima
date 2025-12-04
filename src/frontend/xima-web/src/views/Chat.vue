<template>
  <div class="chat-container" :class="{ 'mobile-chat-open': isMobile && currentChat }">
    <!-- 会话列表 -->
    <div class="conversation-list" :class="{ 'mobile-hidden': isMobile && currentChat }">
      <div class="search-box">
        <div class="search-input-container">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话"
            prefix-icon="Search"
            clearable
          />
        </div>
        <el-tooltip content="搜索聊天记录" placement="bottom">
          <div class="search-msg-btn" @click="showMessageSearch = true">
            <div class="btn-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
                <path d="M11 8v6M8 11h6"></path>
              </svg>
            </div>
            <span class="btn-text">记录</span>
          </div>
        </el-tooltip>
      </div>
      
      <!-- 群聊列表 -->
      <div class="section-title" v-if="filteredGroups.length > 0">
        <el-icon><ChatLineSquare /></el-icon>
        <span>群聊</span>
      </div>
      <div class="conversations" v-if="filteredGroups.length > 0">
        <div
          v-for="group in filteredGroups"
          :key="'g_' + group.id"
          class="conversation-item"
          :class="{ active: currentChat?.type === 'group' && currentChat?.id === group.id }"
          @click="selectGroupChat(group)"
        >
          <!-- 群组合头像 -->
          <div class="group-avatar-grid" v-if="group.memberAvatars && group.memberAvatars.length > 0">
            <el-avatar 
              v-for="(avatar, idx) in group.memberAvatars.slice(0, 4)" 
              :key="idx"
              :size="22" 
              :src="getAvatarUrl(avatar)"
              class="grid-avatar"
            >
              {{ group.name?.charAt(0) }}
            </el-avatar>
          </div>
          <el-avatar v-else :size="45" :src="getAvatarUrl(group.avatar)" class="group-avatar">
            {{ group.name?.charAt(0) }}
          </el-avatar>
          <div class="conversation-info">
            <div class="conversation-name">{{ group.name }}</div>
            <div class="conversation-last">{{ group.memberCount }}人</div>
          </div>
          <div class="conversation-meta">
            <el-badge
              v-if="groupUnreadMap[group.id] > 0"
              :value="groupUnreadMap[group.id]"
              :max="99"
              class="unread-badge"
            />
          </div>
        </div>
      </div>
      
      <!-- 好友列表 -->
      <div class="section-title" v-if="filteredFriends.length > 0">
        <el-icon><User /></el-icon>
        <span>好友</span>
      </div>
      <div class="conversations">
        <div
          v-for="friend in filteredFriends"
          :key="'f_' + friend.userId"
          class="conversation-item"
          :class="{ active: currentChat?.type === 'friend' && currentChat?.userId === friend.userId }"
          @click="selectChat(friend)"
        >
          <el-avatar 
            :size="45" 
            :src="getAvatarUrl(friend.avatar)"
            class="clickable-avatar"
            @click.stop="showFriendProfile(friend, $event)"
          >
            {{ friend.nickname?.charAt(0) || friend.username?.charAt(0) }}
          </el-avatar>
          <div class="conversation-info">
            <div class="conversation-name">{{ friend.nickname || friend.username }}</div>
            <div class="conversation-last">{{ getLastMessage(friend.userId) }}</div>
          </div>
          <div class="conversation-meta">
            <div class="conversation-time">{{ getLastTime(friend.userId) }}</div>
            <el-badge
              v-if="unreadMap[friend.userId] > 0"
              :value="unreadMap[friend.userId]"
              :max="99"
              class="unread-badge"
            />
          </div>
        </div>
        
        <el-empty v-if="filteredFriends.length === 0 && filteredGroups.length === 0" description="暂无会话" />
      </div>
    </div>
    
    <!-- 聊天区域 -->
    <div class="chat-area" v-if="currentChat">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <el-icon v-if="isMobile" class="back-btn" @click="goBack"><ArrowLeft /></el-icon>
        <!-- 好友聊天头部 -->
        <template v-if="currentChat.type === 'friend'">
          <span class="chat-title">{{ getChatTitle() }}</span>
          <span class="chat-status" :class="currentChat.status === 'ONLINE' ? 'online' : 'offline'">
            <span class="status-dot"></span>
            {{ currentChat.status === 'ONLINE' ? '在线' : '离线' }}
          </span>
        </template>
        <!-- 群聊头部 -->
        <template v-else>
          <div class="group-header-avatar" @click="showGroupInfo = true">
            <!-- 群组合头像 -->
            <div class="header-avatar-grid" v-if="currentChat.memberAvatars && currentChat.memberAvatars.length > 0">
              <el-avatar 
                v-for="(avatar, idx) in currentChat.memberAvatars.slice(0, 4)" 
                :key="idx"
                :size="16" 
                :src="getAvatarUrl(avatar)"
                class="grid-avatar"
              >
                {{ currentChat.name?.charAt(0) }}
              </el-avatar>
            </div>
            <el-avatar v-else :size="36" :src="getAvatarUrl(currentChat.avatar)">
              {{ currentChat.name?.charAt(0) }}
            </el-avatar>
          </div>
          <span class="chat-title clickable" @click="showGroupInfo = true">{{ getChatTitle() }}</span>
          <span class="chat-status group-member-count">{{ currentChat.memberCount }}人</span>
        </template>
      </div>
      
      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef" :style="chatBackgroundStyle">
        <div
          v-for="(msg, index) in currentMessages"
          :key="index"
          class="message-item"
          :class="{ 'message-self': msg.senderId === userInfo?.id }"
        >
          <!-- 系统消息 -->
          <div v-if="msg.contentType === 'SYSTEM' || msg.recalled" class="system-message">
            {{ msg.recalled ? (msg.senderId === userInfo?.id ? '你撤回了一条消息' : msg.content) : msg.content }}
          </div>
          <template v-else-if="!msg.recalled">
            <!-- 对方头像 -->
            <el-avatar 
              :size="36" 
              :src="getMessageAvatar(msg)" 
              v-if="msg.senderId !== userInfo?.id"
              class="clickable-avatar"
              @click="currentChat.type === 'group' ? showMemberProfile(msg, $event) : showFriendProfile(currentChat, $event)"
            >
              {{ getMessageSenderName(msg)?.charAt(0) }}
            </el-avatar>
            <div class="message-content">
              <!-- 群聊显示发送者名称（优先显示好友备注） -->
              <div v-if="currentChat.type === 'group' && msg.senderId !== userInfo?.id" class="message-sender">
                {{ msg.senderRemark || msg.senderName }}
              </div>
              <!-- 文本消息 -->
              <div v-if="!msg.contentType || msg.contentType === 'TEXT'" 
                   class="message-bubble"
                   @contextmenu.prevent="showMessageMenu($event, msg)">
                {{ msg.content }}
              </div>
              <!-- 图片消息 -->
              <div v-else-if="msg.contentType === 'IMAGE'" 
                   class="message-image"
                   @contextmenu.prevent="showMessageMenu($event, msg)">
                <el-image 
                  :src="msg.content" 
                  :preview-src-list="[msg.content]"
                  fit="cover"
                  :lazy="true"
                />
              </div>
              <!-- 文件消息 -->
              <div v-else-if="msg.contentType === 'FILE'" 
                   class="message-file" 
                   @click="downloadFile(msg.content)"
                   @contextmenu.prevent="showMessageMenu($event, msg)">
                <el-icon class="file-icon"><Folder /></el-icon>
                <div class="file-info">
                  <span class="file-name">{{ getFileName(msg.content) }}</span>
                  <span class="file-size">{{ getFileSize(msg.content) }}</span>
                </div>
                <el-icon class="download-icon"><Download /></el-icon>
              </div>
              <div class="message-time">{{ formatTime(msg.timestamp || msg.createTime) }}</div>
            </div>
            <!-- 自己头像 -->
            <el-avatar :size="36" :src="getAvatarUrl(userInfo?.avatar)" v-if="msg.senderId === userInfo?.id">
              {{ userInfo?.nickname?.charAt(0) }}
            </el-avatar>
          </template>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-toolbar">
          <div class="toolbar-left">
            <!-- 表情选择器 -->
            <el-popover
              placement="top-start"
              :width="340"
              trigger="click"
              :show-arrow="true"
            >
              <template #reference>
                <div class="toolbar-btn" title="表情">😊</div>
              </template>
              <div class="emoji-picker">
                <div class="emoji-title">选择表情</div>
                <div class="emoji-grid">
                  <span 
                    v-for="emoji in emojiList" 
                    :key="emoji" 
                    class="emoji-item"
                    @click="insertEmoji(emoji)"
                  >{{ emoji }}</span>
                </div>
              </div>
            </el-popover>
            
            <!-- 图片上传 -->
            <el-tooltip content="发送图片" placement="top">
              <div class="toolbar-btn" @click="triggerImageUpload">
                <el-icon><Picture /></el-icon>
              </div>
            </el-tooltip>
            <input
              ref="imageInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload"
            />
            
            <!-- 文件上传 -->
            <el-tooltip content="发送文件" placement="top">
              <div class="toolbar-btn" @click="triggerFileUpload">
                <el-icon><Folder /></el-icon>
              </div>
            </el-tooltip>
            <input
              ref="fileInputRef"
              type="file"
              style="display: none"
              @change="handleFileUpload"
            />
          </div>
        </div>
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入消息，按 Enter 发送..."
            resize="none"
            @keydown.enter.exact.prevent="sendMessage"
          />
          <el-button 
            class="send-btn" 
            type="primary" 
            :disabled="!inputMessage.trim()"
            @click="sendMessage"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div class="chat-empty" v-else>
      <el-empty description="选择一个会话开始聊天" />
    </div>
    
    <!-- 好友信息卡片 -->
    <FriendProfileCard 
      v-model="showProfileCard" 
      :friend="selectedFriend"
    />
    
    <!-- 群成员信息卡片 -->
    <MemberProfileCard 
      v-model="showMemberCard" 
      :member="selectedMember"
      :groupId="currentChat?.type === 'group' ? currentChat?.id : null"
      :myRole="currentChat?.type === 'group' ? currentChat?.myRole : null"
      @memberUpdated="handleMemberUpdated"
    />
    
    <!-- 群信息弹窗 -->
    <el-drawer
      v-model="showGroupInfo"
      :with-header="false"
      direction="rtl"
      size="360px"
      class="group-info-drawer"
    >
      <div class="group-info-content" v-if="currentChat?.type === 'group'">
        <!-- 顶部背景和头像 -->
        <div class="drawer-header">
          <div class="close-btn" @click="showGroupInfo = false">
            <el-icon><Close /></el-icon>
          </div>
          <div class="group-avatar-wrapper">
            <div class="group-avatar-grid-large" v-if="currentChat.memberAvatars?.length > 0">
              <el-avatar 
                v-for="(avatar, idx) in currentChat.memberAvatars.slice(0, 4)" 
                :key="idx"
                :size="38" 
                :src="getAvatarUrl(avatar)"
              />
            </div>
            <el-avatar v-else :size="80" :src="getAvatarUrl(currentChat.avatar)" class="single-avatar">
              {{ currentChat.name?.charAt(0) }}
            </el-avatar>
          </div>
        </div>
        
        <!-- 群基本信息 -->
        <div class="group-basic-info">
          <h3 class="group-name">{{ currentChat.name }}</h3>
          <p class="group-desc">{{ currentChat.description || '暂无群简介' }}</p>
          <div class="group-stats">
            <div class="stat-item">
              <span class="stat-value">{{ groupMembers.length }}</span>
              <span class="stat-label">成员</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">{{ currentChat.myRole === 'OWNER' ? '群主' : currentChat.myRole === 'ADMIN' ? '管理员' : '成员' }}</span>
              <span class="stat-label">我的身份</span>
            </div>
          </div>
        </div>
        
        <!-- 群主卡片 -->
        <div class="info-card owner-card">
          <div class="card-header">
            <el-icon class="card-icon"><User /></el-icon>
            <span>群主</span>
          </div>
          <div class="owner-info" @click="handleOwnerClick">
            <el-avatar :size="44" :src="getAvatarUrl(currentChat.ownerAvatar)">
              {{ currentChat.ownerName?.charAt(0) }}
            </el-avatar>
            <div class="owner-detail">
              <span class="owner-name">{{ currentChat.ownerName }}</span>
              <span class="owner-tag">
                <el-icon><Key /></el-icon>
                群主
              </span>
            </div>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 群成员卡片 -->
        <div class="info-card members-card">
          <div class="card-header">
            <el-icon class="card-icon"><UserFilled /></el-icon>
            <span>群成员</span>
            <span class="member-count">{{ groupMembers.length }}人</span>
            <el-button type="primary" size="small" class="invite-btn" @click="showInviteDialog = true">
              <el-icon><Plus /></el-icon>
              邀请
            </el-button>
          </div>
          <div class="members-grid">
            <div 
              v-for="member in groupMembers.slice(0, 8)" 
              :key="member.userId" 
              class="member-avatar-item"
              @click="handleMemberClick(member)"
            >
              <el-avatar :size="40" :src="getAvatarUrl(member.avatar)">
                {{ member.nickname?.charAt(0) }}
              </el-avatar>
              <span class="member-role-dot" :class="getRoleClass(member.role)" v-if="member.role !== 'MEMBER'"></span>
            </div>
            <div class="member-avatar-item more-btn" v-if="groupMembers.length > 8" @click="showAllMembers = true">
              <div class="more-icon">
                <el-icon><MoreFilled /></el-icon>
              </div>
            </div>
          </div>
          <!-- 展开的成员列表 -->
          <div class="members-list-expanded" v-if="showAllMembers || groupMembers.length <= 8">
            <div 
              v-for="member in groupMembers" 
              :key="member.userId" 
              class="member-list-item"
              @click="handleMemberClick(member)"
            >
              <el-avatar :size="36" :src="getAvatarUrl(member.avatar)">
                {{ member.nickname?.charAt(0) }}
              </el-avatar>
              <span class="member-name">{{ member.nickname }}</span>
              <span class="member-role-tag" :class="getRoleClass(member.role)" v-if="member.role !== 'MEMBER'">
                {{ getRoleText(member.role) }}
              </span>
              <span class="online-dot" :class="member.status?.toLowerCase()"></span>
            </div>
          </div>
          <div class="toggle-members" v-if="groupMembers.length > 8" @click="showAllMembers = !showAllMembers">
            {{ showAllMembers ? '收起' : '查看全部成员' }}
            <el-icon><ArrowDown v-if="!showAllMembers" /><ArrowUp v-else /></el-icon>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <div class="action-btn danger" v-if="currentChat.myRole === 'OWNER'" @click="handleDissolveGroup">
            <el-icon><Delete /></el-icon>
            <span>解散群聊</span>
          </div>
          <div class="action-btn warning" v-else @click="handleLeaveGroup">
            <el-icon><SwitchButton /></el-icon>
            <span>退出群聊</span>
          </div>
        </div>
      </div>
    </el-drawer>
    
    <!-- 邀请好友加入群聊弹窗 -->
    <el-dialog
      v-model="showInviteDialog"
      title="邀请好友加入群聊"
      width="420px"
      :close-on-click-modal="false"
      class="invite-dialog"
    >
      <div class="invite-content">
        <el-input
          v-model="inviteSearchKeyword"
          placeholder="搜索好友"
          prefix-icon="Search"
          clearable
          class="invite-search"
        />
        <div class="friend-select-list">
          <el-checkbox-group v-model="selectedFriendIds">
            <div 
              v-for="friend in invitableFriends" 
              :key="friend.userId" 
              class="friend-select-item"
            >
              <el-checkbox :label="friend.userId" :disabled="isMemberInGroup(friend.userId)">
                <div class="friend-info">
                  <el-avatar :size="36" :src="getAvatarUrl(friend.avatar)">
                    {{ friend.nickname?.charAt(0) }}
                  </el-avatar>
                  <span class="friend-name">{{ friend.remark || friend.nickname || friend.username }}</span>
                  <span class="already-member" v-if="isMemberInGroup(friend.userId)">已在群中</span>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <el-empty v-if="invitableFriends.length === 0" description="暂无可邀请的好友" :image-size="60" />
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showInviteDialog = false">取消</el-button>
          <el-button type="primary" @click="handleInviteMembers" :loading="inviteLoading" :disabled="selectedFriendIds.length === 0">
            邀请 {{ selectedFriendIds.length > 0 ? `(${selectedFriendIds.length})` : '' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 消息右键菜单 -->
    <Transition name="context-menu">
      <div 
        v-if="messageMenuVisible" 
        class="message-context-menu"
        :style="{ left: messageMenuPosition.x + 'px', top: messageMenuPosition.y + 'px' }"
      >
        <div class="menu-header">
          <span class="menu-title">消息操作</span>
        </div>
        <div class="menu-body">
          <div v-if="selectedMessage?.contentType !== 'FILE'" class="menu-item" @click="copyMessage">
            <div class="menu-icon-wrapper">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="9" y="9" width="13" height="13" rx="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
            </div>
            <div class="menu-text">
              <span class="menu-label">{{ selectedMessage?.contentType === 'IMAGE' ? '复制链接' : '复制文本' }}</span>
              <span class="menu-hint">复制到剪贴板</span>
            </div>
          </div>
          <div 
            v-if="canRecallMessage(selectedMessage)" 
            class="menu-item warning" 
            @click="recallMessage"
          >
            <div class="menu-icon-wrapper warning">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"></path>
                <path d="M3 3v5h5"></path>
              </svg>
            </div>
            <div class="menu-text">
              <span class="menu-label">撤回消息</span>
              <span class="menu-hint">2分钟内可撤回</span>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 消息搜索对话框 -->
    <el-dialog
      v-model="showMessageSearch"
      title=""
      width="480px"
      :show-close="false"
      class="message-search-dialog"
      destroy-on-close
    >
      <div class="search-dialog-container">
        <!-- 顶部区域 -->
        <div class="search-dialog-top">
          <div class="search-header">
            <h3>🔍 搜索消息</h3>
            <button class="close-btn" @click="showMessageSearch = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          <div class="search-input-box">
            <input
              v-model="messageSearchKeyword"
              placeholder="输入关键词搜索..."
              class="search-input"
              @input="handleMessageSearch"
              ref="messageSearchInputRef"
            />
            <Transition name="fade">
              <button v-if="messageSearchKeyword" class="clear-btn" @click="clearMessageSearch">
                清除
              </button>
            </Transition>
          </div>
        </div>
        
        <!-- 搜索结果区域 -->
        <div class="search-results">
          <!-- 加载状态 -->
          <div v-if="messageSearchLoading" class="search-state loading">
            <div class="loader"></div>
            <p>正在搜索...</p>
          </div>
          
          <!-- 初始状态 -->
          <div v-else-if="!messageSearchKeyword" class="search-state initial">
            <div class="illustration">
              <div class="circle c1"></div>
              <div class="circle c2"></div>
              <div class="circle c3"></div>
              <div class="search-icon-big">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
              </div>
            </div>
            <h4>搜索聊天记录</h4>
            <p>输入关键词，快速查找历史消息</p>
            <div class="search-tips">
              <span class="tip">💡 支持搜索文本消息内容</span>
            </div>
          </div>
          
          <!-- 无结果状态 -->
          <div v-else-if="messageSearchResults.length === 0" class="search-state empty">
            <div class="empty-icon">😕</div>
            <h4>没有找到相关消息</h4>
            <p>换个关键词试试吧</p>
          </div>
          
          <!-- 搜索结果列表 -->
          <div v-else class="search-result-list">
            <div class="result-header-bar">
              <span class="result-count">找到 {{ messageSearchResults.length }} 条消息</span>
            </div>
            <div class="result-items">
              <div 
                v-for="(msg, index) in messageSearchResults" 
                :key="msg.id + '-' + msg.chatType" 
                class="result-item"
                :style="{ animationDelay: index * 0.05 + 's' }"
                @click="jumpToMessage(msg)"
              >
                <el-avatar :size="42" :src="getAvatarUrl(msg.senderAvatar)">
                  {{ msg.senderName?.charAt(0) }}
                </el-avatar>
                <div class="result-info">
                  <div class="result-top">
                    <span class="sender-name">{{ msg.senderName }}</span>
                    <span class="chat-type-tag" :class="msg.chatType === 'GROUP' ? 'group' : 'private'">
                      {{ msg.chatType === 'GROUP' ? msg.groupName : '私聊' }}
                    </span>
                    <span class="msg-time">{{ formatSearchTime(msg.timestamp) }}</span>
                  </div>
                  <div class="result-content" v-html="highlightKeyword(msg.content)"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, inject, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { Picture, Folder, Promotion, Download, ChatLineSquare, User, ArrowLeft, MoreFilled, Close, Key, UserFilled, ArrowRight, ArrowDown, ArrowUp, Delete, SwitchButton, Plus, Search, Loading, DocumentRemove } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toast } from '@/utils/toast'
import { userApi } from '@/api/user'
import { groupApi } from '@/api/group'
import { messageApi } from '@/api/message'
import FriendProfileCard from '@/components/FriendProfileCard.vue'
import MemberProfileCard from '@/components/MemberProfileCard.vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const chatStore = useChatStore()
const sendWsMessage = inject('sendMessage')

const searchKeyword = ref('')
const inputMessage = ref('')
const messageListRef = ref(null)
const imageInputRef = ref(null)
const fileInputRef = ref(null)
const showEmojiPicker = ref(false)
const showProfileCard = ref(false)
const selectedFriend = ref(null)
const showMemberCard = ref(false)
const selectedMember = ref(null)
const showGroupInfo = ref(false)
const groupMembers = ref([])
const showAllMembers = ref(false)
const showInviteDialog = ref(false)
const inviteSearchKeyword = ref('')
const selectedFriendIds = ref([])
const inviteLoading = ref(false)

// 消息右键菜单相关
const messageMenuVisible = ref(false)
const messageMenuPosition = ref({ x: 0, y: 0 })
const selectedMessage = ref(null)

// 消息搜索相关
const showMessageSearch = ref(false)
const messageSearchKeyword = ref('')
const messageSearchResults = ref([])
const messageSearchLoading = ref(false)
const messageSearchInputRef = ref(null)
let searchDebounceTimer = null

// 隐藏消息右键菜单（需要在onMounted之前定义）
const hideMessageMenu = () => {
  messageMenuVisible.value = false
  selectedMessage.value = null
}

// 移动端检测
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)

const handleResize = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  document.addEventListener('click', hideMessageMenu)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', hideMessageMenu)
})

// 移动端返回
const goBack = () => {
  chatStore.clearCurrentChat()
}

// 表情列表
const emojiList = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭',
  '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏',
  '😒', '🙄', '😬', '😮', '😯', '😲', '😳', '🥺',
  '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱',
  '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤',
  '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩',
  '👍', '👎', '👏', '🙌', '🤝', '🙏', '💪', '❤️',
  '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '💯'
]

const userInfo = computed(() => userStore.userInfo)
const currentChat = computed(() => chatStore.currentChat)
const currentMessages = computed(() => chatStore.currentMessages)
const unreadMap = computed(() => chatStore.unreadMap)
const groupUnreadMap = computed(() => chatStore.groupUnreadMap)

// 群组列表过滤
const filteredGroups = computed(() => {
  if (!searchKeyword.value) return chatStore.groupList
  const keyword = searchKeyword.value.toLowerCase()
  return chatStore.groupList.filter(group => 
    group.name?.toLowerCase().includes(keyword)
  )
})

// 处理头像URL，确保路径正确
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  // 如果已经是完整URL或以/api开头，直接返回
  if (avatar.startsWith('http') || avatar.startsWith('/api')) {
    return avatar
  }
  // 否则添加/api前缀
  return avatar.startsWith('/') ? `/api${avatar}` : `/api/${avatar}`
}

// 聊天背景样式
const chatBackgroundStyle = ref({})

// 获取背景样式对象
const getBackgroundStyle = () => {
  const bg = localStorage.getItem('chatBackground') || 'solid-light'
  const backgrounds = {
    'default': 'linear-gradient(180deg, var(--bg-primary) 0%, #f1f5f9 100%)',
    'gradient-blue': 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'gradient-green': 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    'gradient-orange': 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'gradient-dark': 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)',
    'solid-light': '#ffffff',
    'solid-dark': '#1e1e2e'
  }
  
  // 如果是自定义背景图片URL
  if (bg.startsWith('url(')) {
    return {
      backgroundImage: bg,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      backgroundRepeat: 'no-repeat'
    }
  }
  
  // 预设背景
  return {
    background: backgrounds[bg] || '#fafafa'
  }
}

onMounted(() => {
  chatBackgroundStyle.value = getBackgroundStyle()
})

// 监听 localStorage 变化
window.addEventListener('storage', () => {
  chatBackgroundStyle.value = getBackgroundStyle()
})

const filteredFriends = computed(() => {
  if (!searchKeyword.value) return chatStore.friendList
  const keyword = searchKeyword.value.toLowerCase()
  return chatStore.friendList.filter(friend => 
    friend.nickname?.toLowerCase().includes(keyword) ||
    friend.username?.toLowerCase().includes(keyword)
  )
})

const selectChat = (friend) => {
  chatStore.setCurrentChat(friend)
}

// 选择群聊
const selectGroupChat = (group) => {
  chatStore.setCurrentGroupChat(group)
}

// 获取聊天标题
const getChatTitle = () => {
  if (!currentChat.value) return ''
  if (currentChat.value.type === 'group') {
    return currentChat.value.name
  }
  return currentChat.value.nickname || currentChat.value.username
}

// 获取消息发送者头像
const getMessageAvatar = (msg) => {
  if (currentChat.value?.type === 'group') {
    return getAvatarUrl(msg.senderAvatar)
  }
  return getAvatarUrl(currentChat.value?.avatar)
}

// 获取消息发送者名称
const getMessageSenderName = (msg) => {
  if (currentChat.value?.type === 'group') {
    return msg.senderName
  }
  return currentChat.value?.nickname
}

// 显示好友信息卡片
const showFriendProfile = (friend, event) => {
  event?.stopPropagation()
  selectedFriend.value = friend
  showProfileCard.value = true
}

// 显示群成员信息卡片
const showMemberProfile = (msg, event) => {
  event?.stopPropagation()
  // 不显示自己的信息卡片
  if (msg.senderId === userInfo.value?.id) return
  
  // 从群成员列表中查找该成员的角色信息
  const memberInfo = groupMembers.value.find(m => m.userId === msg.senderId)
  
  selectedMember.value = {
    userId: msg.senderId,
    senderId: msg.senderId,
    username: msg.senderUsername,
    senderUsername: msg.senderUsername,
    nickname: msg.senderName,
    senderName: msg.senderName,
    avatar: msg.senderAvatar,
    senderAvatar: msg.senderAvatar,
    role: memberInfo?.role,
    muted: memberInfo?.muted,
    status: memberInfo?.status
  }
  showMemberCard.value = true
}

// 加载群成员列表
const loadGroupMembers = async () => {
  if (!currentChat.value || currentChat.value.type !== 'group') return
  try {
    const res = await groupApi.getGroupMembers(currentChat.value.id)
    if (res.code === 200) {
      groupMembers.value = res.data || []
    }
  } catch (error) {
    console.error('加载群成员失败:', error)
  }
}

// 监听群信息弹窗打开
watch(showGroupInfo, (val) => {
  if (val) {
    loadGroupMembers()
  }
})

// 监听当前聊天变化，如果是群聊则加载成员列表
watch(() => currentChat.value, (chat) => {
  if (chat?.type === 'group') {
    loadGroupMembers()
  } else {
    groupMembers.value = []
  }
}, { immediate: true })

// 获取角色显示文本
const getRoleText = (role) => {
  const roleMap = {
    'OWNER': '群主',
    'ADMIN': '管理员',
    'MEMBER': '成员'
  }
  return roleMap[role] || '成员'
}

// 获取角色标签样式
const getRoleClass = (role) => {
  return {
    'role-owner': role === 'OWNER',
    'role-admin': role === 'ADMIN'
  }
}

// 点击群成员
const handleMemberClick = (member) => {
  // 不显示自己的信息卡片
  if (member.userId === userInfo.value?.id) return
  
  selectedMember.value = {
    userId: member.userId,
    senderId: member.userId,
    username: member.username,
    senderUsername: member.username,
    nickname: member.nickname,
    senderName: member.nickname,
    avatar: member.avatar,
    senderAvatar: member.avatar,
    role: member.role,
    muted: member.muted,
    status: member.status
  }
  showGroupInfo.value = false
  showMemberCard.value = true
}

// 成员信息更新后刷新
const handleMemberUpdated = () => {
  loadGroupMembers()
  chatStore.loadGroupList()
}

// 点击群主
const handleOwnerClick = () => {
  const owner = groupMembers.value.find(m => m.role === 'OWNER')
  if (owner && owner.userId !== userInfo.value?.id) {
    handleMemberClick(owner)
  }
}


// 解散群聊
const handleDissolveGroup = () => {
  ElMessageBox.confirm('确定要解散该群聊吗？此操作不可恢复！', '解散群聊', {
    confirmButtonText: '确定解散',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      const res = await groupApi.dissolveGroup(currentChat.value?.id)
      if (res.code === 200) {
        toast.success('群聊已解散')
        showGroupInfo.value = false
        chatStore.clearCurrentChat()
        chatStore.loadGroupList()
      } else {
        toast.error(res.message || '解散失败')
      }
    } catch (error) {
      toast.error('解散失败')
    }
  }).catch(() => {})
}

// 退出群聊
const handleLeaveGroup = () => {
  ElMessageBox.confirm('确定要退出该群聊吗？', '退出群聊', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await groupApi.leaveGroup(currentChat.value?.id)
      if (res.code === 200) {
        toast.success('已退出群聊')
        showGroupInfo.value = false
        chatStore.clearCurrentChat()
        chatStore.loadGroupList()
      } else {
        toast.error(res.message || '退出失败')
      }
    } catch (error) {
      toast.error('退出失败')
    }
  }).catch(() => {})
}

// 可邀请的好友列表（过滤搜索关键词）
const invitableFriends = computed(() => {
  let friends = chatStore.friendList || []
  if (inviteSearchKeyword.value) {
    const keyword = inviteSearchKeyword.value.toLowerCase()
    friends = friends.filter(f => 
      f.nickname?.toLowerCase().includes(keyword) ||
      f.username?.toLowerCase().includes(keyword) ||
      f.remark?.toLowerCase().includes(keyword)
    )
  }
  return friends
})

// 检查好友是否已在群中
const isMemberInGroup = (userId) => {
  return groupMembers.value.some(m => m.userId === userId)
}

// 监听邀请弹窗打开，重置状态
watch(showInviteDialog, (val) => {
  if (val) {
    selectedFriendIds.value = []
    inviteSearchKeyword.value = ''
  }
})

// 邀请好友加入群聊
const handleInviteMembers = async () => {
  if (selectedFriendIds.value.length === 0) {
    toast.warning('请选择要邀请的好友')
    return
  }
  
  inviteLoading.value = true
  try {
    const res = await groupApi.inviteMembers(currentChat.value?.id, selectedFriendIds.value)
    if (res.code === 200) {
      toast.success(res.data?.message || '邀请成功')
      showInviteDialog.value = false
      // 刷新群成员列表和群信息
      loadGroupMembers()
      chatStore.loadGroupList()
    } else {
      toast.error(res.message || '邀请失败')
    }
  } catch (error) {
    console.error('邀请好友失败:', error)
    toast.error('邀请失败')
  } finally {
    inviteLoading.value = false
  }
}

// 显示消息右键菜单
const showMessageMenu = (event, msg) => {
  selectedMessage.value = msg
  
  // 计算菜单位置，确保不超出屏幕
  const menuWidth = 220
  const menuHeight = 180
  let x = event.clientX
  let y = event.clientY
  
  // 如果菜单会超出右边界，向左偏移
  if (x + menuWidth > window.innerWidth) {
    x = window.innerWidth - menuWidth - 10
  }
  // 如果菜单会超出下边界，向上偏移
  if (y + menuHeight > window.innerHeight) {
    y = window.innerHeight - menuHeight - 10
  }
  
  messageMenuPosition.value = { x, y }
  messageMenuVisible.value = true
}

// 判断是否可以撤回消息（2分钟内且是自己发的消息）
const canRecallMessage = (msg) => {
  if (!msg || msg.senderId !== userInfo.value?.id) return false
  const msgTime = new Date(msg.timestamp || msg.createTime).getTime()
  const now = Date.now()
  return (now - msgTime) < 2 * 60 * 1000 // 2分钟
}

// 复制消息
const copyMessage = async () => {
  if (selectedMessage.value?.content) {
    try {
      await navigator.clipboard.writeText(selectedMessage.value.content)
      toast.success('已复制到剪贴板')
    } catch {
      toast.error('复制失败')
    }
  }
  hideMessageMenu()
}

// 撤回消息
const recallMessage = () => {
  if (!selectedMessage.value) return
  
  const msg = selectedMessage.value
  
  if (!msg.id) {
    toast.error('消息ID为空，无法撤回')
    hideMessageMenu()
    return
  }
  
  const recallMsg = {
    type: 'RECALL',
    id: msg.id,
    senderId: msg.senderId,
    receiverId: msg.receiverId,
    groupId: currentChat.value?.type === 'group' ? currentChat.value.id : null
  }
  
  sendWsMessage(recallMsg)
  hideMessageMenu()
}

// 消息搜索方法
const handleMessageSearch = () => {
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }
  
  if (!messageSearchKeyword.value.trim()) {
    messageSearchResults.value = []
    return
  }
  
  searchDebounceTimer = setTimeout(async () => {
    messageSearchLoading.value = true
    try {
      const res = await messageApi.searchMessages(messageSearchKeyword.value.trim())
      if (res.code === 200) {
        messageSearchResults.value = res.data || []
      }
    } catch (error) {
      console.error('搜索消息失败:', error)
    } finally {
      messageSearchLoading.value = false
    }
  }, 300)
}

const clearMessageSearch = () => {
  messageSearchKeyword.value = ''
  messageSearchResults.value = []
}

const formatSearchTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const today = dayjs()
  if (date.isSame(today, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(today, 'year')) {
    return date.format('MM-DD HH:mm')
  } else {
    return date.format('YYYY-MM-DD')
  }
}

const highlightKeyword = (text) => {
  if (!text || !messageSearchKeyword.value) return text
  const keyword = messageSearchKeyword.value.trim()
  const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const jumpToMessage = async (msg) => {
  showMessageSearch.value = false
  
  if (msg.chatType === 'GROUP') {
    // 群聊消息
    const group = chatStore.groupList.find(g => g.id === msg.groupId)
    if (group) {
      await selectGroupChat(group)
      toast.success('已跳转到群聊')
    }
  } else {
    // 私聊消息
    const friendId = msg.senderId === userInfo.value?.id ? msg.receiverId : msg.senderId
    const friend = chatStore.friendList.find(f => f.userId === friendId)
    
    if (friend) {
      await selectChat(friend)
      toast.success('已跳转到聊天')
    }
  }
}

// 监听搜索对话框打开，自动聚焦
watch(showMessageSearch, (val) => {
  if (val) {
    nextTick(() => {
      messageSearchInputRef.value?.focus()
    })
  } else {
    clearMessageSearch()
  }
})

const getLastMessage = (friendId) => {
  const messages = chatStore.messageMap[friendId]
  if (!messages || messages.length === 0) return ''
  const lastMsg = messages[messages.length - 1]
  
  // 撤回的消息
  if (lastMsg.recalled) {
    return '[消息已撤回]'
  }
  // 根据消息类型显示不同内容
  if (lastMsg.contentType === 'IMAGE') {
    return '[图片]'
  } else if (lastMsg.contentType === 'FILE') {
    return '[文件]'
  }
  return lastMsg.content?.substring(0, 20) || ''
}

const getLastTime = (friendId) => {
  const messages = chatStore.messageMap[friendId]
  if (!messages || messages.length === 0) return ''
  const lastMsg = messages[messages.length - 1]
  return formatTime(lastMsg.timestamp)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = dayjs(time)
  const today = dayjs()
  if (date.isSame(today, 'day')) {
    return date.format('HH:mm')
  } else if (date.isSame(today.subtract(1, 'day'), 'day')) {
    return '昨天'
  } else {
    return date.format('MM-DD')
  }
}

// 获取文件名
const getFileName = (content) => {
  try {
    const fileInfo = JSON.parse(content)
    return fileInfo.name || '未知文件'
  } catch {
    return '未知文件'
  }
}

// 获取文件大小
const getFileSize = (content) => {
  try {
    const fileInfo = JSON.parse(content)
    const size = parseInt(fileInfo.size)
    if (size < 1024) return size + ' B'
    if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
    return (size / 1024 / 1024).toFixed(1) + ' MB'
  } catch {
    return ''
  }
}

// 下载文件
const downloadFile = (content) => {
  try {
    const fileInfo = JSON.parse(content)
    const link = document.createElement('a')
    link.href = fileInfo.url
    link.download = fileInfo.name
    link.click()
  } catch {
    ElMessage.error('下载失败')
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !currentChat.value) return
  
  const isGroup = currentChat.value.type === 'group'
  
  const message = {
    type: isGroup ? 'GROUP_CHAT' : 'CHAT',
    receiverId: isGroup ? null : currentChat.value.userId,
    groupId: isGroup ? currentChat.value.id : null,
    content: inputMessage.value.trim(),
    contentType: 'TEXT'
  }
  
  // 通过WebSocket发送
  if (sendWsMessage) {
    sendWsMessage(message)
  }
  
  // 不在本地先添加消息，等后端WebSocket返回带ID的消息后再添加
  // 这样可以确保消息有正确的ID，支持撤回功能
  
  inputMessage.value = ''
  scrollToBottom()
}

// 插入表情
const insertEmoji = (emoji) => {
  inputMessage.value += emoji
  showEmojiPicker.value = false
}

// 触发图片上传
const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

// 处理图片上传
const handleImageUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  
  if (!currentChat.value) {
    ElMessage.warning('请先选择聊天对象')
    return
  }
  
  const isGroup = currentChat.value.type === 'group'
  
  try {
    const res = await userApi.uploadChatImage(file)
    if (res.code === 200) {
      // 发送图片消息
      const message = {
        type: isGroup ? 'GROUP_CHAT' : 'CHAT',
        receiverId: isGroup ? null : currentChat.value.userId,
        groupId: isGroup ? currentChat.value.id : null,
        content: res.data,
        contentType: 'IMAGE'
      }
      
      if (sendWsMessage) {
        sendWsMessage(message)
      }
      
      // 不在本地先添加消息，等后端WebSocket返回带ID的消息后再添加
      
      scrollToBottom()
      toast.success('图片发送成功')
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
  
  e.target.value = ''
}

// 触发文件上传
const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

// 处理文件上传
const handleFileUpload = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  
  if (file.size > 20 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 20MB')
    return
  }
  
  if (!currentChat.value) {
    ElMessage.warning('请先选择聊天对象')
    return
  }
  
  const isGroup = currentChat.value.type === 'group'
  
  try {
    const res = await userApi.uploadChatFile(file)
    if (res.code === 200) {
      // 发送文件消息，content 包含文件信息
      const fileInfo = res.data
      const message = {
        type: isGroup ? 'GROUP_CHAT' : 'CHAT',
        receiverId: isGroup ? null : currentChat.value.userId,
        groupId: isGroup ? currentChat.value.id : null,
        content: JSON.stringify(fileInfo),
        contentType: 'FILE'
      }
      
      if (sendWsMessage) {
        sendWsMessage(message)
      }
      
      // 不在本地先添加消息，等后端WebSocket返回带ID的消息后再添加
      
      scrollToBottom()
      toast.success('文件发送成功')
    }
  } catch (error) {
    ElMessage.error('文件上传失败')
  }
  
  e.target.value = ''
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动
watch(currentMessages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  height: 100%;
  width: 100%;
}

.conversation-list {
  width: 320px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.search-box {
  padding: 16px;
  display: flex;
  gap: 10px;
  align-items: center;
  
  .search-input-container {
    flex: 1;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: #f5f5f5;
    box-shadow: none;
    border: 1px solid #e5e7eb;
    padding: 8px 14px;
    
    &.is-focus {
      border-color: #1a1a2e;
      background: #fff;
    }
  }
  
  .search-msg-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    min-width: 52px;
    
    .btn-icon {
      width: 20px;
      height: 20px;
      color: #fff;
      
      svg {
        width: 100%;
        height: 100%;
      }
    }
    
    .btn-text {
      font-size: 10px;
      color: rgba(255, 255, 255, 0.9);
      font-weight: 500;
    }
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(26, 26, 46, 0.3);
    }
    
    &:active {
      transform: scale(0.95);
    }
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  font-size: 11px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  
  .el-icon {
    font-size: 14px;
  }
}

.group-avatar {
  background: #1a1a2e;
}

.conversations {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 4px;
  transition: all 0.2s ease;
  
  &:hover {
    background: #f5f5f5;
  }
  
  &.active {
    background: #f0f0f0;
    
    .conversation-name {
      color: #1a1a2e;
      font-weight: 600;
    }
  }
  
  :deep(.el-avatar) {
    border: 2px solid #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.conversation-info {
  flex: 1;
  margin-left: 14px;
  overflow: hidden;
}

.conversation-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.conversation-last {
  font-size: 13px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.conversation-time {
  font-size: 11px;
  color: var(--text-muted);
}

.unread-badge {
  :deep(.el-badge__content) {
    background: #1a1a2e;
    border: none;
    font-size: 10px;
    font-weight: 600;
    height: 18px;
    line-height: 18px;
    padding: 0 6px;
    border-radius: 9px;
  }
}

.clickable-avatar {
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  }
}

// 群组合头像（2x2网格）
.group-avatar-grid {
  width: 45px;
  height: 45px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 1px;
  background: #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  
  .grid-avatar {
    width: 100% !important;
    height: 100% !important;
    border-radius: 0 !important;
    border: none !important;
    box-shadow: none !important;
  }
  
  :deep(.el-avatar) {
    border: none !important;
    box-shadow: none !important;
  }
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.chat-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  
  &.clickable {
    cursor: pointer;
    transition: color 0.2s;
    
    &:hover {
      color: #4f46e5;
    }
  }
}

.group-header-avatar {
  margin-right: 12px;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:hover {
    transform: scale(1.05);
  }
  
  .el-avatar {
    border: 2px solid #e5e7eb;
  }
  
  .header-avatar-grid {
    width: 36px;
    height: 36px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 2px;
    background: #e5e7eb;
    border-radius: 8px;
    padding: 2px;
    
    .grid-avatar {
      border-radius: 4px;
    }
  }
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 12px;
  font-size: 12px;
  font-weight: 500;
  
  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
  
  &.online {
    color: #059669;
    
    .status-dot {
      background: #059669;
    }
  }
  
  &.offline {
    color: #9ca3af;
    
    .status-dot {
      background: #9ca3af;
    }
  }
  
  &.group-member-count {
    color: #6b7280;
    font-weight: 400;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #fafafa;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  
  &.message-self {
    justify-content: flex-end;
    
    .message-content {
      align-items: flex-end;
    }
    
    .message-bubble {
      background: #1a1a2e;
      color: #fff;
      border-radius: 16px 16px 4px 16px;
    }
  }
  
  :deep(.el-avatar) {
    border: 2px solid #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  margin: 0 12px;
  max-width: 60%;
}

.message-sender {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
  font-weight: 500;
}

.system-message {
  width: 100%;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  padding: 8px 16px;
  background: #fff;
  border-radius: 16px;
  margin: 12px auto;
  max-width: 70%;
  border: 1px solid #e5e7eb;
}

.message-bubble {
  background: #fff;
  padding: 12px 16px;
  border-radius: 16px 16px 16px 4px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  border: 1px solid #e5e7eb;
}

.message-image {
  max-width: 240px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  
  :deep(.el-image) {
    display: block;
    width: 100%;
    cursor: pointer;
  }
  
  :deep(img) {
    max-width: 100%;
    max-height: 300px;
    object-fit: cover;
  }
}

.message-file {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border-radius: 12px;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
  
  &:hover {
    background: var(--bg-tertiary);
    
    .download-icon {
      color: var(--primary-color);
    }
  }
  
  .file-icon {
    font-size: 32px;
    color: var(--primary-color);
  }
  
  .file-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }
  
  .file-name {
    font-size: 14px;
    color: var(--text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .file-size {
    font-size: 12px;
    color: var(--text-muted);
  }
  
  .download-icon {
    font-size: 20px;
    color: var(--text-muted);
    transition: color 0.2s;
  }
}

.message-time {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 6px;
  padding: 0 4px;
}

.chat-input {
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 16px 20px;
}

.input-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.toolbar-left {
  display: flex;
  gap: 4px;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  font-size: 18px;
  color: #9ca3af;
  transition: all 0.2s ease;
  
  &:hover {
    background: #f5f5f5;
    color: #1a1a2e;
  }
  
  .el-icon {
    font-size: 18px;
  }
}


.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  
  :deep(.el-textarea) {
    flex: 1;
  }
  
  :deep(.el-textarea__inner) {
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    background: #f5f5f5;
    padding: 12px 16px;
    font-size: 14px;
    resize: none;
    transition: all 0.2s ease;
    
    &:focus {
      border-color: #1a1a2e;
      background: #fff;
    }
    
    &::placeholder {
      color: #9ca3af;
    }
  }
}

.send-btn {
  width: 44px;
  height: 44px;
  border-radius: 10px !important;
  padding: 0 !important;
  background: #1a1a2e !important;
  border: none !important;
  transition: all 0.2s ease !important;
  
  .el-icon {
    font-size: 20px;
  }
  
  &:hover:not(:disabled) {
    background: #2d2d44 !important;
  }
  
  &:disabled {
    opacity: 0.5;
  }
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #fafafa;
  
  :deep(.el-empty__description) {
    color: #9ca3af;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 移动端返回按钮
.back-btn {
  font-size: 22px;
  cursor: pointer;
  margin-right: 12px;
  color: #1a1a2e;
  
  &:hover {
    color: #667eea;
  }
}

// 移动端响应式样式
@media (max-width: 767px) {
  .chat-container {
    position: relative;
  }
  
  .conversation-list {
    width: 100% !important;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    transition: transform 0.3s ease;
    
    &.mobile-hidden {
      transform: translateX(-100%);
    }
  }
  
  .chat-area {
    width: 100% !important;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 20;
  }
  
  .chat-header {
    .chat-title {
      font-size: 16px;
    }
  }
  
  .message-input-area {
    padding: 8px 12px;
    
    .input-actions {
      gap: 8px;
      
      .el-icon {
        font-size: 20px;
      }
    }
    
    :deep(.el-input__wrapper) {
      padding: 8px 12px;
    }
  }
  
  .message-bubble {
    max-width: 85% !important;
    font-size: 14px;
  }
  
  .chat-empty {
    display: none;
  }
}
</style>

<!-- 表情选择器样式（非scoped，因为popover渲染在body下） -->
<style lang="scss">
.emoji-picker {
  padding: 4px;
}

.emoji-title {
  font-size: 13px;
  color: #666;
  padding: 4px 8px 8px;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.15s;
  
  &:hover {
    background: #e8e8e8;
    transform: scale(1.15);
  }
}

// 群信息弹窗样式
.group-info-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.drawer-header {
  position: relative;
  padding: 16px 16px 20px;
  background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .close-btn {
    position: absolute;
    top: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    color: white;
    transition: all 0.2s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: scale(1.1);
    }
  }
  
  .group-avatar-wrapper {
    margin-top: 5px;
  }
  
  .group-avatar-grid-large {
    width: 80px;
    height: 80px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 2px;
    background: #e5e7eb;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    border: 3px solid #fff;
  }
  
  .single-avatar {
    border: 3px solid #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
}

.group-basic-info {
  text-align: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  
  .group-name {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 4px;
  }
  
  .group-desc {
    font-size: 12px;
    color: #9ca3af;
    margin: 0 0 14px;
  }
  
  .group-stats {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 32px;
    padding: 10px 0;
    
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;
      
      .stat-value {
        font-size: 15px;
        font-weight: 600;
        color: #1a1a2e;
      }
      
      .stat-label {
        font-size: 11px;
        color: #9ca3af;
      }
    }
    
    .stat-divider {
      width: 1px;
      height: 28px;
      background: #e5e7eb;
    }
  }
}

.info-card {
  background: #fff;
  margin: 10px 12px;
  border-radius: 12px;
  padding: 14px;
  border: 1px solid #f0f0f0;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
    font-size: 13px;
    font-weight: 600;
    color: #1a1a2e;
    
    .card-icon {
      font-size: 15px;
      color: #1a1a2e;
    }
    
    .member-count {
      margin-left: auto;
      font-size: 12px;
      font-weight: 500;
      color: #9ca3af;
    }
  }
}

.owner-card {
  .owner-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px;
    border-radius: 10px;
    cursor: pointer;
    transition: background 0.2s;
    
    &:hover {
      background: #f5f5f5;
    }
    
    .owner-detail {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 3px;
      
      .owner-name {
        font-size: 14px;
        font-weight: 600;
        color: #1a1a2e;
      }
      
      .owner-tag {
        display: inline-flex;
        align-items: center;
        gap: 3px;
        font-size: 11px;
        color: #1a1a2e;
        background: #f0f0f0;
        padding: 2px 6px;
        border-radius: 4px;
        width: fit-content;
        
        .el-icon {
          font-size: 10px;
        }
      }
    }
    
    .arrow-icon {
      color: #9ca3af;
      font-size: 14px;
    }
  }
}

.members-card {
  .members-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 6px;
    margin-bottom: 10px;
  }
  
  .member-avatar-item {
    position: relative;
    display: flex;
    justify-content: center;
    cursor: pointer;
    
    &:hover .el-avatar {
      transform: scale(1.08);
    }
    
    .el-avatar {
      transition: transform 0.2s;
    }
    
    .member-role-dot {
      position: absolute;
      bottom: 0;
      right: 50%;
      transform: translateX(50%);
      width: 8px;
      height: 8px;
      border-radius: 50%;
      border: 2px solid #fff;
      
      &.role-owner {
        background: #1a1a2e;
      }
      
      &.role-admin {
        background: #6b7280;
      }
    }
    
    &.more-btn {
      .more-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #f5f5f5;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #9ca3af;
        transition: all 0.2s;
        
        &:hover {
          background: #e5e7eb;
          color: #1a1a2e;
        }
      }
    }
  }
  
  .members-list-expanded {
    max-height: 200px;
    overflow-y: auto;
    border-top: 1px solid #f0f0f0;
    padding-top: 10px;
    
    .member-list-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 6px 8px;
      border-radius: 8px;
      cursor: pointer;
      transition: background 0.2s;
      
      &:hover {
        background: #f5f5f5;
      }
      
      .member-name {
        flex: 1;
        font-size: 13px;
        color: #1a1a2e;
      }
      
      .member-role-tag {
        font-size: 10px;
        padding: 2px 6px;
        border-radius: 4px;
        font-weight: 500;
        
        &.role-owner {
          background: #1a1a2e;
          color: #fff;
        }
        
        &.role-admin {
          background: #e5e7eb;
          color: #4b5563;
        }
      }
      
      .online-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        
        &.online {
          background: #10b981;
        }
        
        &.offline {
          background: #d1d5db;
        }
      }
    }
  }
  
  .toggle-members {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 10px;
    font-size: 12px;
    color: #6b7280;
    cursor: pointer;
    border-top: 1px solid #f0f0f0;
    margin-top: 8px;
    
    &:hover {
      color: #1a1a2e;
    }
    
    .el-icon {
      font-size: 12px;
    }
  }
}

.action-buttons {
  margin-top: auto;
  padding: 12px;
  
  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 14px;
    border-radius: 12px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    
    &.danger {
      background: #1a1a2e;
      color: #fff;
      border: none;
      
      &:hover {
        background: #2d2d44;
      }
    }
    
    &.warning {
      background: #f5f5f5;
      color: #1a1a2e;
      border: 1px solid #e5e7eb;
      
      &:hover {
        background: #e5e7eb;
      }
    }
    
    .el-icon {
      font-size: 16px;
    }
  }
}

// 邀请按钮样式
.invite-btn {
  margin-left: auto;
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 6px;
  background: #1a1a2e !important;
  border-color: #1a1a2e !important;
  
  &:hover {
    background: #2d2d44 !important;
    border-color: #2d2d44 !important;
  }
}

// 邀请弹窗样式
.invite-dialog {
  :deep(.el-dialog) {
    border-radius: 12px;
    overflow: hidden;
  }
  
  :deep(.el-dialog__header) {
    background: #1a1a2e;
    padding: 16px 20px;
    margin: 0;
    
    .el-dialog__title {
      color: #fff;
      font-size: 15px;
      font-weight: 500;
    }
    
    .el-dialog__headerbtn {
      top: 16px;
      
      .el-dialog__close {
        color: #fff;
        
        &:hover {
          color: #e5e7eb;
        }
      }
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  .invite-content {
    .invite-search {
      margin-bottom: 16px;
      
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        box-shadow: 0 0 0 1px #e5e7eb;
        
        &:hover, &.is-focus {
          box-shadow: 0 0 0 1px #1a1a2e;
        }
      }
    }
    
    .friend-select-list {
      max-height: 320px;
      overflow-y: auto;
      
      &::-webkit-scrollbar {
        width: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: #d1d5db;
        border-radius: 2px;
      }
      
      .friend-select-item {
        padding: 10px 8px;
        border-radius: 8px;
        margin-bottom: 4px;
        transition: background 0.2s;
        
        &:hover {
          background: #f9fafb;
        }
        
        .el-checkbox {
          width: 100%;
          
          :deep(.el-checkbox__input) {
            .el-checkbox__inner {
              border-radius: 4px;
              border-color: #d1d5db;
              
              &:hover {
                border-color: #1a1a2e;
              }
            }
            
            &.is-checked .el-checkbox__inner {
              background: #1a1a2e;
              border-color: #1a1a2e;
            }
          }
          
          :deep(.el-checkbox__label) {
            flex: 1;
            padding-left: 12px;
          }
        }
        
        .friend-info {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .el-avatar {
            border: 2px solid #f3f4f6;
          }
          
          .friend-name {
            flex: 1;
            font-size: 14px;
            color: #1a1a2e;
            font-weight: 500;
          }
          
          .already-member {
            font-size: 11px;
            color: #9ca3af;
            padding: 3px 10px;
            background: #f3f4f6;
            border-radius: 12px;
          }
        }
      }
    }
  }
  
  :deep(.el-dialog__footer) {
    padding: 16px 20px;
    border-top: 1px solid #f0f0f0;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    
    .el-button {
      border-radius: 8px;
      padding: 10px 24px;
      font-weight: 500;
      
      &--default {
        background: #fff;
        border-color: #e5e7eb;
        color: #1a1a2e;
        
        &:hover {
          background: #f9fafb;
          border-color: #d1d5db;
        }
      }
      
      &--primary {
        background: #1a1a2e;
        border-color: #1a1a2e;
        
        &:hover {
          background: #2d2d44;
          border-color: #2d2d44;
        }
        
        &.is-disabled {
          background: #9ca3af;
          border-color: #9ca3af;
        }
      }
    }
  }
}

// 消息右键菜单样式
.message-context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 220px;
  overflow: visible;
  
  .menu-header {
    padding: 12px 16px 8px;
    border-bottom: 1px solid #f0f0f0;
    
    .menu-title {
      font-size: 11px;
      font-weight: 600;
      color: #9ca3af;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
  }
  
  .menu-body {
    padding: 8px;
  }
  
  .menu-item {
    padding: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 12px;
    border-radius: 12px;
    transition: all 0.2s ease;
    
    .menu-icon-wrapper {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      svg {
        width: 20px;
        height: 20px;
        color: #1a1a2e;
      }
      
      &.warning {
        background: linear-gradient(135deg, #fef3f2 0%, #fee2e2 100%);
        
        svg {
          color: #ef4444;
        }
      }
    }
    
    .menu-text {
      display: flex;
      flex-direction: column;
      gap: 2px;
      
      .menu-label {
        font-size: 14px;
        font-weight: 600;
        color: #1a1a2e;
      }
      
      .menu-hint {
        font-size: 12px;
        color: #9ca3af;
      }
    }
    
    &:hover {
      background: #f8f9fa;
      
      .menu-icon-wrapper {
        transform: scale(1.05);
      }
    }
    
    &:active {
      transform: scale(0.98);
    }
    
    &.warning {
      .menu-label {
        color: #ef4444;
      }
      
      &:hover {
        background: #fef2f2;
      }
    }
  }
}

// 菜单过渡动画
.context-menu-enter-active {
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.context-menu-leave-active {
  transition: all 0.15s ease-out;
}

.context-menu-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-8px);
}

.context-menu-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

// 消息搜索对话框样式
.message-search-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
  
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
  }
}

.search-dialog-container {
  background: #fff;
}

.search-dialog-top {
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0;
  }
  
  .close-btn {
    width: 32px;
    height: 32px;
    border: none;
    background: #f5f5f5;
    border-radius: 8px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    
    svg {
      width: 18px;
      height: 18px;
      color: #6b7280;
    }
    
    &:hover {
      background: #1a1a2e;
      
      svg {
        color: #fff;
      }
    }
  }
}

.search-input-box {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 4px 4px 4px 16px;
  border: 2px solid transparent;
  transition: all 0.2s;
  
  &:focus-within {
    background: #fff;
    border-color: #1a1a2e;
  }
  
  .search-input {
    flex: 1;
    border: none;
    outline: none;
    font-size: 15px;
    background: transparent;
    padding: 10px 0;
    
    &::placeholder {
      color: #9ca3af;
    }
  }
  
  .clear-btn {
    padding: 8px 16px;
    background: #1a1a2e;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #2d2d44;
    }
  }
}

.search-results {
  height: 380px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #e5e7eb;
    border-radius: 3px;
  }
}

.search-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 40px 20px;
  text-align: center;
  
  &.loading {
    .loader {
      width: 40px;
      height: 40px;
      border: 3px solid #f0f0f0;
      border-top-color: #1a1a2e;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-bottom: 16px;
    }
    
    p {
      color: #6b7280;
      font-size: 14px;
    }
  }
  
  &.initial {
    .illustration {
      position: relative;
      width: 120px;
      height: 120px;
      margin-bottom: 24px;
      
      .circle {
        position: absolute;
        border-radius: 50%;
        
        &.c1 {
          width: 120px;
          height: 120px;
          background: linear-gradient(135deg, #f0f4ff 0%, #e0e7ff 100%);
          top: 0;
          left: 0;
        }
        
        &.c2 {
          width: 80px;
          height: 80px;
          background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
          top: 20px;
          left: 20px;
        }
        
        &.c3 {
          width: 50px;
          height: 50px;
          background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
          top: 35px;
          left: 35px;
        }
      }
      
      .search-icon-big {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        
        svg {
          width: 28px;
          height: 28px;
          color: #fff;
        }
      }
    }
    
    h4 {
      font-size: 17px;
      font-weight: 600;
      color: #1a1a2e;
      margin: 0 0 8px;
    }
    
    p {
      font-size: 14px;
      color: #9ca3af;
      margin: 0 0 20px;
    }
    
    .search-tips {
      .tip {
        display: inline-block;
        padding: 8px 16px;
        background: #f8fafc;
        border-radius: 20px;
        font-size: 13px;
        color: #6b7280;
      }
    }
  }
  
  &.empty {
    .empty-icon {
      font-size: 56px;
      margin-bottom: 16px;
    }
    
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #1a1a2e;
      margin: 0 0 8px;
    }
    
    p {
      font-size: 14px;
      color: #9ca3af;
      margin: 0;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.search-result-list {
  .result-header-bar {
    padding: 12px 20px;
    background: #f8fafc;
    border-bottom: 1px solid #f0f0f0;
    
    .result-count {
      font-size: 13px;
      color: #6b7280;
    }
  }
  
  .result-items {
    padding: 8px;
  }
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  animation: slideIn 0.3s ease forwards;
  opacity: 0;
  
  &:hover {
    background: #f5f5f5;
  }
  
  .result-info {
    flex: 1;
    min-width: 0;
  }
  
  .result-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
  }
  
  .sender-name {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a2e;
  }
  
  .chat-type-tag {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 10px;
    font-weight: 500;
    
    &.private {
      background: #e0f2fe;
      color: #0284c7;
    }
    
    &.group {
      background: #dcfce7;
      color: #16a34a;
    }
  }
  
  .msg-time {
    font-size: 12px;
    color: #9ca3af;
    margin-left: auto;
  }
  
  .result-content {
    font-size: 13px;
    color: #6b7280;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    
    :deep(mark) {
      background: #fef08a;
      color: #1a1a2e;
      padding: 1px 4px;
      border-radius: 3px;
      font-weight: 500;
    }
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 淡入淡出动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
