<script setup>
// ============================================================
// 分类管理（管理员后台）
// 按钮权限：category:manage
// ============================================================
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../../mock/admin.js'

const tree = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await adminApi.getCategoryTree()
    tree.value = Array.isArray(res) ? res : (res.records || [])
  } finally {
    loading.value = false
  }
}

function todo(action, row) {
  ElMessage.info(`${action}「${row?.categoryName || '分类'}」功能待后端分类接口就绪`)
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>分类管理</h2>
      <p class="desc">维护商品分类树形结构</p>
    </div>

    <el-card v-loading="loading">
      <el-table :data="tree" row-key="id" border default-expand-all>
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column label="子分类数" width="120">
          <template #default="{ row }">{{ row.children?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-permission="'category:manage'" size="small" type="primary" @click="todo('新增子分类', row)">新增子分类</el-button>
            <el-button v-permission="'category:manage'" size="small" @click="todo('编辑', row)">编辑</el-button>
            <el-button v-permission="'category:manage'" size="small" type="danger" @click="todo('删除', row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page-header h2 { margin: 0 0 6px; font-size: 20px; color: #333; }
.desc { margin: 0 0 18px; color: #888; font-size: 13px; }
</style>
