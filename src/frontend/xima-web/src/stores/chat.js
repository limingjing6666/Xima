import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { friendApi } from '@/api/friend'
import { messageApi } from '@/api/message'
import { groupApi } from '@/api/group'

export const useChatStore = defineStore('chat', () => {
  // 好友列表
  const friendList = ref([])
  // 群组列表
  const groupList = ref([])
  // 当前聊天对象 { type: 'friend' | 'group', ...data }
  const currentChat = ref(null)
  // 私聊消息列表 { friendId: [messages] }
  const messageMap = ref({})
  // 群聊消息列表 { groupId: [messages] }
  const groupMessageMap = ref({})
  // 私聊未读消息数 { friendId: count }
  const unreadMap = ref({})
  // 群聊未读消息数 { groupId: count }
  const groupUnreadMap = ref({})

  // 总未读数（私聊+群聊）
  const totalUnreadCount = computed(() => {
    const friendUnread = Object.values(unreadMap.value).reduce((sum, count) => sum + count, 0)
    const groupUnread = Object.values(groupUnreadMap.value).reduce((sum, count) => sum + count, 0)
    return friendUnread + groupUnread
  })

  // 当前聊天的消息列表
  const currentMessages = computed(() => {
    if (!currentChat.value) return []
    if (currentChat.value.type === 'group') {
      return groupMessageMap.value[currentChat.value.id] || []
    }
    return messageMap.value[currentChat.value.userId] || []
  })

  // 加载好友列表
  async function loadFriendList() {
    try {
      const res = await friendApi.getFriendList()
      if (res.code === 200) {
        friendList.value = res.data || []
        // 加载好友列表后，初始化未读消息数
        await loadOfflineMessages()
      }
    } catch (error) {
      console.error('加载好友列表失败:', error)
    }
  }

  // 加载离线消息并统计未读数
  async function loadOfflineMessages() {
    try {
      const res = await messageApi.getOfflineMessages()
      if (res.code === 200 && res.data) {
        const offlineMessages = res.data
        // 按发送者分组统计未读数
        offlineMessages.forEach(msg => {
          const senderId = msg.senderId
          // 添加到消息列表
          if (!messageMap.value[senderId]) {
            messageMap.value[senderId] = []
          }
          // 避免重复添加
          const exists = messageMap.value[senderId].some(m => m.id === msg.id)
          if (!exists) {
            messageMap.value[senderId].push(msg)
            // 增加未读数
            unreadMap.value[senderId] = (unreadMap.value[senderId] || 0) + 1
          }
        })
      }
    } catch (error) {
      console.error('加载离线消息失败:', error)
    }
  }

  // 设置当前聊天对象（私聊）
  async function setCurrentChat(friend) {
    currentChat.value = { ...friend, type: 'friend' }
    if (friend) {
      // 清除未读数
      unreadMap.value[friend.userId] = 0
      // 加载聊天历史
      await loadChatHistory(friend.userId)
    }
  }

  // 清除当前聊天（移动端返回用）
  function clearCurrentChat() {
    currentChat.value = null
  }

  // 设置当前群聊
  async function setCurrentGroupChat(group) {
    currentChat.value = { ...group, type: 'group' }
    if (group) {
      // 清除未读数
      groupUnreadMap.value[group.id] = 0
      // 加载群聊历史
      await loadGroupChatHistory(group.id)
      // 标记已读
      try {
        await groupApi.markAsRead(group.id)
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    }
  }

  // 加载私聊历史
  async function loadChatHistory(friendId) {
    try {
      const res = await messageApi.getChatHistory(friendId)
      if (res.code === 200) {
        messageMap.value[friendId] = res.data || []
      }
    } catch (error) {
      console.error('加载聊天历史失败:', error)
    }
  }

  // 加载群聊历史
  async function loadGroupChatHistory(groupId) {
    try {
      const res = await groupApi.getMessages(groupId)
      if (res.code === 200) {
        // 消息按时间正序排列
        groupMessageMap.value[groupId] = (res.data || []).reverse()
      }
    } catch (error) {
      console.error('加载群聊历史失败:', error)
    }
  }

  // 加载群组列表
  async function loadGroupList() {
    try {
      const res = await groupApi.getMyGroups()
      if (res.code === 200) {
        groupList.value = res.data || []
        // 加载群聊未读数
        await loadGroupUnreadCounts()
      }
    } catch (error) {
      console.error('加载群组列表失败:', error)
    }
  }

  // 加载群聊未读消息数
  async function loadGroupUnreadCounts() {
    try {
      const res = await groupApi.getUnreadCounts()
      if (res.code === 200 && res.data) {
        // res.data 是 { groupId: count } 的对象
        Object.entries(res.data).forEach(([groupId, count]) => {
          groupUnreadMap.value[groupId] = count
        })
      }
    } catch (error) {
      console.error('加载群聊未读数失败:', error)
    }
  }

  // 接收私聊消息
  function receiveMessage(message) {
    const friendId = message.senderId
    
    // 添加到消息列表
    if (!messageMap.value[friendId]) {
      messageMap.value[friendId] = []
    }
    // 检查是否已存在（避免重复添加）
    if (message.id) {
      const exists = messageMap.value[friendId].some(m => m.id === message.id)
      if (exists) return
    }
    messageMap.value[friendId].push(message)
    
    // 如果不是当前聊天，增加未读数
    if (!currentChat.value || currentChat.value.type !== 'friend' || currentChat.value.userId !== friendId) {
      unreadMap.value[friendId] = (unreadMap.value[friendId] || 0) + 1
    }
  }

  // 接收群聊消息
  function receiveGroupMessage(message) {
    const groupId = message.groupId
    
    // 添加到消息列表
    if (!groupMessageMap.value[groupId]) {
      groupMessageMap.value[groupId] = []
    }
    // 检查是否已存在（避免重复添加）
    if (message.id) {
      const exists = groupMessageMap.value[groupId].some(m => m.id === message.id)
      if (exists) return
    }
    groupMessageMap.value[groupId].push(message)
    
    // 如果不是当前群聊，增加未读数
    if (!currentChat.value || currentChat.value.type !== 'group' || currentChat.value.id !== groupId) {
      groupUnreadMap.value[groupId] = (groupUnreadMap.value[groupId] || 0) + 1
    }
  }

  // 发送私聊消息后添加到列表
  function addSentMessage(message) {
    const friendId = message.receiverId
    if (!messageMap.value[friendId]) {
      messageMap.value[friendId] = []
    }
    // 检查是否已存在（避免重复添加）
    if (message.id) {
      const exists = messageMap.value[friendId].some(m => m.id === message.id)
      if (exists) return
    }
    messageMap.value[friendId].push(message)
  }

  // 发送群聊消息后添加到列表
  function addSentGroupMessage(message) {
    const groupId = message.groupId
    if (!groupMessageMap.value[groupId]) {
      groupMessageMap.value[groupId] = []
    }
    // 检查是否已存在（避免重复添加）
    if (message.id) {
      const exists = groupMessageMap.value[groupId].some(m => m.id === message.id)
      if (exists) return
    }
    groupMessageMap.value[groupId].push(message)
  }

  // 更新好友状态
  function updateFriendStatus(friendId, status) {
    const friend = friendList.value.find(f => f.userId === friendId)
    if (friend) {
      friend.status = status
    }
    // 如果当前聊天对象是这个好友，也更新
    if (currentChat.value && currentChat.value.type === 'friend' && currentChat.value.userId === friendId) {
      currentChat.value.status = status
    }
  }

  // 处理消息撤回
  function recallMessage(messageId, groupId, content) {
    if (groupId) {
      // 群消息撤回
      const messages = groupMessageMap.value[groupId]
      if (messages) {
        const msg = messages.find(m => m.id === messageId)
        if (msg) {
          msg.recalled = true
          msg.content = content
        }
      }
    } else {
      // 私聊消息撤回 - 遍历所有私聊消息
      for (const friendId in messageMap.value) {
        const messages = messageMap.value[friendId]
        const msg = messages.find(m => m.id === messageId)
        if (msg) {
          msg.recalled = true
          msg.content = content
          break
        }
      }
    }
  }

  return {
    friendList,
    groupList,
    currentChat,
    messageMap,
    groupMessageMap,
    unreadMap,
    groupUnreadMap,
    totalUnreadCount,
    currentMessages,
    loadFriendList,
    loadGroupList,
    loadOfflineMessages,
    setCurrentChat,
    setCurrentGroupChat,
    clearCurrentChat,
    loadChatHistory,
    loadGroupChatHistory,
    receiveMessage,
    receiveGroupMessage,
    addSentMessage,
    addSentGroupMessage,
    updateFriendStatus,
    recallMessage
  }
})
