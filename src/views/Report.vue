<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

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
    <NavBar />

    <main class="container">
      <h2>举报商品 #{{ route.params.goodsId }}</h2>
      <div class="card">
        <div class="form-item">
          <label>举报原因</label>
          <div class="radio-group">
            <label v-for="r in reasons" :key="r" class="radio-item">
              <input type="radio" :value="r" v-model="reason" /> {{ r }}
            </label>
          </div>
        </div>
        <div class="form-item">
          <label>联系方式</label>
          <input v-model="contact" placeholder="选填，方便管理员联系你" />
        </div>
        <div class="btn-row">
          <button class="submit-btn" @click="submit">提交举报</button>
          <button class="cancel-btn" @click="router.back()">取消</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; text-decoration: none; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; text-decoration: none; }

.container { max-width: 600px; margin: 24px auto; padding: 0 16px; }
.container h2 { margin-bottom: 20px; }
.card { background: #fff; border-radius: 16px; padding: 28px; border: 1px solid #f0f0f0; }
.form-item { margin-bottom: 20px; }
.form-item label { display: block; margin-bottom: 10px; font-size: 14px; font-weight: 500; color: #334155; }
.radio-group { display: flex; flex-direction: column; gap: 10px; }
.radio-item { font-size: 14px; color: #475569; cursor: pointer; }
.form-item input[type="text"], .form-item input:not([type="radio"]) { width: 100%; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 10px; font-size: 14px; outline: none; }
.btn-row { display: flex; gap: 12px; margin-top: 8px; }
.submit-btn { background: #2563eb; color: #fff; border: none; padding: 10px 24px; border-radius: 10px; cursor: pointer; font-size: 14px; }
.cancel-btn { background: #f1f5f9; color: #475569; border: none; padding: 10px 24px; border-radius: 10px; cursor: pointer; font-size: 14px; }
</style>
