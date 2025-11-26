<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  }
})

// 模态框状态
const showRecordModal = ref(false)
const showBrandModal = ref(false)
const selectedRecord = ref(null)
const selectedBrandName = ref('')

// 获取选中品牌的记录列表
const selectedBrandRecords = computed(() => {
  if (!selectedBrandName.value) return []
  return props.records
    .filter(r => (r.brandName || r.brand) === selectedBrandName.value)
    .sort((a, b) => new Date(b.consumeDate || b.date) - new Date(a.consumeDate || a.date))
})

// 打开记录详情
const openRecordDetail = (record) => {
  selectedRecord.value = record
  showRecordModal.value = true
}

// 打开评分榜单详情（找到该品类最高分的一条记录）
const openTopRatingDetail = (item) => {
  const targetRecords = props.records.filter(r => 
    (r.brandName || r.brand) === item.brand && 
    r.category === item.category
  )
  
  if (targetRecords.length > 0) {
    // 找评分最高的，如果评分一样，找最近的
    targetRecords.sort((a, b) => {
      const scoreA = a.rating !== undefined ? a.rating : (a.score !== undefined ? a.score : 0)
      const scoreB = b.rating !== undefined ? b.rating : (b.score !== undefined ? b.score : 0)
      if (scoreB !== scoreA) return scoreB - scoreA
      return new Date(b.consumeDate || b.date) - new Date(a.consumeDate || a.date)
    })
    openRecordDetail(targetRecords[0])
  }
}

// 打开品牌记录列表
const openBrandList = (brandName) => {
  selectedBrandName.value = brandName
  showBrandModal.value = true
}

// 计算统计数据
const stats = computed(() => {
  const totalCups = props.records.length
  const totalCost = props.records.reduce((sum, record) => sum + (record.price || 0), 0)
  const avgPrice = totalCups > 0 ? totalCost / totalCups : 0

  // 评分统计 Top 5
  const ratingMap = {}
  props.records.forEach(record => {
    const brand = record.brandName || record.brand
    const category = record.category
    if (brand && category) {
      const key = `${brand}-${category}`
      if (!ratingMap[key]) {
        ratingMap[key] = {
          totalRating: 0,
          count: 0,
          brand,
          category,
          logo: record.brandLogo
        }
      }
      const rating = record.rating !== undefined ? record.rating : (record.score !== undefined ? record.score : 0)
      ratingMap[key].totalRating += rating
      ratingMap[key].count += 1
    }
  })

  const topRatings = Object.values(ratingMap)
    .map(item => ({
      ...item,
      avgRating: item.totalRating / item.count
    }))
    .sort((a, b) => b.avgRating - a.avgRating)
    .slice(0, 5)

  // 购买次数统计 Top 5
  const purchaseMap = {}
  props.records.forEach(record => {
    const brand = record.brandName || record.brand
    if (brand) {
      if (!purchaseMap[brand]) {
        purchaseMap[brand] = {
          count: 0,
          brand,
          logo: record.brandLogo
        }
      }
      purchaseMap[brand].count += 1
    }
  })

  const topPurchaseCounts = Object.values(purchaseMap)
    .sort((a, b) => b.count - a.count)
    .slice(0, 5)

  // 最近记录（按日期排序）
  const recentRecords = [...props.records]
    .sort((a, b) => {
      const dateA = new Date(a.consumeDate || a.date)
      const dateB = new Date(b.consumeDate || b.date)
      return dateB - dateA
    })
    .slice(0, 5)

  return {
    totalCups,
    totalCost,
    avgPrice,
    topRatings,
    topPurchaseCounts,
    recentRecords
  }
})

const maxCount = computed(() => {
  return stats.value.topBrands.length > 0 ? stats.value.topBrands[0][1] : 1
})
</script>

<template>
  <div class="stats-summary">
    <h2 class="section-title">消费统计</h2>

    <!-- 概览卡片 -->
    <div class="overview-cards">
      <div class="stat-card cost-card">
        <div class="stat-icon">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="1" x2="12" y2="23"></line>
            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-label">累计消费</p>
          <p class="stat-value cost-value">¥{{ stats.totalCost.toFixed(1) }}</p>
        </div>
      </div>

      <div class="stat-card cups-card">
        <div class="stat-icon">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 11h14a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2z"></path>
            <path d="M7 7V3a1 1 0 0 1 1-1h8a1 1 0 0 1 1 1v4"></path>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-label">总杯数</p>
          <p class="stat-value cups-value">{{ stats.totalCups }}<span class="stat-unit">杯</span></p>
        </div>
      </div>

      <div class="stat-card avg-card">
        <div class="stat-icon">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="20" x2="18" y2="10"></line>
            <line x1="12" y1="20" x2="12" y2="4"></line>
            <line x1="6" y1="20" x2="6" y2="14"></line>
          </svg>
        </div>
        <div class="stat-content">
          <p class="stat-label">平均单价</p>
          <p class="stat-value avg-value">¥{{ stats.avgPrice.toFixed(1) }}</p>
        </div>
      </div>
    </div>

    <!-- 排行榜 -->
    <div class="rankings-container">
      <!-- 评分 Top 5 -->
      <div class="ranking-card">
        <h3 class="subsection-title">
          <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
          评分 Top 5
        </h3>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in stats.topRatings" 
            :key="index" 
            class="ranking-item clickable"
            @click="openTopRatingDetail(item)"
          >
            <div class="ranking-info">
              <img :src="item.logo || '/default-brand-icon.png'" class="brand-logo-sm" alt="logo" />
              <span class="ranking-name">{{ item.brand }} - {{ item.category }}</span>
            </div>
            <span class="ranking-value score-value">{{ item.avgRating.toFixed(1) }}</span>
          </div>
          <p v-if="stats.topRatings.length === 0" class="empty-state">暂无数据</p>
        </div>
      </div>

      <!-- 购买次数 Top 5 -->
      <div class="ranking-card">
        <h3 class="subsection-title">
          <svg class="title-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
            <line x1="3" y1="6" x2="21" y2="6"></line>
            <path d="M16 10a4 4 0 0 1-8 0"></path>
          </svg>
          购买次数 Top 5
        </h3>
        <div class="ranking-list">
          <div 
            v-for="(item, index) in stats.topPurchaseCounts" 
            :key="index" 
            class="ranking-item clickable"
            @click="openBrandList(item.brand)"
          >
            <div class="ranking-info">
              <img :src="item.logo || '/default-brand-icon.png'" class="brand-logo-sm" alt="logo" />
              <span class="ranking-name">{{ item.brand }}</span>
            </div>
            <span class="ranking-value count-value">{{ item.count }}</span>
          </div>
          <p v-if="stats.topPurchaseCounts.length === 0" class="empty-state">暂无数据</p>
        </div>
      </div>
    </div>

    <!-- 近期记录 -->
    <div class="recent-records">
      <h3 class="subsection-title">近期记录</h3>
      <div class="records-table">
        <table v-if="stats.recentRecords.length > 0">
          <thead>
            <tr>
              <th style="width: 15%">日期</th>
              <th style="width: 30%">品牌</th>
              <th style="width: 15%">金额</th>
              <th style="width: 40%">评价</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="record in stats.recentRecords" 
              :key="record.id" 
              class="clickable-row"
              @click="openRecordDetail(record)"
            >
              <td class="record-date">{{ (record.consumeDate || record.date).slice(5) }}</td>
              <td class="record-brand">
                <div class="brand-cell">
                  <img :src="record.brandLogo || '/default-brand-icon.png'" class="brand-logo-xs" alt="logo" />
                  <span>{{ record.brandName || record.brand }} - {{ record.category }}</span>
                </div>
              </td>
              <td class="record-price">¥{{ record.price }}</td>
              <td class="record-comment">
                <span v-if="record.comment" class="comment-text">{{ record.comment }}</span>
                <span v-else class="no-comment">暂无评语</span>
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty-state">暂无记录</p>
      </div>
    </div>

    <!-- 品牌记录列表模态框 -->
    <div v-if="showBrandModal" class="modal-overlay" @click="showBrandModal = false">
      <div class="modal-card brand-list-card" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedBrandName }} - 消费记录</h3>
          <button class="close-btn" @click="showBrandModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="records-table-container">
            <table>
              <thead>
                <tr>
                  <th>日期</th>
                  <th>品类名称</th>
                  <th class="text-right">金额</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="record in selectedBrandRecords" 
                  :key="record.id"
                  class="clickable-row"
                  @click="openRecordDetail(record)"
                >
                  <td class="record-date">{{ (record.consumeDate || record.date).slice(5) }}</td>
                  <td class="record-brand">{{ record.category }}</td>
                  <td class="record-price">¥{{ record.price }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 记录详情模态框 (z-index higher to show on top) -->
    <div v-if="showRecordModal && selectedRecord" class="modal-overlay" style="z-index: 110;" @click="showRecordModal = false">
      <div class="modal-card record-detail-card" @click.stop>
        <div class="card-header">
          <div class="brand-info-large">
            <img :src="selectedRecord.brandLogo || '/default-brand-icon.png'" class="brand-logo-lg" alt="logo" />
            <div class="brand-text">
              <h3 class="detail-brand-name">{{ selectedRecord.brandName || selectedRecord.brand }} | {{ selectedRecord.category }}</h3>
              <p class="detail-specs">
                {{ selectedRecord.sweetness || selectedRecord.sugar }} · 
                {{ selectedRecord.iceLevel || selectedRecord.ice }} · 
                {{ selectedRecord.rating || selectedRecord.score }}分
              </p>
            </div>
          </div>
          <div class="detail-price">¥{{ selectedRecord.price }}</div>
        </div>
        
        <div class="card-body">
          <div class="detail-comment-box">
            <p v-if="selectedRecord.comment" class="detail-comment">"{{ selectedRecord.comment }}"</p>
            <p v-else class="detail-no-comment">"暂无评语"</p>
          </div>
          <div class="detail-date">
            消费于 {{ selectedRecord.consumeDate || selectedRecord.date }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-summary {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  animation: fade-in 0.5s ease-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(1rem);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--mt-text-main);
  margin: 0;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: var(--mt-white);
  padding: 1rem;
  border-radius: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  transition: var(--mt-transition);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cost-card {
  border-left: 4px solid var(--mt-primary);
}

.cups-card {
  border-left: 4px solid #3B82F6;
}

.avg-card {
  border-left: 4px solid #8B5CF6;
}

.stat-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.cost-card .stat-icon {
  background: rgba(212, 165, 116, 0.1);
  color: var(--mt-primary);
}

.cups-card .stat-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.avg-card .stat-icon {
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--mt-text-light);
  margin: 0 0 0.25rem 0;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.cost-value {
  color: var(--mt-primary);
}

.cups-value {
  color: #3B82F6;
}

.avg-value {
  color: #8B5CF6;
}

.stat-unit {
  font-size: 0.75rem;
  color: var(--mt-text-light);
  font-weight: 400;
}

.recent-records {
  background: var(--mt-white);
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.rankings-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.ranking-card {
  background: var(--mt-white);
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.subsection-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--mt-text-main);
  margin: 0 0 1rem 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.title-icon {
  width: 1.125rem;
  height: 1.125rem;
  color: var(--mt-primary);
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ranking-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem;
  border-radius: 0.5rem;
  background: var(--mt-input-bg);
  transition: var(--mt-transition);
}

.ranking-item.clickable {
  cursor: pointer;
}

.ranking-item.clickable:hover {
  background: rgba(212, 165, 116, 0.2);
  transform: translateX(4px);
}

.ranking-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
}

.brand-logo-sm {
  width: 2rem;
  height: 2rem;
  border-radius: 0.25rem;
  object-fit: cover;
  flex-shrink: 0;
  background-color: #fff;
}

.brand-logo-xs {
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 0.25rem;
  object-fit: cover;
  flex-shrink: 0;
  background-color: #fff;
}

.brand-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.ranking-name {
  font-size: 0.875rem;
  color: var(--mt-text-main);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-value {
  font-weight: 700;
  font-size: 0.875rem;
  flex-shrink: 0;
  margin-left: 0.5rem;
}

.score-value {
  color: #F59E0B;
}

.count-value {
  color: var(--mt-primary);
}

.records-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead th {
  text-align: left;
  padding: 0.75rem 1rem;
  background: var(--mt-input-bg);
  color: var(--mt-text-main);
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.025em;
  font-weight: 600;
}

thead th:first-child {
  border-radius: 0.5rem 0 0 0.5rem;
}

thead th:last-child {
  border-radius: 0 0.5rem 0.5rem 0;
}

.text-right {
  text-align: right;
}

tbody td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #F3F4F6;
}

.clickable-row {
  cursor: pointer;
  transition: background-color 0.2s;
}

.clickable-row:hover {
  background: var(--mt-input-bg);
}

.record-date {
  color: var(--mt-text-light);
  font-size: 0.875rem;
  white-space: nowrap;
}

.record-brand {
  color: var(--mt-text-main);
  font-weight: 500;
  font-size: 0.875rem;
}

.record-comment {
  font-size: 0.875rem;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.comment-text {
  color: var(--mt-text-main);
}

.no-comment {
  color: var(--mt-text-light);
  font-style: italic;
  font-size: 0.75rem;
}

.record-price {
  color: var(--mt-primary);
  font-weight: 600;
}

.empty-state {
  text-align: center;
  color: var(--mt-text-light);
  font-size: 0.875rem;
  padding: 1rem;
  margin: 0;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  animation: fade-in 0.2s ease-out;
}

.modal-card {
  background: var(--mt-white);
  border-radius: 1rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  animation: modal-in 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  max-width: 90vw;
  width: 100%;
}

.record-detail-card {
  width: 320px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.brand-info-large {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.brand-logo-lg {
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 0.5rem;
  object-fit: cover;
  background-color: #fff;
  border: 1px solid var(--mt-input-bg);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-brand-name {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--mt-text-main);
  margin: 0;
}

.detail-specs {
  font-size: 0.875rem;
  color: var(--mt-text-light);
  margin: 0;
}

.detail-price {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--mt-primary);
}

.detail-comment-box {
  background: var(--mt-input-bg);
  padding: 1rem;
  border-radius: 0.75rem;
  margin-bottom: 0.75rem;
}

.detail-comment {
  font-size: 0.95rem;
  color: var(--mt-text-main);
  font-style: italic;
  margin: 0;
  line-height: 1.5;
}

.detail-no-comment {
  font-size: 0.875rem;
  color: var(--mt-text-light);
  font-style: italic;
  margin: 0;
}

.detail-date {
  font-size: 0.75rem;
  color: var(--mt-text-light);
  text-align: right;
}

.brand-list-card {
  width: 500px;
  display: flex;
  flex-direction: column;
  max-height: 80vh;
}

.modal-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--mt-input-bg);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.125rem;
  color: var(--mt-text-main);
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--mt-text-light);
  cursor: pointer;
  padding: 0 0.5rem;
}

.modal-body {
  padding: 1rem;
  overflow-y: auto;
}

.records-table-container {
  border-radius: 0.5rem;
  overflow: hidden;
  border: 1px solid var(--mt-input-bg);
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(1rem);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }

  .rankings-container {
    grid-template-columns: 1fr;
  }

  .stat-value {
    font-size: 1.25rem;
  }

  .ranking-card, .recent-records {
    padding: 1rem;
  }
  
  .record-comment {
    display: none; /* 移动端隐藏评语列以节省空间 */
  }
  
  thead th:nth-child(4) {
    display: none;
  }
}
</style>

<style scoped>

</style>