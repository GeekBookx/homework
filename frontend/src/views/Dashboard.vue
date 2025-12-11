<template>
  <div class="dashboard">
    <nav class="nav-bar">
      <h2>欢迎你，{{ user.full_name }} ({{ user.role }})</h2>
      <button @click="logout">退出</button>
    </nav>

    <div class="content">
      <div v-if="user.role === 'STUDENT' || user.role === 'TEACHER'">
        <h3>📅 实验室预约</h3>
        <CalendarComponent />
      </div>

      <div v-if="user.role === 'MANAGER'">
        <h3>📝 待审核预约</h3>
        <table>
          <tr v-for="item in pendingList" :key="item.id">
            <td>{{ item.username }}</td>
            <td>{{ item.startTime }}</td>
            <td>
              <button @click="audit(item.id, 'APPROVED')">通过</button>
              <button @click="audit(item.id, 'REJECTED')">驳回</button>
            </td>
          </tr>
        </table>
      </div>

      <div v-if="user.role === 'ADMIN'">
        <h3>📊 系统管理</h3>
        <button>发布新公告</button>
        <button>导出月度报表</button>
        <button>用户审核（负责人注册）</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import CalendarComponent from '../components/Calendar.vue'; // 假设你封装了FullCalendar

const router = useRouter();
const user = ref({});
const pendingList = ref([]);

onMounted(() => {
  const storedUser = localStorage.getItem('user');
  if (!storedUser) {
    router.push('/login');
    return;
  }
  user.value = JSON.parse(storedUser);

  // 如果是负责人，加载待审核列表
  if (user.value.role === 'MANAGER') {
    loadPendingReservations();
  }
});

const loadPendingReservations = async () => {
  const res = await axios.get('/api/reservations/pending');
  pendingList.value = res.data;
};

const audit = async (id, status) => {
  await axios.post(`/api/reservations/audit/${id}`, { status });
  loadPendingReservations(); // 刷新列表
};

const logout = () => {
  localStorage.clear();
  router.push('/login');
};
</script>