<template>
  <div class="calendar-container">
    <FullCalendar :options="calendarOptions" />

    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h3>📅 申请预约</h3>
        <p>时间：{{ selectedInfo.startStr }} 至 {{ selectedInfo.endStr }}</p>
        
        <div class="form-group">
          <label>选择实验室：</label>
          <select v-model="form.labId">
            <option v-for="lab in labs" :key="lab.id" :value="lab.id">
              {{ lab.name }} (容量:{{ lab.capacity }})
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>实验内容：</label>
          <input v-model="form.reason" placeholder="请输入实验用途" />
        </div>

        <div class="form-group">
          <label>重复预约：</label>
          <select v-model="form.repeatWeeks">
            <option :value="0">仅一次</option>
            <option :value="1">重复 1 周</option>
            <option :value="2">重复 2 周</option>
            <option :value="3">重复 3 周</option>
            <option :value="4">重复 4 周 (一个月)</option>
          </select>
          <small v-if="form.repeatWeeks > 0">
            ⚠️ 将自动预约未来 {{ form.repeatWeeks }} 周的同一时段
          </small>
        </div>

        <div class="actions">
          <button @click="closeModal" class="cancel">取消</button>
          <button @click="submitReservation" class="confirm">提交申请</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

// 状态管理
const showModal = ref(false)
const labs = ref([])
const selectedInfo = ref({})
const form = reactive({
  labId: null,
  reason: '',
  repeatWeeks: 0
})

// 加载实验室列表供选择
onMounted(async () => {
  try {
    const res = await axios.get('/api/labs/list')
    labs.value = res.data
    if (labs.value.length > 0) form.labId = labs.value[0].id
  } catch (e) {
    console.error("加载实验室失败")
  }
})

// 提交逻辑 (含批量处理)
const submitReservation = async () => {
  if (!form.reason) return alert("请填写实验内容")
  
  const user = JSON.parse(localStorage.getItem('user'))
  const reservations = []
  
  // 核心：前端计算批量日期
  for (let i = 0; i <= form.repeatWeeks; i++) {
    // 计算偏移毫秒数 (i * 7天 * 24小时...)
    const offset = i * 7 * 24 * 60 * 60 * 1000
    
    // 基于原始选择时间，加上偏移量
    // 注意：FullCalendar 返回的是 ISO 字符串，可以直接解析
    const baseStart = new Date(selectedInfo.value.startStr).getTime()
    const baseEnd = new Date(selectedInfo.value.endStr).getTime()

    // 转换回 ISO 格式发送给后端 (注意时区处理，简单起见这里用本地时间转 ISO)
    // 实际项目中建议使用 moment.js 或 dayjs，这里用原生简化处理
    const newStart = new Date(baseStart + offset)
    const newEnd = new Date(baseEnd + offset)

    reservations.push({
      labId: form.labId,
      userId: user.id,
      startTime: toLocalIsoString(newStart),
      endTime: toLocalIsoString(newEnd),
      reason: form.reason + (i > 0 ? ` (第${i+1}周)` : '')
    })
  }

  try {
    // 调用后端的批量接口
    await axios.post('/api/reservations/batch', reservations)
    alert("预约申请已提交！")
    closeModal()
    // 刷新日历事件 (简单做法：重新加载页面，或者使用 calendarApi.refetchEvents())
    window.location.reload()
  } catch (err) {
    alert("预约失败：" + (err.response?.data?.message || "时间冲突或系统错误"))
  }
}

// 辅助函数：处理 JS Date 到类似于 '2023-12-01T10:00:00' 的格式
function toLocalIsoString(date) {
  const pad = (num) => (num < 10 ? '0' : '') + num
  return date.getFullYear() +
    '-' + pad(date.getMonth() + 1) +
    '-' + pad(date.getDate()) +
    'T' + pad(date.getHours()) +
    ':' + pad(date.getMinutes()) +
    ':' + pad(date.getSeconds())
}

const handleDateSelect = (selectInfo) => {
  selectedInfo.value = selectInfo
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  // 清除日历上的选择高亮
  selectedInfo.value.view?.calendar.unselect()
}

// 日历配置
const calendarOptions = reactive({
  plugins: [ dayGridPlugin, timeGridPlugin, interactionPlugin ],
  initialView: 'timeGridWeek',
  headerToolbar: { left: 'prev,next', center: 'title', right: 'dayGridMonth,timeGridWeek' },
  locale: 'zh-cn',
  selectable: true,
  allDaySlot: false,
  slotMinTime: '08:00:00',
  slotMaxTime: '22:00:00',
  select: handleDateSelect,
  // 从后端加载事件显示在日历上
  events: async (info, success, failure) => {
    try {
      const res = await axios.get('/api/reservations/list')
      // 转换后端数据格式为 FullCalendar 格式
      const events = res.data.map(r => ({
        title: `${r.reason} (${r.status})`,
        start: r.startTime,
        end: r.endTime,
        color: r.status === 'APPROVED' ? '#67C23A' : (r.status === 'REJECTED' ? '#F56C6C' : '#E6A23C')
      }))
      success(events)
    } catch (e) { failure(e) }
  }
})
</script>

<style scoped>
.calendar-container { position: relative; background: #fff; padding: 20px; border-radius: 8px; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; justify-content: center; align-items: center;
}
.modal-content {
  background: white; padding: 30px; border-radius: 8px; width: 400px;
}
.form-group { margin-bottom: 15px; display: flex; flex-direction: column; }
input, select { padding: 8px; margin-top: 5px; border: 1px solid #ddd; }
.actions { display: flex; justify-content: space-between; margin-top: 20px; }
button { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; }
.confirm { background: #409EFF; color: white; }
.cancel { background: #f4f4f5; }
</style>
