import request from './request'

/**
 * 群组相关API
 */
export const groupApi = {
  // 创建群组
  createGroup(data) {
    return request({
      url: '/v1/groups',
      method: 'post',
      data
    })
  },

  // 获取我的群组列表
  getMyGroups() {
    return request({
      url: '/v1/groups',
      method: 'get'
    })
  },

  // 获取群组详情
  getGroupDetail(groupId) {
    return request({
      url: `/v1/groups/${groupId}`,
      method: 'get'
    })
  },

  // 更新群组信息
  updateGroup(groupId, data) {
    return request({
      url: `/v1/groups/${groupId}`,
      method: 'put',
      data
    })
  },

  // 获取群成员列表
  getGroupMembers(groupId) {
    return request({
      url: `/v1/groups/${groupId}/members`,
      method: 'get'
    })
  },

  // 添加群成员
  addMember(groupId, userId) {
    return request({
      url: `/v1/groups/${groupId}/members`,
      method: 'post',
      data: { userId }
    })
  },

  // 移除群成员
  removeMember(groupId, userId) {
    return request({
      url: `/v1/groups/${groupId}/members/${userId}`,
      method: 'delete'
    })
  },

  // 退出群组
  leaveGroup(groupId) {
    return request({
      url: `/v1/groups/${groupId}/leave`,
      method: 'post'
    })
  },

  // 解散群组
  dissolveGroup(groupId) {
    return request({
      url: `/v1/groups/${groupId}`,
      method: 'delete'
    })
  },

  // 获取群聊天记录
  getMessages(groupId, page = 0, size = 50) {
    return request({
      url: `/v1/groups/${groupId}/messages`,
      method: 'get',
      params: { page, size }
    })
  },

  // 发送群消息（HTTP方式，备用）
  sendMessage(groupId, content, contentType = 'TEXT') {
    return request({
      url: `/v1/groups/${groupId}/messages`,
      method: 'post',
      data: { content, contentType }
    })
  },

  // 获取群聊未读消息数
  getUnreadCounts() {
    return request({
      url: '/v1/groups/unread',
      method: 'get'
    })
  },

  // 标记群消息已读
  markAsRead(groupId) {
    return request({
      url: `/v1/groups/${groupId}/read`,
      method: 'post'
    })
  },

  // 设置/取消管理员
  setAdmin(groupId, userId, isAdmin) {
    return request({
      url: `/v1/groups/${groupId}/admin`,
      method: 'post',
      data: { userId, isAdmin }
    })
  },

  // 转让群主
  transferOwner(groupId, newOwnerId) {
    return request({
      url: `/v1/groups/${groupId}/transfer`,
      method: 'post',
      data: { newOwnerId }
    })
  },

  // 禁言/解除禁言
  setMuted(groupId, userId, muted) {
    return request({
      url: `/v1/groups/${groupId}/mute`,
      method: 'post',
      data: { userId, muted }
    })
  },

  // 邀请好友加入群聊
  inviteMembers(groupId, userIds) {
    return request({
      url: `/v1/groups/${groupId}/invite`,
      method: 'post',
      data: { groupId, userIds }
    })
  }
}
