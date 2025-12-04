import request from './request'

export const authApi = {
  // 登录
  login(data) {
    return request.post('/v1/auth/login', data)
  },

  // 注册
  register(data) {
    return request.post('/v1/auth/register', data)
  },

  // 刷新Token
  refreshToken() {
    return request.post('/v1/auth/refresh')
  },

  // 发送邮箱验证码
  sendCode(email) {
    return request.post('/v1/auth/send-code', { email })
  },

  // 发送重置密码验证码
  sendResetCode(email) {
    return request.post('/v1/auth/send-reset-code', { email })
  },

  // 重置密码
  resetPassword(data) {
    return request.post('/v1/auth/reset-password', data)
  }
}
