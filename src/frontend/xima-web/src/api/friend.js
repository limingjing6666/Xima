import request from './request'

export const friendApi = {
  // 获取好友列表
  getFriendList() {
    return request.get('/v1/friends')
  },

  // 发送好友请求
  sendFriendRequest(friendId) {
    return request.post('/v1/friends/request', { friendId })
  },

  // 接受好友请求
  acceptFriendRequest(requestId) {
    return request.post(`/v1/friends/request/${requestId}/accept`)
  },

  // 拒绝好友请求
  rejectFriendRequest(requestId) {
    return request.post(`/v1/friends/request/${requestId}/reject`)
  },

  // 删除好友
  deleteFriend(friendId) {
    return request.delete(`/v1/friends/${friendId}`)
  },

  // 获取待处理好友请求列表
  getFriendRequests() {
    return request.get('/v1/friends/requests/pending')
  },

  // 检查好友关系
  checkFriendship(friendId) {
    return request.get(`/v1/friends/check/${friendId}`)
  },

  // 设置好友备注
  setFriendRemark(friendId, remark) {
    return request.put(`/v1/friends/${friendId}/remark`, { remark })
  }
}
