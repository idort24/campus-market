// ============================================================
// v-permission 按钮级权限指令
// 用法：<el-button v-permission="'user:ban'">封禁</el-button>
// 逻辑：从 localStorage.userInfo.perms 里查权限码，没有就移除 DOM
// ============================================================

function getUserPerms() {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return info.perms || []
  } catch { return [] }
}

export const permissionDirective = {
  mounted(el, binding) {
    const required = binding.value
    if (!required) return
    const perms = getUserPerms()
    // 管理员默认拥有全部权限
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (userInfo.roleCode === 'ADMIN' || userInfo.userType === 9) return
    if (!perms.includes(required)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
