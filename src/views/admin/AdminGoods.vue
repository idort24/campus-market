<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../../mock/admin.js'
import { QUALITY_MAP } from '../../data/dict.js'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try { list.value = await adminApi.getAuditGoods() } finally { loading.value = false }
}

async function audit(row, pass) {
  try {
    await ElMessageBox.confirm(`确定${pass ? '通过' : '拒绝'}商品「${row.title}」？`, '审核', { type: 'warning' })
    await adminApi.auditGoods(row.id, pass, pass ? '' : '信息不完整')
    list.value = list.value.filter(g => g.id !== row.id)
    ElMessage.success(pass ? '已通过' : '已拒绝')
  } catch {}
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>商品审核</h2>
      <p class="desc">新发布商品需审核通过后才能上架</p>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="商品标题" />
      <el-table-column prop="seller_name" label="卖家" width="120" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="quality" label="成色" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ QUALITY_MAP[row.quality] || row.quality }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="create_time" label="提交时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="audit(row, true)">通过</el-button>
          <el-button size="small" type="danger" @click="audit(row, false)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page-header h2 { margin: 0 0 6px; font-size: 20px; color: #333; }
.desc { margin: 0 0 18px; color: #888; font-size: 13px; }
</style>
