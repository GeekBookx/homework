<template>
  <div class="calendar-wrapper">
    <FullCalendar :options="calendarOptions" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import axios from 'axios'

// 定义 FullCalendar 的配置
const calendarOptions = reactive({
  plugins: [ dayGridPlugin, timeGridPlugin, interactionPlugin ],
  initialView: 'timeGridWeek', // 默认按周显示，适合实验室预约
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  locale: 'zh-cn', // 中文界面
  selectable: true, // 允许拖拽选择时间段
  selectMirror: true,
  dayMaxEvents: true,
  allDaySlot: false, // 实验室通常不需要全天事件，需要具体时段
  slotMinTime: '08:00:00', // 早上8点开始
  slotMaxTime: '22:00:00', // 晚上10点结束
  
  // 1. 加载现有的预约数据
  events: async (fetchInfo, successCallback, failureCallback) => {
    try {
      // 调用后端接口获取预约列表 (后端需要实现 GET /api/reservations)
      // 这里为了防止构建报错，先返回空数组，等你后端写好接口再取消注释
      // const res = await axios.get('/api/reservations');
      // successCallback(res.data.map(item => ({
      //   title: item.reason || '已预约',
      //   start: item.startTime,
      //   end: item.endTime,
      //   color: item.userId === 1 ? '#3788d8' : '#ff9f89' // 示例颜色逻辑
      // })));
      
      successCallback([]); 
    } catch (error) {
      failureCallback(error);
    }
  },

  // 2. 选择时间段触发预约
  select: handleDateSelect
})

// 处理日期选择（点击并拖拽时间段）
async function handleDateSelect(selectInfo) {
  const title = prompt('请输入预约理由/实验内容:')
  const calendarApi = selectInfo.view.calendar

  calendarApi.unselect() // 清除选中状态

  if (title) {
    const newReservation = {
      startTime: selectInfo.startStr,
      endTime: selectInfo.endStr,
      reason: title
    }

    try {
      // 获取当前登录用户ID (假设存在localStorage)
      const userStr = localStorage.getItem('user');
      if(!userStr) {
        alert('请先登录');
        return;
      }
      const user = JSON.parse(userStr);

      // 发送请求给后端
      await axios.post('/api/reservations/create', {
        ...newReservation,
        userId: user.id, 
        labId: 1 // 暂时硬编码为1号实验室，后续可扩展选择
      });

      // 成功后在日历上渲染
      calendarApi.addEvent({
        title,
        start: selectInfo.startStr,
        end: selectInfo.endStr,
        allDay: selectInfo.allDay
      })
      alert('预约申请提交成功');
    } catch (error) {
      alert('预约失败: ' + (error.response?.data?.message || '未知错误'));
    }
  }
}
</script>

<style scoped>
.calendar-wrapper {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
