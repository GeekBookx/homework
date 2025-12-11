<template>
  <div class="dashboard">
    <nav class="nav-bar">
      <div class="brand">🔬 实验室管理系统</div>
      <div class="user-info">
        <span>{{ user.full_name }} ({{ roleMap[user.role] }})</span>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </nav>

    <div class="main-content">
      <div v-if="['STUDENT', 'TEACHER'].includes(user.role)" class="student-grid">
        <div class="panel">
          <div class="panel-header">
            <h3>📅 实验室预约日历</h3>
            <span class="tip">拖拽日历时间段即可发起预约</span>
          </div>
          <CalendarComponent />
        </div>
        <div class="panel">
          <h3>👤 我的预约记录</h3>
          <table class="data-table">
            <thead><tr><th>实验室</th><th>时间段</th><th>用途</th><th>状态</th></tr></thead>
            <tbody>
              <tr v-for="res in myReservations" :key="res.id">
                <td>{{ res.labName }}</td>
                <td>{{ formatTime(res.startTime) }}<br>至<br>{{ formatTime(res.endTime) }}</td>
                <td>{{ res.reason }}</td>
                <td>
                  <span :class="statusClass(res.status)">{{ statusMap[res.status] || res.status }}</span>
                </td>
              </tr>
              <tr v-if="myReservations.length === 0"><td colspan="4" class="empty">暂无记录</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="user.role === 'MANAGER'" class="admin-grid">
        <div class="panel full-width">
          <h3>📝 预约审核任务</h3>
          <table class="data-table">
            <thead><tr><th>申请人</th><th>实验室</th><th>时间段</th><th>用途</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="item in pendingReservations" :key="item.id">
                <td>{{ item.username }}</td>
                <td>{{ item.labName }}</td>
                <td>{{ formatTime(item.startTime) }}<br>至<br>{{ formatTime(item.endTime) }}</td>
                <td>{{ item.reason }}</td>
                <td>
                  <button class="btn-approve" @click="audit(item.id, 'APPROVED')">通过</button>
                  <button class="btn-reject" @click="audit(item.id, 'REJECTED')">驳回</button>
                </td>
              </tr>
              <tr v-if="pendingReservations.length === 0"><td colspan="5" class="empty">暂无待审核预约</td></tr>
            </tbody>
          </table>
        </div>
        <div class="panel full-width">
          <div class="panel-header"><h3>🛠️ 实验室状态维护</h3></div>
          <table class="data-table">
            <thead><tr><th>名称</th><th>容量</th><th>当前状态</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="lab in labs" :key="lab.id">
                <td>{{ lab.name }}</td>
                <td>{{ lab.capacity }}人</td>
                <td>
                  <span :class="lab.isActive ? 'tag-green' : 'tag-red'">{{ lab.isActive ? '开放预约' : '维护中' }}</span>
                </td>
                <td>
                  <button class="btn-text" @click="toggleLab(lab)">{{ lab.isActive ? '设为维护' : '恢复开放' }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="user.role === 'ADMIN'" class="admin-grid">
        <div class="panel">
          <div class="panel-header">
            <h3>🏢 实验室资产管理</h3>
            <button class="btn-primary" @click="showAddLab = true">新增实验室</button>
          </div>
          <table class="data-table">
            <thead><tr><th>名称</th><th>容量</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="lab in labs" :key="lab.id">
                <td>{{ lab.name }}</td>
                <td>{{ lab.capacity }}</td>
                <td><button class="btn-text delete" @click="deleteLab(lab.id)">删除</button></td>
              </tr>
            </tbody>
          </table>
          <div v-if="showAddLab" class="inline-form">
            <input v-model="newLab.name" placeholder="名称" />
            <input v-model="newLab.capacity" placeholder="容量" type="number" />
            <button @click="addLab">保存</button>
            <button @click="showAddLab = false" class="btn-cancel">取消</button>
          </div>
        </div>

        <div class="panel">
          <h3>👥 负责人注册审核</h3>
          <ul class="audit-list">
            <li v-for="u in pendingUsers" :key="u.id">
              <div class="user-detail">
                <strong>{{ u.fullName }}</strong> 
                <span class="sub-text">账号: {{ u.username }}</span>
                <span class="role-tag">{{ roleMap[u.role] }}</span>
              </div>
              <div class="btn-group">
                <button class="btn-approve small" @click="approveUser(u.id)">批准</button>
                <button class="btn-reject small" @click="rejectUser(u.id)">拒绝</button>
              </div>
            </li>
            <li v-if="pendingUsers.length === 0" class="empty">无待审核用户</li>
          </ul>
        </div>

        <div class="panel full-width">
          <div class="panel-header">
            <h3>📊 实验室使用热度统计</h3>
            <button class="btn-text" @click="exportReport">📥 导出报表</button>
          </div>
          <div class="chart-container">
            <div v-for="stat in stats" :key="stat.name" class="bar-group">
              <div class="bar-label">{{ stat.name }}</div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: Math.min(stat.count * 5, 100) + '%' }">{{ stat.count }}次</div>
              </div>
            </div>
            <div v-if="stats.length === 0" class="empty">暂无统计数据</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import CalendarComponent from '../components/Calendar.vue';

const router = useRouter();
const user = ref({});
const roleMap = { STUDENT: '学生', TEACHER: '教师', MANAGER: '实验室负责人', ADMIN: '系统管理员' };
const statusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' };

const myReservations = ref([]);
const pendingReservations = ref([]);
const labs = ref([]);
const pendingUsers = ref([]);
const stats = ref([]);
const showAddLab = ref(false);
const newLab = reactive({ name: '', capacity: '', isActive: true, equipmentList: '标准配置' });

onMounted(async () => {
  const storedUser = localStorage.getItem('user');
  if (!storedUser) return router.push('/login');
  user.value = JSON.parse(storedUser);

  if (['STUDENT', 'TEACHER'].includes(user.value.role)) loadMyReservations();
  if (user.value.role === 'MANAGER') { loadPendingReservations(); loadLabs(); }
  if (user.value.role === 'ADMIN') { loadLabs(); loadPendingUsers(); loadStats(); }
});

const logout = () => { localStorage.clear(); router.push('/login'); };
const formatTime = (iso) => new Date(iso).toLocaleString('zh-CN', { hour12: false });
const statusClass = (s) => ({ 'PENDING': 'tag-orange', 'APPROVED': 'tag-green', 'REJECTED': 'tag-red' }[s]);

const loadMyReservations = async () => { const res = await axios.get(`/api/reservations/my?userId=${user.value.id}`); myReservations.value = res.data; };
const loadPendingReservations = async () => { const res = await axios.get('/api/reservations/pending'); pendingReservations.value = res.data; };
const audit = async (id, status) => { await axios.post(`/api/reservations/audit/${id}`, { status }); loadPendingReservations(); };
const toggleLab = async (lab) => { await axios.post(`/api/labs/update/${lab.id}`, { isActive: !lab.isActive }); loadLabs(); };
const loadLabs = async () => { const res = await axios.get('/api/labs/list'); labs.value = res.data; };
const loadPendingUsers = async () => { const res = await axios.get('/api/admin/users/pending'); pendingUsers.value = res.data; };
const loadStats = async () => { const res = await axios.get('/api/admin/stats'); stats.value = res.data; };
const addLab = async () => { if(!newLab.name) return; await axios.post('/api/labs/add', newLab); showAddLab.value = false; loadLabs(); };
const deleteLab = async (id) => { if(confirm('确定删除该实验室？')) { await axios.delete(`/api/labs/${id}`); loadLabs(); } };
const approveUser = async (id) => { await axios.post(`/api/admin/users/approve/${id}`); loadPendingUsers(); };

// 🔥 新增：拒绝用户逻辑
const rejectUser = async (id) => { 
  if(confirm('确定拒绝该用户的注册申请吗？')) {
    await axios.post(`/api/admin/users/reject/${id}`); 
    loadPendingUsers(); 
  }
};

const exportReport = () => {
  let csvContent = "data:text/csv;charset=utf-8,实验室名称,预约次数\n";
  stats.value.forEach(row => { csvContent += `${row.name},${row.count}\n`; });
  const link = document.createElement("a"); link.href = encodeURI(csvContent); link.download = "lab_report.csv"; link.click();
};
</script>

<style scoped>
.dashboard { min-height: 100vh; background: #f0f2f5; }
.nav-bar { background: #001529; color: white; padding: 0 20px; height: 60px; display: flex; justify-content: space-between; align-items: center; }
.brand { font-size: 1.2rem; font-weight: bold; }
.logout-btn { margin-left: 15px; background: transparent; color: #ff4d4f; border: 1px solid #ff4d4f; }
.main-content { padding: 20px; max-width: 1200px; margin: 0 auto; }
.panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
.data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
.empty { text-align: center; color: #999; padding: 20px; }
.student-grid, .admin-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
.full-width { grid-column: 1 / -1; }
button { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; transition: 0.3s; margin-right: 5px;}
.btn-primary { background: #1890ff; color: white; }
.btn-approve { background: #52c41a; color: white; }
.btn-reject { background: #ff4d4f; color: white; }
.btn-text { background: transparent; color: #1890ff; }
.btn-text.delete { color: #ff4d4f; }
.tag-green { color: #52c41a; background: #f6ffed; padding: 2px 8px; border-radius: 4px; }
.tag-red { color: #ff4d4f; background: #fff1f0; padding: 2px 8px; border-radius: 4px; }
.tag-orange { color: #fa8c16; background: #fff7e6; padding: 2px 8px; border-radius: 4px; }
.audit-list li { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #eee; }
.user-detail { display: flex; flex-direction: column; }
.sub-text { font-size: 12px; color: #666; }
.role-tag { font-size: 12px; background: #e6f7ff; color: #1890ff; padding: 1px 5px; border-radius: 4px; display: inline-block; width: fit-content; margin-top: 2px;}
.bar-track { flex: 1; background: #f5f5f5; height: 24px; border-radius: 4px; overflow: hidden; margin-top: 5px;}
.bar-fill { height: 100%; background: #1890ff; color: white; font-size: 12px; display: flex; align-items: center; padding-left: 10px; transition: width 0.5s ease; }
.inline-form { margin-top: 15px; padding: 15px; background: #f9f9f9; display: flex; gap: 10px; }
</style>
