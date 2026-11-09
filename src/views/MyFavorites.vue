<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getFavorites, removeFavorite } from '../api/favorites.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

async function load() {
  loading.value = true
  try {
    // 后端：GET /api/v1/goodsCollect/my（USE_MOCK 时走本地演示数据）
    list.value = await getFavorites()
  } catch (e) {
    ElMessage.error(e.message || '加载收藏失败')
  } finally { loading.value = false }
}
onMounted(load)

async function removeFav(item) {
  try {
    // 后端按 goodsId 取消收藏：DELETE /api/v1/goodsCollect/{goodsId}
    await removeFavorite(item.id)
    list.value = list.value.filter(i => i.id !== item.id)
    ElMessage.success('已取消收藏')
  } catch (e) {
    ElMessage.error(e.message || '取消收藏失败')
  }
}
</script>

<template>
  <div class="page">
    <NavBar />

    <main class="container">
      <div class="page-head">
        <h2>我的收藏</h2>
        <span class="count">共 {{ list.length }} 件收藏</span>
      </div>

      <div v-if="list.length === 0 && !loading" class="empty">
        <div class="empty-icon">💔</div>
        <p>还没有收藏任何商品</p>
        <button class="go-market" @click="router.push('/product')">去逛逛</button>
      </div>

      <div v-loading="loading" class="goods-grid">
        <div v-for="item in list" :key="item.id" class="goods-card">
          <div class="goods-img">{{ item.icon || '📦' }}</div>
          <div class="goods-info">
            <h4>{{ item.title || item.name }}</h4>
            <p class="desc">{{ item.description || '二手好物' }}</p>
            <div class="goods-bottom">
              <span class="price">¥{{ item.price }}</span>
              <span class="seller">卖家：{{ item.seller || '同学' }}</span>
            </div>
            <div class="actions">
              <button class="view-btn" @click="router.push(`/product/${item.id}`)">查看详情</button>
              <button class="unfav-btn" @click="removeFav(item)">♥ 取消收藏</button>
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

.empty { text-align: center; padding: 80px 0; }
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty p { color: #94a3b8; margin-bottom: 20px; }
.go-market { background: #2563eb; color: #fff; border: none; padding: 10px 24px; border-radius: 20px; cursor: pointer; }

.goods-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.goods-card { background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid #f0f0f0; transition: all .2s; }
.goods-card:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.06); transform: translateY(-2px); }
.goods-img { height: 140px; background: linear-gradient(135deg, #fef2f2, #fee2e2); display: flex; align-items: center; justify-content: center; font-size: 48px; }
.goods-info { padding: 16px; }
.goods-info h4 { margin: 0 0 6px; font-size: 15px; }
.desc { margin: 0 0 12px; font-size: 12px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.price { color: #ef4444; font-weight: 700; font-size: 18px; }
.seller { font-size: 12px; color: #94a3b8; }
.actions { display: flex; gap: 8px; }
.actions button { flex: 1; padding: 6px; border-radius: 8px; font-size: 12px; cursor: pointer; border: none; }
.view-btn { background: #eff6ff; color: #2563eb; }
.unfav-btn { background: #fef2f2; color: #ef4444; }

@media (max-width: 800px) { .goods-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
