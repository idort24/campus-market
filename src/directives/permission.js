// ============================================================
// v-permission 按钮级权限指令
// ------------------------------------------------------------
// 用法：<el-button v-permission="'goods:audit'">审核通过</el-button>
//      <el-button v-permission="['goods:audit','user:status']">多权限（任一满足）</el-button>
// 逻辑：从 localStorage.userInfo.perms 里查权限码，没有就移除 DOM。
// 权限码必须与后端 AuthInterceptor / sys_permission 表完全一致：
//   user:manage、user:status、goods:audit、report:handle、category:manage、dict:manage
// ============================================================

function getUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch {
    return {}
  }
}

export const permissionDirective = {
  mounted(el, binding) {
    const required = binding.value
    if (!required) return

    const info = getUserInfo()
    const perms = Array.isArray(info.perms) ? info.perms : []

    // 管理员兜底：即使 perms 未下发完整，也放行（后端仍会二次校验）
    if (info.roleCode === 'ADMIN') return

    const need = Array.isArray(required) ? required : [required]
    const ok = need.some((code) => perms.includes(code))

    if (!ok && el.parentNode) {
      el.parentNode.removeChild(el)
    }
  },
}
