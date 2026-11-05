<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../mock/admin.js'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try { list.value = await adminApi.getReports() } finally { loading.value = false }
}

async function handle(row, action) {
  await adminApi.handleReport(row.id, action)
  row.status = 'HANDLED'
  ElMessage.success(action === 'ban' ? '已下架商品并封禁用户' : '已驳回举报')
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>举报处理</h2>
      <p class="desc">处理用户提交的举报工单</p>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="goods_title" label="被举报商品" />
      <el-table-column prop="reporter" label="举报人" width="120" />
      <el-table-column prop="reason" label="举报原因" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
            {{ row.status === 'PENDING' ? '待处理' : '已处理' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="提交时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button size="small" type="danger" @click="handle(row, 'ban')">下架商品</el-button>
            <el-button size="small" @click="handle(row, 'reject')">驳回</el-button>
          </template>
          <span v-else style="color:#999">—</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page-header h2 { margin: 0 0 6px; font-size: 20px; color: #333; }
.desc { margin: 0 0 18px; color: #888; font-size: 13px; }
</style>
