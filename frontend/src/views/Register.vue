<template>
  <div class="register-container">
    <div class="register-card">
      <h2>📝 注册新账号</h2>
      
      <div class="form-item">
        <label>用户名</label>
        <input v-model="form.username" type="text" placeholder="设置登录账号" />
      </div>

      <div class="form-item">
        <label>密码</label>
        <input v-model="form.password" type="password" placeholder="设置登录密码" />
      </div>

      <div class="form-item">
        <label>真实姓名</label>
        <input v-model="form.fullName" type="text" placeholder="用于预约核实" />
      </div>

      <div class="form-item">
        <label>身份角色</label>
        <select v-model="form.role">
          <option value="STUDENT">学生</option>
          <option value="TEACHER">教师</option>
          <option value="MANAGER">实验室负责人</option>
        </select>
        <p class="hint" v-if="form.role === 'MANAGER'">
          ⚠️ 注意：负责人账号注册后，需管理员后台审核通过才可登录。
        </p>
      </div>

      <button @click="handleRegister" :disabled="loading">
        {{ loading ? '提交中...' : '注册' }}
      </button>

      <div class="links">
        <span @click="$router.push('/login')">已有账号？去登录</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const loading = ref(false);
const form = ref({
  username: '',
  password: '',
  fullName: '',
  role: 'STUDENT' // 默认选中学生
});

const handleRegister = async () => {
  if (!form.value.username || !form.value.password || !form.value.fullName) {
    alert("请填写完整信息");
    return;
  }

  loading.value = true;
  try {
    // 调用我们在后端 AuthController 写好的 /register 接口
    const res = await axios.post('/api/users/register', form.value);
    
    // 根据后端返回的信息提示用户
    // 如果是负责人，后端会返回 "注册成功，等待管理员审核"
    alert(res.data); 
    
    // 注册完成后跳转回登录页
    router.push('/login');
  } catch (err) {
    alert('注册失败: ' + (err.response?.data?.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.register-card {
  width: 400px;
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.form-item {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
}
input, select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 5px;
}
button {
  width: 100%;
  padding: 12px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
button:hover { background-color: #66b1ff; }
.hint { font-size: 12px; color: #e6a23c; margin-top: 5px; }
.links { margin-top: 15px; text-align: center; color: #409EFF; cursor: pointer; }
</style>