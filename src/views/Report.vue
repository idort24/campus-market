<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const reason = ref('')
const contact = ref('')
const reasons = ['商品描述不符', '疑似假货/盗版', '价格欺诈', '辱骂/骚扰', '其他']

function submit() {
  if (!reason.value) { ElMessage.warning('请选择举报原因'); return }
  ElMessage.success('举报已提交，管理员会尽快处理')
  router.back()
}
</script>

<template>
  <div class="page">
    <h2>举报商品 #{{ route.params.goodsId }}</h2>
    <el-card>
      <el-form label-width="100px" @submit.prevent="submit">
        <el-form-item label="举报原因">
          <el-radio-group v-model="reason">
            <el-radio v-for="r in reasons" :key="r" :value="r">{{ r }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="contact" placeholder="选填，方便管理员联系你" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交举报</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width: 600px; margin: 20px auto; padding: 0 16px; }
h2 { margin: 0 0 16px; }
</style>
