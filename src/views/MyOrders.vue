<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { ORDER_STATUS_MAP, ORDER_STATUS_TAG } from '../data/dict.js'

const orders = ref([
  { id: 1, goods_title: '九成新《数据结构》', price: 25.00, seller: '张爱阳', status: 'WAIT_CONFIRM' },
  { id: 2, goods_title: '高数习题集', price: 18.50, seller: '周泓颖', status: 'FINISH' },
])

function pay(row) {
  ElMessage.success(`订单 #${row.id} 支付成功（演示）`)
  row.status = 'FINISH'
}
</script>

<template>
  <div class="page">
    <h2>我的订单</h2>
    <el-table :data="orders" border>
      <el-table-column prop="id" label="订单号" width="100" />
      <el-table-column prop="goods_title" label="商品" />
      <el-table-column prop="price" label="金额" width="100">
        <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="seller" label="卖家" width="120" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="ORDER_STATUS_TAG[row.status]">{{ ORDER_STATUS_MAP[row.status] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 'WAIT_CONFIRM'" size="small" type="primary" @click="pay(row)">去付款</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: 24px; }
h2 { margin: 0 0 16px; }
</style>
