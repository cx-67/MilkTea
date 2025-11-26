<script setup>
import { reactive, watch, computed, ref } from 'vue'
import { DEFAULT_BRANDS, SUGAR_LEVELS, ICE_LEVELS, formatDate } from '../../utils/constants'

const props = defineProps({
  initialData: Object,
  availableBrands: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['save', 'addBrand'])

const validationError = ref('')

const form = reactive({
  brandId: null,    // 存储选中的品牌ID
  brand: '',        // 存储品牌名称（用于显示）
  category: '',
  sugar: '半糖',
  ice: '少冰',
  price: '',
  score: 8,
  comment: '',
  date: formatDate(new Date())
})

const isAddingBrand = reactive({ value: false })
const customBrand = reactive({
  value: '',
  imageFile: null,
  imagePreview: '',
  uploading: false,
  error: ''
})

// 合并默认品牌和用户自定义品牌
const allBrands = computed(() => {
  // 只使用从API获取的品牌，确保都有有效的ID
  return Array.isArray(props.availableBrands) ? props.availableBrands : []
})

// 当传入初始数据时，填充表单
watch(() => props.initialData, (newData) => {
  if (newData) {
    // 处理字段映射
    form.brandId = newData.brandId
    form.brand = newData.brandName || newData.brand
    form.category = newData.category
    form.sugar = newData.sweetness || newData.sugar
    form.ice = newData.iceLevel || newData.ice
    form.price = newData.price
    form.score = newData.rating !== undefined ? newData.rating : (newData.score !== undefined ? newData.score : 8)
    form.comment = newData.comment || ''
    form.date = newData.consumeDate || newData.date || formatDate(new Date())
  } else {
    // 重置表单
    Object.assign(form, {
      brandId: null,
      brand: '',
      category: '',
      sugar: '半糖',
      ice: '少冰',
      price: '',
      score: 8,
      comment: '',
      date: formatDate(new Date())
    })
  }
}, { immediate: true })

const handleSave = () => {
  validationError.value = ''
  
  if (!form.brandId) {
    validationError.value = '请选择品牌'
    return
  }

  if (!form.category || !form.category.trim()) {
    validationError.value = '请输入品类名称'
    return
  }
  
  if (!form.price) {
    validationError.value = '请输入价格'
    return
  }

  // 转换为API格式
  const recordData = {
    brandId: form.brandId,
    category: form.category,
    sweetness: form.sugar,     // 前端sugar -> 后端sweetness
    iceLevel: form.ice,        // 前端ice -> 后端iceLevel
    price: typeof form.price === 'string' ? parseFloat(form.price) || 0 : form.price,
    rating: parseInt(form.score),  // 前端score -> 后端rating
    comment: form.comment || '',
    consumeDate: form.date     // 前端date -> 后端consumeDate
  }

  emit('save', recordData)
}

const selectBrand = (brand) => {
  if (brand && brand.id) {
    form.brandId = brand.id
    form.brand = brand.name
  } else {
    console.warn('尝试选择没有ID的品牌:', brand)
  }
}

const handleImageChange = (event) => {
  customBrand.error = ''
  const file = event.target.files[0]
  if (file) {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      customBrand.error = '请选择图片文件'
      return
    }

    // 验证文件大小 (2MB)
    if (file.size > 2 * 1024 * 1024) {
      customBrand.error = '图片大小不能超过2MB'
      return
    }

    customBrand.imageFile = file

    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      customBrand.imagePreview = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

const handleConfirmAddBrand = async () => {
  customBrand.error = ''
  if (customBrand.value.trim()) {
    customBrand.uploading = true

    try {
      let logoUrl = null

      // 如果用户选择了图片，直接使用Base64数据
      if (customBrand.imagePreview) {
        logoUrl = customBrand.imagePreview
        console.log(`📤 品牌图标使用Base64数据`)
      }

      const brandData = {
        name: customBrand.value.trim(),
        description: '用户自定义品牌',
        logoUrl: logoUrl
      }

      console.log(`📤 添加品牌: ${brandData.name}`, brandData)

      emit('addBrand', brandData)

      // 重置表单
      isAddingBrand.value = false
      customBrand.value = ''
      customBrand.imageFile = null
      customBrand.imagePreview = ''
      customBrand.error = ''

    } catch (error) {
      console.error('添加品牌失败:', error)
      customBrand.error = '添加品牌失败: ' + (error.message || '未知错误')
    } finally {
      customBrand.uploading = false
    }
  }
}

const cancelAddBrand = () => {
  isAddingBrand.value = false
  customBrand.value = ''
  customBrand.imageFile = null
  customBrand.imagePreview = ''
  customBrand.uploading = false
  customBrand.error = ''
}
</script>

<template>
  <div class="record-form">
    <div class="form-grid">
      <div class="form-group">
        <label class="form-label">日期</label>
        <input
          type="date"
          v-model="form.date"
          class="form-input"
        />
      </div>
      <div class="form-group">
        <label class="form-label">价格 (元)</label>
        <div class="price-input-wrapper">
          <span class="price-symbol">¥</span>
          <input
            type="number"
            v-model="form.price"
            class="form-input price-input"
            placeholder="0.0"
            step="0.1"
            min="0"
          />
        </div>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">品牌</label>
      <div class="brand-grid">
        <button
          v-for="brand in allBrands"
          :key="brand.id || brand.name"
          type="button"
          @click="selectBrand(brand)"
          class="brand-icon-btn"
          :class="{ 'brand-icon-active': form.brandId === brand.id }"
          :title="brand.name"
        >
          <img
            :src="brand.logoUrl || '/default-brand-icon.png'"
            :alt="brand.name"
            class="brand-icon"
          />
          <span class="brand-name">{{ brand.name }}</span>
        </button>

        <!-- 添加品牌UI -->
        <div v-if="isAddingBrand.value" class="add-brand-inline">
          <div class="add-brand-form">
            <div v-if="customBrand.error" class="error-msg-inline">{{ customBrand.error }}</div>
            <input
              type="text"
              v-model="customBrand.value"
              class="add-brand-input"
              placeholder="输入品牌名称"
              @keydown.enter="handleConfirmAddBrand"
              autofocus
            />

            <!-- 图片上传区域 -->
            <div class="image-upload-section">
              <label class="upload-label">品牌图标 (可选)</label>
              <div class="image-upload-area">
                <input
                  type="file"
                  ref="imageInput"
                  @change="handleImageChange"
                  accept="image/*"
                  style="display: none"
                />

                <div v-if="customBrand.imagePreview" class="image-preview">
                  <img :src="customBrand.imagePreview" alt="品牌图标预览" class="preview-img" />
                  <button @click="customBrand.imagePreview = ''; customBrand.imageFile = null" class="remove-image-btn" type="button">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                </div>

                <button
                  v-else
                  @click="$refs.imageInput.click()"
                  class="upload-btn"
                  type="button"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17,8 12,3 7,8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                  <span>选择图片</span>
                </button>
              </div>
              <p class="upload-hint">支持 JPG、PNG 格式，最大 2MB</p>
            </div>

            <div class="add-brand-actions">
              <button
                @click="handleConfirmAddBrand"
                class="confirm-btn"
                :disabled="!customBrand.value.trim() || customBrand.uploading"
              >
                <svg v-if="customBrand.uploading" class="icon spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="m9 12 2 2 4-4"></path>
                  <circle cx="12" cy="12" r="10"></circle>
                </svg>
                {{ customBrand.uploading ? '上传中...' : '确认添加' }}
              </button>
              <button
                @click="cancelAddBrand"
                class="cancel-btn"
                :disabled="customBrand.uploading"
              >
                <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                取消
              </button>
            </div>
          </div>
        </div>

        <button
          v-else
          type="button"
          @click="isAddingBrand.value = true"
          class="add-brand-btn"
        >
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          添加品牌
        </button>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">品类名称</label>
      <input
        type="text"
        v-model="form.category"
        class="form-input"
        placeholder="例如：多肉葡萄"
      />
    </div>

    <div class="form-grid">
      <div class="form-group">
        <label class="form-label">甜度</label>
        <select v-model="form.sugar" class="form-select">
          <option v-for="level in SUGAR_LEVELS" :key="level" :value="level">
            {{ level }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">冰度</label>
        <select v-model="form.ice" class="form-select">
          <option v-for="level in ICE_LEVELS" :key="level" :value="level">
            {{ level }}
          </option>
        </select>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">评分: {{ form.score }}</label>
      <input
        type="range"
        min="0"
        max="10"
        v-model="form.score"
        class="score-slider"
      />
      <div class="score-labels">
        <span>难喝</span>
        <span>一般</span>
        <span>好喝</span>
        <span>神作</span>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">评语 (可选)</label>
      <textarea
        v-model="form.comment"
        class="form-textarea"
        placeholder="记录此刻的感受..."
      ></textarea>
    </div>

    <div v-if="validationError" class="form-error-msg">{{ validationError }}</div>

    <button
      @click="handleSave"
      class="save-btn"
    >
      {{ initialData ? '保存修改' : '确认添加' }}
    </button>
  </div>
</template>

<style scoped>
.form-error-msg {
  color: var(--mt-error);
  font-size: 0.875rem;
  text-align: center;
  background: #FFF0F0;
  padding: 0.5rem;
  border-radius: 0.5rem;
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}

.record-form {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--mt-text-light);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.form-input, .form-select, .form-textarea {
  padding: 0.5rem;
  background-color: var(--mt-input-bg);
  border: 1px solid transparent;
  border-radius: var(--mt-radius);
  font-size: 0.875rem;
  color: var(--mt-text-main);
  outline: none;
  transition: var(--mt-transition);
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  background-color: var(--mt-white);
  border-color: var(--mt-primary);
  box-shadow: 0 0 0 2px rgba(212, 165, 116, 0.1);
}

.price-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.price-symbol {
  position: absolute;
  left: 0.5rem;
  color: var(--mt-text-light);
  font-size: 0.875rem;
}

.price-input {
  padding-left: 1.75rem;
}

.form-textarea {
  min-height: 5rem;
  resize: vertical;
}

.brand-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.brand-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.75rem 0.5rem;
  border: 2px solid var(--mt-border);
  background: var(--mt-white);
  border-radius: var(--mt-radius);
  cursor: pointer;
  transition: var(--mt-transition);
  min-height: 80px;
}

.brand-icon-btn:hover {
  border-color: var(--mt-primary);
  background: var(--mt-primary-light);
}

.brand-icon-active {
  border-color: var(--mt-primary);
  background: var(--mt-primary-light);
  box-shadow: 0 4px 12px rgba(212, 165, 116, 0.5);
  transform: translateY(-2px);
  border-width: 2px;
}

.brand-icon-active .brand-name {
  font-weight: 700;
  color: var(--mt-primary);
}

.brand-icon {
  width: 32px;
  height: 32px;
  object-fit: cover;
  border-radius: 4px;
}

.brand-name {
  font-size: 0.75rem;
  text-align: center;
  color: var(--mt-text-main);
  line-height: 1.2;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.add-brand-inline {
  grid-column: span 2;
  min-height: auto;
}

.add-brand-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1rem;
  border: 2px dashed var(--mt-primary);
  border-radius: var(--mt-radius);
  background: var(--mt-primary-light);
}

.error-msg-inline {
  color: var(--mt-error);
  font-size: 0.875rem;
  background: #FFF0F0;
  padding: 0.5rem;
  border-radius: 0.5rem;
}

.image-upload-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.upload-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--mt-text-light);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.image-upload-area {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80px;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.preview-img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: var(--mt-radius);
  border: 2px solid var(--mt-border);
}

.remove-image-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 20px;
  height: 20px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.remove-image-btn:hover {
  background: #dc2626;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  border: 2px dashed var(--mt-border);
  background: var(--mt-white);
  border-radius: var(--mt-radius);
  cursor: pointer;
  transition: var(--mt-transition);
  color: var(--mt-text-light);
  min-width: 120px;
}

.upload-btn:hover {
  border-color: var(--mt-primary);
  color: var(--mt-primary);
  background: var(--mt-primary-light);
}

.upload-btn svg {
  width: 24px;
  height: 24px;
}

.upload-hint {
  margin: 0;
  font-size: 0.75rem;
  color: var(--mt-text-light);
  text-align: center;
  font-style: italic;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.add-brand-actions {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.confirm-btn,
.cancel-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--mt-radius);
  font-size: 0.875rem;
  cursor: pointer;
  transition: var(--mt-transition);
}

.confirm-btn {
  background: var(--mt-primary);
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background: var(--mt-primary-dark);
}

.confirm-btn:disabled {
  background: var(--mt-input-bg);
  color: var(--mt-text-light);
  cursor: not-allowed;
}

.cancel-btn {
  background: var(--mt-input-bg);
  color: var(--mt-text-light);
}

.cancel-btn:hover {
  background: var(--mt-border);
}

.add-brand-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  padding: 0.75rem 0.5rem;
  border: 2px dashed var(--mt-primary);
  background: var(--mt-white);
  border-radius: var(--mt-radius);
  cursor: pointer;
  transition: var(--mt-transition);
  color: var(--mt-primary);
  font-size: 0.75rem;
  min-height: 80px;
}

.add-brand-btn:hover {
  border-color: var(--mt-primary-dark);
  background: var(--mt-primary-light);
}

.add-brand-inline {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  animation: fade-in 0.2s ease;
}

.add-brand-input {
  width: 6rem;
  padding: 0.375rem;
  font-size: 0.75rem;
  border: 1px solid var(--mt-primary);
  border-radius: 4px;
  background-color: rgba(212, 165, 116, 0.05);
  outline: none;
}

.add-brand-input:focus {
  box-shadow: 0 0 0 2px rgba(212, 165, 116, 0.2);
}

.confirm-btn, .cancel-btn {
  padding: 0.375rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: var(--mt-transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-btn {
  background-color: var(--mt-primary);
  color: var(--mt-white);
}

.confirm-btn:hover {
  background-color: var(--mt-primary-dark);
}

.cancel-btn {
  background-color: var(--mt-text-light);
  color: var(--mt-white);
}

.cancel-btn:hover {
  background-color: var(--mt-text-main);
}

.icon {
  width: 0.75rem;
  height: 0.75rem;
}

.score-slider {
  width: 100%;
  height: 0.5rem;
  border-radius: 0.25rem;
  background: var(--mt-input-bg);
  outline: none;
  -webkit-appearance: none;
  appearance: none;
}

.score-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  background: var(--mt-primary);
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(212, 165, 116, 0.3);
}

.score-slider::-moz-range-thumb {
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  background: var(--mt-primary);
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 4px rgba(212, 165, 116, 0.3);
}

.score-labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--mt-text-light);
  margin-top: 0.25rem;
}

.save-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: var(--mt-primary);
  color: var(--mt-white);
  border: none;
  border-radius: var(--mt-radius);
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: var(--mt-transition);
  box-shadow: 0 4px 12px rgba(212, 165, 116, 0.3);
}

.save-btn:hover:not(:disabled) {
  background-color: var(--mt-primary-dark);
  transform: translateY(-1px);
}

.save-btn:disabled {
  background-color: var(--mt-text-light);
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

@keyframes fade-in {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>

