<template>
  <el-dialog
    v-model="visible"
    title=""
    width="420px"
    :close-on-click-modal="false"
    class="create-group-dialog"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-icon">
          <el-icon><ChatLineSquare /></el-icon>
        </div>
        <div class="header-text">
          <h3>创建群聊</h3>
          <p>邀请好友一起聊天</p>
        </div>
      </div>
    </template>
    
    <div class="form-section">
      <div class="form-item">
        <label class="form-label">群名称 <span class="required">*</span></label>
        <el-input 
          v-model="form.name" 
          placeholder="给群聊起个名字吧" 
          maxlength="100" 
          show-word-limit
          class="custom-input"
        />
      </div>
      
      <div class="form-item">
        <label class="form-label">群描述</label>
        <el-input 
          v-model="form.description" 
          type="textarea" 
          :rows="2" 
          placeholder="介绍一下这个群（可选）" 
          maxlength="500" 
          show-word-limit
          class="custom-input"
        />
      </div>
      
      <div class="form-item">
        <label class="form-label">邀请成员 <span class="member-count">已选 {{ form.memberIds.length }} 人</span></label>
        <div class="member-select">
          <div 
            v-for="friend in friendList" 
            :key="friend.userId" 
            class="member-item"
            :class="{ selected: form.memberIds.includes(friend.userId) }"
            @click="toggleMember(friend.userId)"
          >
            <el-avatar :size="40" :src="getAvatarUrl(friend.avatar)">
              {{ friend.nickname?.charAt(0) || friend.username?.charAt(0) }}
            </el-avatar>
            <span class="member-name">{{ friend.nickname || friend.username }}</span>
            <div class="check-icon" v-if="form.memberIds.includes(friend.userId)">
              <el-icon><Check /></el-icon>
            </div>
          </div>
          <el-empty v-if="friendList.length === 0" description="暂无好友可邀请" :image-size="60" />
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" class="cancel-btn">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleCreate" class="create-btn">
          <el-icon v-if="!loading"><Plus /></el-icon>
          创建群聊
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import { groupApi } from '@/api/group'
import { ElMessage } from 'element-plus'
import { toast } from '@/utils/toast'
import { ChatLineSquare, Check, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'created'])

const chatStore = useChatStore()
const loading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = ref({
  name: '',
  description: '',
  memberIds: []
})

const friendList = computed(() => chatStore.friendList)

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http') || avatar.startsWith('/api')) {
    return avatar
  }
  return avatar.startsWith('/') ? `/api${avatar}` : `/api/${avatar}`
}

const toggleMember = (userId) => {
  const index = form.value.memberIds.indexOf(userId)
  if (index > -1) {
    form.value.memberIds.splice(index, 1)
  } else {
    form.value.memberIds.push(userId)
  }
}

const handleClose = () => {
  form.value = {
    name: '',
    description: '',
    memberIds: []
  }
  visible.value = false
}

const handleCreate = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入群名称')
    return
  }
  
  loading.value = true
  try {
    const res = await groupApi.createGroup({
      name: form.value.name.trim(),
      description: form.value.description.trim(),
      memberIds: form.value.memberIds
    })
    
    if (res.code === 200) {
      toast.success('群聊创建成功')
      // 重新加载群组列表
      await chatStore.loadGroupList()
      emit('created', res.data)
      handleClose()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建群聊失败:', error)
    ElMessage.error('创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 0 24px 24px;
}

:deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
  color: #fff;
  
  .header-icon {
    width: 48px;
    height: 48px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .el-icon {
      font-size: 24px;
    }
  }
  
  .header-text {
    h3 {
      margin: 0 0 4px;
      font-size: 18px;
      font-weight: 600;
    }
    
    p {
      margin: 0;
      font-size: 13px;
      opacity: 0.9;
    }
  }
}

.form-section {
  padding-top: 20px;
}

.form-item {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.form-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 8px;
  
  .required {
    color: #ef4444;
  }
  
  .member-count {
    margin-left: auto;
    font-size: 12px;
    color: var(--primary-color);
    font-weight: 400;
  }
}

.custom-input {
  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 10px;
    box-shadow: none;
    border: 1px solid var(--border-light);
    
    &:focus, &.is-focus {
      border-color: var(--primary-color);
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
    }
  }
}

.member-select {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 8px;
  background: var(--bg-tertiary);
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  
  &:hover {
    background: var(--bg-secondary);
  }
  
  &.selected {
    background: rgba(99, 102, 241, 0.1);
    
    .member-name {
      color: var(--primary-color);
      font-weight: 500;
    }
  }
  
  :deep(.el-avatar) {
    flex-shrink: 0;
  }
}

.member-name {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
}

.check-icon {
  width: 22px;
  height: 22px;
  background: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  
  .el-icon {
    font-size: 14px;
  }
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  
  .cancel-btn {
    border-radius: 10px;
    padding: 10px 20px;
  }
  
  .create-btn {
    border-radius: 10px;
    padding: 10px 24px;
    background: var(--primary-gradient);
    border: none;
    
    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
