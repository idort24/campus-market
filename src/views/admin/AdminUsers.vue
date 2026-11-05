<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../mock/admin.js'

const users = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try { users.value = await adminApi.getUsers() } finally { loading.value = false }
}

async function toggleStatus(row) {
  const action = row.account_status === 1 ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.nick_name}」？`, '提示', { type: 'warning' })
    await adminApi.toggleUserStatus(row.id, row.account_status === 1 ? 0 : 1)
    row.account_status = row.account_status === 1 ? 0 : 1
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
      <el-table-column prop="student_no" label="学号" width="130" />
      <el-table-column prop="nick_name" label="昵称" width="120" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.user_type === 9 ? 'danger' : 'primary'">
            {{ row.user_type === 9 ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="credit_score" label="信用分" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.account_status === 1 ? 'success' : 'danger'">
            {{ row.account_status === 1 ? '正常' : '已封禁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="注册时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.account_status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.account_status === 1 ? '封禁' : '解封' }}
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
