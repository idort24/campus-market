<script setup>
// ============================================================
// 用户管理（管理员后台）
// ------------------------------------------------------------
// 字段命名统一驼峰，与后端 SysUserVO 保持一致：
//   studentNo / nickName / userType / accountStatus / creditScore / createTime
// 按钮级权限：v-permission="'user:status'" —— 无该权限码时按钮不渲染
// ============================================================
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../mock/admin.js'

const users = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await adminApi.getUsers()
    users.value = Array.isArray(res) ? res : (res.records || [])
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row) {
  const action = row.accountStatus === 1 ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.nickName}」？`, '提示', { type: 'warning' })
    await adminApi.toggleUserStatus(row.id, row.accountStatus === 1 ? 0 : 1)
    row.accountStatus = row.accountStatus === 1 ? 0 : 1
    ElMessage.success(`${action}成功`)
  } catch {}
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
      <p class="desc">查看平台用户，可封禁违规账号</p>
    </div>

    <el-table :data="users" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="studentNo" label="学号" width="130" />
      <el-table-column prop="nickName" label="昵称" width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.userType === 9 ? 'danger' : 'primary'">
            {{ row.userType === 9 ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creditScore" label="信用分" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.accountStatus === 1 ? 'success' : 'danger'">
            {{ row.accountStatus === 1 ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-permission="'user:status'"
            size="small"
            :type="row.accountStatus === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.accountStatus === 1 ? '封禁' : '解封' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page-header h2 { margin: 0 0 6px; font-size: 20px; color: #333; }
.desc { margin: 0 0 18px; color: #888; font-size: 13px; }
</style>
