import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

// Calendar Options
calendarOptions: {
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  events: '/api/reservations/list', // 从后端获取预约数据渲染颜色块
  selectable: true,
  select: this.handleDateSelect // 点击空白处触发预约弹窗
}