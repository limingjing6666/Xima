import { ElNotification } from 'element-plus'

/**
 * 轻量级提示工具
 * 使用右下角通知替代顶部弹窗，更优雅不突兀
 */
export const toast = {
  success(message, title = '成功') {
    ElNotification({
      title,
      message,
      type: 'success',
      duration: 2000,
      position: 'bottom-right',
      showClose: false
    })
  },
  
  error(message, title = '错误') {
    ElNotification({
      title,
      message,
      type: 'error',
      duration: 3000,
      position: 'bottom-right'
    })
  },
  
  warning(message, title = '提示') {
    ElNotification({
      title,
      message,
      type: 'warning',
      duration: 2500,
      position: 'bottom-right'
    })
  },
  
  info(message, title = '提示') {
    ElNotification({
      title,
      message,
      type: 'info',
      duration: 2000,
      position: 'bottom-right',
      showClose: false
    })
  }
}
