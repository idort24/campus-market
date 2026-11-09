<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { fetchMyGoods, updateGoodsStatus } from '../api/product.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

async function load() {
  loading.value = true
  try {
    // 后端：GET /api/v1/goods/my（当前登录人发布的商品）
    const all = await fetchMyGoods()
    list.value = all.map(g => ({ ...g, status: g.goodsStatus || 'ON_SHELF' }))
  } catch (e) {
    ElMessage.error(e.message || '加载我的发布失败')
  } finally { loading.value = false }
}
onMounted(load)

// 上下架统一走 PUT /api/v1/goods/{id}/status?status=ON_SHELF|OFF_SHELF
// 先乐观更新界面，失败回滚，避免按钮点了没反应
async function changeStatus(item, status) {
  const old = item.status
  item.status = status
  try {
    await updateGoodsStatus(item.id, status)
    ElMessage.success(status === 'OFF_SHELF' ? '已下架' : '已重新上架')
  } catch (e) {
    item.status = old
    ElMessage.error(e.message || '操作失败，请稍后重试')
  }
}

function offShelf(item) {
  ElMessageBox.confirm(`确定要下架「${item.name}」吗？`, '提示', { type: 'warning' })
    .then(() => changeStatus(item, 'OFF_SHELF'))
    .catch(() => {})
}

function onShelf(item) {
  changeStatus(item, 'ON_SHELF')
}
</script>

<template>
  <div class="page">
    <NavBar :show-publish="false" />

    <main class="container">
      <div class="page-head">
        <h2>我的发布</h2>
        <span class="count">共 {{ list.length }} 件商品</span>
      </div>

      <div v-loading="loading" class="goods-grid">
        <div v-for="item in list" :key="item.id" class="goods-card">
          <div class="goods-img">{{ item.icon || '📦' }}</div>
          <div class="goods-info">
            <h4>{{ item.name }}</h4>
            <p class="desc">{{ item.description || item.category }}</p>
            <div class="goods-bottom">
              <span class="price">¥{{ item.price }}</span>
              <span class="status-tag" :class="item.status">{{ item.status === 'ON_SHELF' ? '已上架' : '已下架' }}</span>
            </div>
            <div class="actions">
              <button class="view-btn" @click="router.push(`/product/${item.id}`)">查看</button>
              <button v-if="item.status === 'ON_SHELF'" class="off-btn" @click="offShelf(item)">下架</button>
              <button v-else class="on-btn" @click="onShelf(item)">上架</button>
            </div>
          </div>
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

.container { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
.page-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 24px; }
.page-head h2 { margin: 0; font-size: 22px; }
.count { color: #94a3b8; font-size: 13px; }

.goods-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.goods-card { background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid #f0f0f0; transition: all .2s; }
.goods-card:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.06); transform: translateY(-2px); }
.goods-img { height: 140px; background: linear-gradient(135deg, #f1f5f9, #e2e8f0); display: flex; align-items: center; justify-content: center; font-size: 48px; }
.goods-info { padding: 16px; }
.goods-info h4 { margin: 0 0 6px; font-size: 15px; }
.desc { margin: 0 0 12px; font-size: 12px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.price { color: #ef4444; font-weight: 700; font-size: 18px; }
.status-tag { font-size: 11px; padding: 2px 8px; border-radius: 10px; }
.status-tag.ON_SHELF { background: #dcfce7; color: #166534; }
.status-tag.OFF_SHELF { background: #f1f5f9; color: #64748b; }
.actions { display: flex; gap: 8px; }
.actions button { flex: 1; padding: 6px; border-radius: 8px; font-size: 12px; cursor: pointer; border: none; }
.view-btn { background: #eff6ff; color: #2563eb; }
.off-btn { background: #fef2f2; color: #ef4444; }
.on-btn { background: #f0fdf4; color: #16a34a; }

@media (max-width: 800px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
