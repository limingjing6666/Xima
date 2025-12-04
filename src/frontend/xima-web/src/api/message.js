import request from './request'

export const messageApi = {
  // 获取聊天历史 (后端page从0开始)
  getChatHistory(friendId, page = 0, size = 20) {
    return request.get(`/v1/messages/history/${friendId}`, {
      params: { page, size }
    })
  },

  // 获取离线消息
  getOfflineMessages() {
    return request.get('/v1/messages/offline')
  },

  // 标记消息已读 (后端接收messageId而非senderId)
  markAsRead(messageId) {
    return request.post(`/v1/messages/${messageId}/read`)
  },

  // 获取未读消息数 (需要senderId参数)
  getUnreadCount(senderId) {
    return request.get(`/v1/messages/unread/count/${senderId}`)
  },

  // 删除消息
  deleteMessage(messageId) {
    return request.delete(`/v1/messages/${messageId}`)
  },

  // 搜索消息
  searchMessages(keyword, page = 0, size = 20) {
    return request.get('/v1/messages/search', {
      params: { keyword, page, size }
    })
  }
}
