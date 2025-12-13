<template>
  <div class="calendar-container">
    <FullCalendar :options="calendarOptions" />

    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h3>📅 申请预约</h3>
        <p>时间：{{ selectedInfo.startStr }} 至 {{ selectedInfo.endStr }}</p>
        
        <div class="form-group">
          <label>选择实验室（实时剩余容量）：</label>
          <select v-model="form.labId">
            <option v-for="lab in labs" :key="lab.id" :value="lab.id" :disabled="lab.remaining <= 0 || !lab.isActive">
              {{ lab.name }} 
              <template v-if="!lab.isActive">[维护中]</template>
              <template v-else> (余: {{ lab.remaining }} / 总: {{ lab.capacity }})</template>
            </option>
          </select>
          <small v-if="selectedLab && selectedLab.remaining <= 0" style="color: red;">
            该时段已满，请选择其他实验室或时间
          </small>
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
            ⚠️ 注意：后续周次的容量情况以提交时系统检测为准
          </small>
        </div>

        <div class="actions">
          <button @click="closeModal" class="cancel">取消</button>
          <button @click="submitReservation" class="confirm" :disabled="!form.labId">提交申请</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import axios from 'axios'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

// 状态管理
const showModal = ref(false)
const labs = ref([]) // 这里现在存的是带有 remaining 信息的列表
const selectedInfo = ref({})
const form = reactive({ labId: null, reason: '', repeatWeeks: 0 })

// 计算属性：当前选中的实验室对象
const selectedLab = computed(() => labs.value.find(l => l.id === form.labId))

// 提交逻辑
const submitReservation = async () => {
  if (!form.reason) return alert("请填写实验内容")
  
  const user = JSON.parse(localStorage.getItem('user'))
  const reservations = []
  
  for (let i = 0; i <= form.repeatWeeks; i++) {
    const offset = i * 7 * 24 * 60 * 60 * 1000
    const baseStart = new Date(selectedInfo.value.startStr).getTime()
    const baseEnd = new Date(selectedInfo.value.endStr).getTime()
    
    reservations.push({
      labId: form.labId,
      userId: user.id,
      startTime: toLocalIsoString(new Date(baseStart + offset)),
      endTime: toLocalIsoString(new Date(baseEnd + offset)),
      reason: form.reason + (i > 0 ? ` (第${i+1}周)` : '')
    })
  }

  try {
    await axios.post('/api/reservations/batch', reservations)
    alert("预约申请已提交！")
    closeModal()
    window.location.reload()
  } catch (err) {
    alert("预约失败：" + (err.response?.data?.message || "时间冲突或系统错误"))
  }
}

function toLocalIsoString(date) {
  const pad = (num) => (num < 10 ? '0' : '') + num
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) +
    'T' + pad(date.getHours()) + ':' + pad(date.getMinutes()) + ':' + pad(date.getSeconds())
}

// 🔥 核心修改：点击日历时，先去后端查容量，再弹窗
const handleDateSelect = async (selectInfo) => {
  selectedInfo.value = selectInfo
  
  // 1. 获取该时间段的实验室容量情况
  try {
    // 转换 FullCalendar 的时间字符串为 ISO 格式发给后端
    // 注意：FullCalendar 的 selectInfo.startStr 可能是 '2023-12-01T10:00:00+08:00'，我们需要截取或处理
    // 简单处理：直接传 selectInfo.startStr，Spring Boot 应该能解析 ISO 格式
    const res = await axios.get('/api/labs/available', {
      params: {
        start: selectInfo.startStr,
        end: selectInfo.endStr
      }
    })
    
    labs.value = res.data
    
    // 自动选择第一个有空位且开放的实验室
    const firstAvailable = labs.value.find(l => l.remaining > 0 && l.isActive)
    form.labId = firstAvailable ? firstAvailable.id : null
    
    // 2. 显示弹窗
    showModal.value = true
  } catch (e) {
    alert('无法加载实验室数据，请稍后重试')
    console.error(e)
  }
}

const closeModal = () => {
  showModal.value = false
  selectedInfo.value.view?.calendar.unselect()
}

// 加载事件：过滤掉 REJECTED
const fetchEvents = async (info, success, failure) => {
  try {
    const user = JSON.parse(localStorage.getItem('user'));
    let url = '/api/reservations/list'; 
    if (user.role === 'STUDENT' || user.role === 'TEACHER') {
      url = `/api/reservations/my?userId=${user.id}`;
    }

    const res = await axios.get(url);
    const events = res.data
      .filter(r => r.status !== 'REJECTED') // 🔥 关键：过滤掉已驳回的
      .map(r => ({
        title: (user.role === 'ADMIN' || user.role === 'MANAGER') 
             ? `${r.username}: ${r.reason}` 
             : `${r.reason} (${r.status})`,
        start: r.startTime,
        end: r.endTime,
        color: r.status === 'APPROVED' ? '#67C23A' : '#E6A23C' // 既然没Rejected了，就只剩绿和黄
      }))
    success(events)
  } catch (e) { failure(e) }
}

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
  events: fetchEvents
})
</script>

<style scoped>
.calendar-container { position: relative; background: #fff; padding: 20px; border-radius: 8px; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; justify-content: center; align-items: center;
}
.modal-content { background: white; padding: 30px; border-radius: 8px; width: 400px; }
.form-group { margin-bottom: 15px; display: flex; flex-direction: column; }
input, select { padding: 8px; margin-top: 5px; border: 1px solid #ddd; }
.actions { display: flex; justify-content: space-between; margin-top: 20px; }
button { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; }
.confirm { background: #409EFF; color: white; }
.confirm:disabled { background: #ccc; cursor: not-allowed; }
.cancel { background: #f4f4f5; }
</style>
