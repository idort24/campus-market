import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// Element Plus 组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 自定义指令：v-permission
import { permissionDirective } from './directives/permission.js'

const app = createApp(App)

app.use(router)
app.use(ElementPlus)
app.directive('permission', permissionDirective)

app.mount('#app')
