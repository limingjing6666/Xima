import request from './request'

export const userApi = {
  // 获取用户信息
  getUserById(id) {
    return request.get(`/v1/users/${id}`)
  },

  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/v1/users/me')
  },

  // 更新当前用户信息
  updateCurrentUser(data) {
    return request.put('/v1/users/me', data)
  },

  // 更新用户状态
  updateStatus(status) {
    return request.put('/v1/users/me/status', null, { params: { status } })
  },

  // 搜索用户
  searchUsers(keyword) {
    return request.get('/v1/users/search', { params: { keyword } })
  },

  // 上传头像
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/v1/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传聊天背景
  uploadBackground(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/v1/users/me/background', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传聊天图片
  uploadChatImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/v1/users/chat/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 上传聊天文件
  uploadChatFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/v1/users/chat/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 修改密码
  changePassword(oldPassword, newPassword) {
    return request.put('/v1/users/me/password', { oldPassword, newPassword })
  },

  // 更新聊天背景设置
  updateChatBackground(chatBackground) {
    return request.put('/v1/users/me/chat-background', { chatBackground })
  },

  // 获取聊天背景设置
  getChatBackground() {
    return request.get('/v1/users/me/chat-background')
  }
}
