<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { mockApi } from '../mock/index.js'

const router = useRouter()
const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const all = await mockApi.getGoods()
    list.value = all.slice(0, 4)
  } finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div class="page">
    <h2>我的发布</h2>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default>
          <el-tag type="success">已上架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="router.push(`/product/${row.id}`)">查看</el-button>
          <el-button size="small" type="danger">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: 24px; }
h2 { margin: 0 0 16px; }
</style>
