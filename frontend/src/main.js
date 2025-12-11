import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 后面会定义

const app = createApp(App)
app.use(router)
app.mount('#app')