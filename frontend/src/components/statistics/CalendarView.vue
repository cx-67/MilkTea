<script setup>
import { computed } from 'vue'
import { getDaysInMonth, getFirstDayOfMonth, formatDate } from '../../utils/constants'

const props = defineProps({
  currentDate: Date,
  records: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['dateSelect', 'monthChange'])

const calendarData = computed(() => {
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  const daysInMonth = getDaysInMonth(year, month)
  const firstDay = getFirstDayOfMonth(year, month)

  console.log('📅 Calendar computing:', {
    year,
    month: month + 1,
    totalRecords: props.records.length,
    sampleRecord: props.records[0]
  })

  const days = []
  // 添加前面的空白天数
  for (let i = 0; i < firstDay; i++) {
    days.push(null)
  }

  // 添加当月的天数
  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = formatDate(new Date(year, month, i))
    const dayRecords = props.records.filter(r => {
      // 支持不同的日期字段格式
      const recordDate = r.consumeDate || r.date
      const matches = recordDate === dateStr
      if (matches) {
        console.log(`📅 Date match found for ${dateStr}:`, r)
      }
      return matches
    })

    // 计算总价格和品牌分组
    const totalPrice = dayRecords.reduce((sum, record) => {
      return sum + (record.price || 0)
    }, 0)

    // 按品牌分组记录
    const brandGroups = dayRecords.reduce((groups, record) => {
      const brandId = record.brandId
      const brandName = record.brandName || record.brand
      const brandLogo = record.brandLogo || '/default-brand-icon.png'

      if (!groups[brandId]) {
        groups[brandId] = {
          id: brandId,
          name: brandName,
          logo: brandLogo,
          count: 0
        }
      }
      groups[brandId].count++
      return groups
    }, {})

    days.push({
      day: i,
      date: dateStr,
      records: dayRecords,
      count: dayRecords.length,
      amount: totalPrice,
      hasConsumption: dayRecords.length > 0,
      brandGroups: Object.values(brandGroups)
    })
  }

  return days
})

const monthName = computed(() => {
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth() + 1
  return `${year}年${month}月`
})

const monthlyTotal = computed(() => {
  const year = props.currentDate.getFullYear()
  const month = props.currentDate.getMonth()
  
  return props.records
    .filter(r => {
      const d = new Date(r.consumeDate || r.date)
      return d.getFullYear() === year && d.getMonth() === month
    })
    .reduce((sum, r) => sum + (Number(r.price) || 0), 0)
})

const goToPreviousMonth = () => {
  const newDate = new Date(props.currentDate)
  newDate.setMonth(newDate.getMonth() - 1)
  emit('monthChange', newDate)
}

const goToNextMonth = () => {
  const newDate = new Date(props.currentDate)
  newDate.setMonth(newDate.getMonth() + 1)
  emit('monthChange', newDate)
}

const handleDateClick = (dayData) => {
  if (dayData && dayData.records.length > 0) {
    emit('dateSelect', dayData)
  }
}
</script>

<template>
  <div class="calendar-view">
    <!-- 日历网格 -->
    <div class="calendar-grid">
      <!-- 星期标题 -->
      <div class="weekday-header">
        <div v-for="day in ['日', '一', '二', '三', '四', '五', '六']"
             :key="day"
             class="weekday"
        >
          {{ day }}
        </div>
      </div>

      <!-- 日期网格 -->
      <div class="days-grid">
        <div
          v-for="(item, index) in calendarData"
          :key="index"
          class="day-cell"
          :class="{ 'has-records': item?.records.length > 0 }"
          @click="handleDateClick(item)"
        >
          <div v-if="item" class="day-content">
            <!-- 背景Logo -->
            <div v-if="item.brandGroups.length > 0" class="brand-bg">
              <img 
                :src="item.brandGroups[0].logo || '/default-brand-icon.png'" 
                class="brand-bg-img"
              />
              <div v-if="item.brandGroups.length > 1" class="brand-count-badge">
                +{{ item.brandGroups.length - 1 }}
              </div>
            </div>

            <span class="day-number">{{ item.day }}</span>

            <!-- 总价格 -->
            <div v-if="item.records.length > 0" class="total-price">
              ¥{{ item.records.reduce((sum, r) => sum + (r.price || 0), 0).toFixed(1) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.calendar-view {
  background: var(--mt-white);
  border-radius: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-shrink: 0;
}

.month-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--mt-text-main);
  margin: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.monthly-total {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
  background: rgba(212, 165, 116, 0.1);
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
}

.total-label {
  font-size: 0.75rem;
  color: var(--mt-text-light);
}

.total-amount {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--mt-primary);
}

.nav-buttons {
  display: flex;
  gap: 0.25rem;
}

.nav-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  border-radius: 0.5rem;
  color: var(--mt-text-light);
  transition: var(--mt-transition);
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover {
  background-color: var(--mt-input-bg);
  color: var(--mt-text-main);
}

.icon {
  width: 1.25rem;
  height: 1.25rem;
}

.calendar-grid {
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.weekday-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 0.5rem;
  flex-shrink: 0;
}

.weekday {
  text-align: center;
  font-size: 0.875rem;
  color: var(--mt-text-light);
  font-weight: 500;
  padding: 0.5rem;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.25rem;
  flex: 1;
  grid-auto-rows: 1fr;
}

.day-cell {
  aspect-ratio: 1;
  border-radius: 0.75rem;
  transition: var(--mt-transition);
  cursor: pointer;
  background: var(--mt-input-bg);
  border: 1px solid transparent;
  min-height: 0;
}

.day-cell.has-records {
  background: rgba(212, 165, 116, 0.1);
  border-color: rgba(212, 165, 116, 0.2);
}

.day-cell.has-records:hover {
  background: rgba(212, 165, 116, 0.15);
  border-color: var(--mt-primary);
  box-shadow: 0 2px 8px rgba(212, 165, 116, 0.2);
}

.day-content {
  padding: 0.5rem;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.brand-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  opacity: 0.8;
  padding: 4px;
  box-sizing: border-box;
}

.brand-bg-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 0.5rem;
}

.brand-count-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: rgba(0,0,0,0.5);
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 4px;
}

.day-number {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--mt-text-light);
  position: relative;
  z-index: 2;
}

.has-records .day-number {
  color: var(--mt-primary);
  font-weight: 700;
  text-shadow: 0 0 3px rgba(255,255,255,0.9);
}

.total-price {
  position: absolute;
  bottom: 0.25rem;
  right: 0.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--mt-accent);
  background: rgba(255,255,255,0.8);
  padding: 0 4px;
  border-radius: 4px;
  backdrop-filter: blur(2px);
  z-index: 2;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .calendar-view {
    padding: 1.5rem;
  }

  .month-title {
    font-size: 1.75rem;
  }

  .day-cell {
    aspect-ratio: auto;
    height: auto;
  }
}

@media (max-width: 640px) {
  .day-content {
    padding: 0.25rem;
  }

  .day-number {
    font-size: 0.75rem;
  }

  .total-price {
    font-size: 0.625rem;
  }
}
</style>
