<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  isOpen: Boolean,
  user: Object
})

const emit = defineEmits(['close', 'updateProfile', 'updateUsername', 'updatePassword', 'logout'])

const state = reactive({
  activeTab: 'profile',
  username: '',
  oldPassword: '',
  newPassword: '',
  showOldPassword: false,
  showNewPassword: false,
  avatarFile: null,
  avatarPreview: '',
  error: '',
  usernameError: '',
  passwordError: ''
})

// 监听用户信息变化
watch(() => props.user, (newUser) => {
  if (newUser) {
    state.username = newUser.username || ''
  }
}, { immediate: true })


const handleUpdateProfile = () => {
  state.usernameError = ''
  const username = state.username.trim()
  
  console.log('📝 Updating username:', { 
    newUsername: username, 
    currentUsername: props.user?.username,
    isSame: username === props.user?.username 
  })
  
  if (!username) {
    state.usernameError = '用户名不能为空'
    return
  }
  
  if (username.length < 3 || username.length > 25) {
    state.usernameError = '用户名不符合规范'
    return
  }
  
  // 检查是否与当前用户名相同
  if (username === props.user?.username) {
    state.usernameError = '新用户名与当前用户名相同'
    return
  }
  
  emit('updateUsername', username)
}

const handleAvatarChange = (event) => {
  state.error = ''
  const file = event.target.files[0]
  if (file) {
    if (file.size > 2 * 1024 * 1024) { // 2MB limit
      state.error = '图片大小不能超过2MB'
      return
    }

    state.avatarFile = file

    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      state.avatarPreview = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const handleUpdateAvatar = () => {
  if (state.avatarFile || state.avatarPreview) {
    const avatarData = state.avatarPreview || (props.user?.avatar)
    emit('updateProfile', { avatar: avatarData })
    state.avatarFile = null
    state.avatarPreview = ''
  }
}

const handleUpdatePassword = () => {
  state.passwordError = ''
  
  if (!state.oldPassword) {
    state.passwordError = '请输入当前密码'
    return
  }
  
  if (!state.newPassword) {
    state.passwordError = '请输入新密码'
    return
  }
  
  if (state.newPassword.length < 6 || state.newPassword.length > 25) {
    state.passwordError = '密码不符合规范'
    return
  }
  
  // 检查是否包含字母和数字
  const hasLetter = /[a-zA-Z]/.test(state.newPassword)
  const hasDigit = /\d/.test(state.newPassword)
  
  if (!hasLetter || !hasDigit) {
    state.passwordError = '密码不符合规范'
    return
  }
  
  emit('updatePassword', state.oldPassword, state.newPassword)
  state.oldPassword = ''
  state.newPassword = ''
}


const handleLogout = () => {
  emit('logout')
}
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <!-- 头部 -->
      <div class="modal-header">
        <button @click="$emit('close')" class="close-btn">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>

        <div class="avatar-section">
          <div v-if="state.error" class="error-msg-modal">{{ state.error }}</div>
          <div class="avatar-container">
            <img
              :src="state.avatarPreview || user?.avatar || '/default-avatar.svg'"
              alt="用户头像"
              class="user-avatar clickable"
              @click="$refs.avatarInput.click()"
            />
            <div class="avatar-overlay" @click="$refs.avatarInput.click()">
              <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path>
                <circle cx="12" cy="13" r="4"></circle>
              </svg>
            </div>
            <input
              type="file"
              ref="avatarInput"
              @change="handleAvatarChange"
              accept="image/*"
              style="display: none"
            />
          </div>
          <button v-if="state.avatarPreview" @click="handleUpdateAvatar" class="save-avatar-btn">
            保存头像
          </button>
        </div>

        <h2 class="user-name">{{ user?.username || '用户' }}</h2>
        <p class="user-phone">{{ user?.phone || '手机用户' }}</p>
      </div>

      <!-- 标签页 -->
      <div class="tab-nav">
        <button
          @click="state.activeTab = 'profile'"
          :class="['tab-btn', { 'tab-active': state.activeTab === 'profile' }]"
        >
          资料设置
        </button>
      </div>

      <!-- 内容区域 -->
      <div class="tab-content">
        <!-- 资料设置 -->
        <div class="profile-tab">
          <div class="form-section">
            <label class="section-label">修改用户名</label>
            <p class="field-hint">用户名长度必须在3-25个字符之间</p>
            <div class="input-group">
              <input
                type="text"
                v-model="state.username"
                class="modal-input"
                :class="{ 'input-error': state.usernameError }"
                placeholder="用户名"
              />
              <button @click="handleUpdateProfile" class="action-btn">保存</button>
            </div>
            <p v-if="state.usernameError" class="error-msg">{{ state.usernameError }}</p>
          </div>

          <div class="form-section">
            <label class="section-label">修改密码</label>
            <p class="field-hint">密码长度必须在6-25个字符之间，需包含字母与数字</p>
            <div class="password-inputs">
              <div class="input-wrapper">
                <input
                  :type="state.showOldPassword ? 'text' : 'password'"
                  v-model="state.oldPassword"
                  class="modal-input"
                  :class="{ 'input-error': state.passwordError }"
                  placeholder="当前密码"
                />
                <button 
                  class="toggle-password" 
                  @click="state.showOldPassword = !state.showOldPassword"
                  type="button"
                  tabindex="-1"
                >
                  <svg v-if="state.showOldPassword" class="icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <svg v-else class="icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </button>
              </div>

              <div class="input-wrapper">
                <input
                  :type="state.showNewPassword ? 'text' : 'password'"
                  v-model="state.newPassword"
                  class="modal-input"
                  :class="{ 'input-error': state.passwordError }"
                  placeholder="新密码"
                />
                <button 
                  class="toggle-password" 
                  @click="state.showNewPassword = !state.showNewPassword"
                  type="button"
                  tabindex="-1"
                >
                  <svg v-if="state.showNewPassword" class="icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                  </svg>
                  <svg v-else class="icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                  </svg>
                </button>
              </div>
              <button @click="handleUpdatePassword" class="action-btn">修改</button>
            </div>
            <p v-if="state.passwordError" class="error-msg">{{ state.passwordError }}</p>
          </div>

          <button @click="handleLogout" class="logout-btn">
            <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16,17 21,12 16,7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            退出登录
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.modal-content {
  background: var(--mt-white);
  border-radius: 1rem;
  width: 100%;
  max-width: 24rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: modal-in 0.3s ease-out;
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-1rem);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  background: var(--mt-primary);
  padding: 1.5rem;
  text-align: center;
  position: relative;
}

.close-btn {
  position: absolute;
  right: 1rem;
  top: 1rem;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 0.25rem;
  transition: var(--mt-transition);
}

.close-btn:hover {
  color: white;
  background: rgba(255, 255, 255, 0.1);
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
}

.user-avatar {
  width: 5rem;
  height: 5rem;
  background: var(--mt-white);
  border-radius: 50%;
  margin: 0 auto 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--mt-primary);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.3);
  object-fit: cover;
  cursor: pointer;
  transition: var(--mt-transition);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 0.5rem;
  margin-bottom: 1rem;
}

.error-msg-modal {
  color: var(--mt-error);
  font-size: 0.875rem;
  background: #FFF0F0;
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  margin-bottom: 0.5rem;
}

.avatar-container {
  position: relative;
  cursor: pointer;
}

.avatar-container:hover .user-avatar {
  transform: scale(1.05);
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  background: var(--mt-accent);
  color: white;
  border-radius: 50%;
  width: 1.75rem;
  height: 1.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--mt-transition);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.avatar-overlay:hover {
  background: var(--mt-primary);
  transform: scale(1.1);
}

.save-avatar-btn {
  background: var(--mt-accent);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--mt-radius);
  font-size: 0.875rem;
  cursor: pointer;
  transition: var(--mt-transition);
  margin-top: 0.5rem;
}

.save-avatar-btn:hover {
  background: var(--mt-primary);
}

.user-name {
  color: var(--mt-white);
  font-weight: 700;
  font-size: 1.125rem;
  margin-bottom: 0.25rem;
}

.user-phone {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.75rem;
}

.tab-nav {
  display: flex;
  border-bottom: 1px solid #E5E7EB;
}

.tab-btn {
  flex: 1;
  padding: 0.75rem;
  background: none;
  border: none;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--mt-text-light);
  cursor: pointer;
  transition: var(--mt-transition);
  border-bottom: 2px solid transparent;
}

.tab-active {
  color: var(--mt-primary);
  border-bottom-color: var(--mt-primary);
}

.tab-content {
  padding: 1.5rem;
}

.form-section {
  margin-bottom: 1rem;
}

.section-label {
  display: block;
  font-size: 0.875rem;
  color: var(--mt-text-main);
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.field-hint {
  font-size: 0.75rem;
  color: #9CA3AF;
  margin: -0.25rem 0 0.75rem 0;
  font-style: italic;
}

.input-group {
  display: flex;
  gap: 0.5rem;
}

.password-inputs {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.password-inputs .action-btn {
  align-self: flex-start;
  margin-top: 0.25rem;
}

.modal-input {
  flex: 1;
  padding: 0.5rem;
  background-color: var(--mt-input-bg);
  border: 1px solid #D1D5DB;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  color: var(--mt-text-main);
  outline: none;
  transition: var(--mt-transition);
}

.modal-input:focus {
  border-color: var(--mt-primary);
  box-shadow: 0 0 0 2px rgba(212, 165, 116, 0.1);
}

.modal-input.input-error {
  border-color: #DC2626;
}

.modal-input.input-error:focus {
  box-shadow: 0 0 0 2px rgba(220, 38, 38, 0.1);
}

.error-msg {
  font-size: 0.75rem;
  color: #DC2626;
  margin: 0.25rem 0 0 0;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.toggle-password {
  position: absolute;
  right: 0.5rem;
  background: none;
  border: none;
  color: #9CA3AF;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: var(--mt-text-main);
}

.icon-sm {
  width: 1rem;
  height: 1rem;
}

.action-btn {
  padding: 0.5rem 0.75rem;
  background-color: var(--mt-text-main);
  color: var(--mt-white);
  border: none;
  border-radius: 0.5rem;
  font-size: 0.75rem;
  cursor: pointer;
  transition: var(--mt-transition);
}

.action-btn:hover {
  background-color: var(--mt-accent);
}

.logout-btn {
  width: 100%;
  margin-top: 1rem;
  padding: 0.5rem;
  border: 1px solid #FCA5A5;
  color: #DC2626;
  background: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  cursor: pointer;
  transition: var(--mt-transition);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.logout-btn:hover {
  background-color: #FEF2F2;
}
</style>
