import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// Element Plus 组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 中文语言包：确认框按钮显示"确定/取消"，不配就是默认的 OK/Cancel
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 自定义指令：v-permission
import { permissionDirective } from './directives/permission.js'

const app = createApp(App)

app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.directive('permission', permissionDirective)

app.mount('#app')
