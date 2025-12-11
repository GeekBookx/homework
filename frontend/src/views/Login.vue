<template>
  <div class="login-container">
    <div class="notice-board">
      <h3>📢 实验室公告</h3>
      <div class="notice-content">
        {{ notice || '正在加载公告...' }}
      </div>
    </div>

    <div class="login-form">
      <h2>实验室预约系统</h2>
      <input v-model="form.username" placeholder="用户名" />
      <input type="password" v-model="form.password" placeholder="密码" />
      <button @click="handleLogin">登录</button>
      <div class="links">
        <span @click="$router.push('/register')">没有账号？去注册</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const notice = ref('');
const form = ref({ username: '', password: '' });

// 页面加载时获取公告
onMounted(async () => {
  try {
    const res = await axios.get('/api/announcements/current');
    notice.value = res.data.content;
  } catch (e) {
    notice.value = "暂无最新公告";
  }
});

const handleLogin = async () => {
  try {
    const res = await axios.post('/api/users/login', form.value);
    // 登录成功，保存用户信息到本地
    localStorage.setItem('user', JSON.stringify(res.data));
    alert('登录成功');
    router.push('/dashboard');
  } catch (err) {
    // 智能解析错误信息
    const errorData = err.response?.data;
    let msg = '登录失败';
    
    if (errorData) {
      // 优先显示后端传回来的 message
      if (errorData.message) {
        msg = errorData.message;
      } else if (typeof errorData === 'string') {
        msg = errorData;
      } else {
        // 如果还是对象，转成字符串方便调试，不再显示 [object Object]
        msg = JSON.stringify(errorData);
      }
    }
    alert(msg);
    console.error(err);
  }
};
</script> <style scoped>
/* 简单的左右布局样式 */
.login-container { display: flex; height: 100vh; }
.notice-board { width: 40%; background: #f0f2f5; padding: 40px; }
.login-form { width: 60%; display: flex; flex-direction: column; justify-content: center; padding: 100px; }
input { margin-bottom: 15px; padding: 10px; }
</style>
