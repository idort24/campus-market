<script setup>
import NavBar from '../components/NavBar.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProducts } from '../api/product.js'
import { addFavorite, removeFavorite, getFavorites } from '../api/favorites.js'
import { CATEGORY_OPTIONS } from '../data/categories.js'

const router = useRouter()
const products = ref([])
const loading = ref(false)
const searchText = ref('')
const activeCategory = ref('全部')
// 分类按钮统一取数据库里的一级分类（1-5），不在这里另写一份
const categories = ['全部', ...CATEGORY_OPTIONS]
const favoriteIds = ref(new Set())
const userInfo = computed(() => { try { return JSON.parse(localStorage.getItem('userInfo') || '{}') } catch { return {} } })
const isAdmin = computed(() => userInfo.value?.roleCode === 'ADMIN')

const filteredProducts = computed(() => {
  return products.value.filter((p) => {
    const cat = activeCategory.value === '全部' || p.category === activeCategory.value
    const kw = searchText.value.trim().toLowerCase()
    const match = p.name.toLowerCase().includes(kw) || (p.description || '').toLowerCase().includes(kw)
    return cat && match
  })
})

async function loadProducts() {
  loading.value = true
  try {
    products.value = await fetchProducts()
    // 收藏状态以后端为准（GET /goodsCollect/my），失败就留空集合，不影响列表展示
    try {
      const favs = await getFavorites()
      favoriteIds.value = new Set(favs.map((f) => f.id))
    } catch {
      favoriteIds.value = new Set()
    }
  }
  catch (e) { ElMessage.error(e?.message || '商品加载失败') }
  finally { loading.value = false }
}
onMounted(loadProducts)

function goDetail(id) { router.push(`/product/${id}`) }
function isFavorite(id) { return favoriteIds.value.has(id) }

async function toggleFavorite(product) {
  try {
    if (isFavorite(product.id)) {
      await removeFavorite(product.id)
      favoriteIds.value.delete(product.id)
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.id)
      favoriteIds.value.add(product.id)
      ElMessage.success('收藏成功')
    }
    favoriteIds.value = new Set(favoriteIds.value)
  } catch (e) { ElMessage.error(e?.message || '收藏操作失败') }
}
</script>

<template>
  <div class="market-page">
    <NavBar />

    <main class="container">
      <div class="page-head">
        <h1>商品市场</h1>
        <div class="search-box">
          <input v-model="searchText" placeholder="搜索商品…" />
          <button>🔍</button>
        </div>
      </div>

      <div class="cat-bar">
        <button v-for="c in categories" :key="c" :class="{ active: activeCategory === c }" @click="activeCategory = c">{{ c }}</button>
      </div>

      <div v-if="filteredProducts.length" class="goods-grid">
        <div v-for="p in filteredProducts" :key="p.id" class="goods-card" @click="goDetail(p.id)">
          <div class="goods-img">{{ p.icon }}</div>
          <div class="goods-body">
            <div class="goods-title">{{ p.name }}</div>
            <div class="goods-meta">{{ p.category }} · {{ p.location || '同校交易' }}</div>
            <div class="goods-bottom">
              <div class="goods-price">¥{{ p.price }}</div>
              <button class="fav" :class="{ on: isFavorite(p.id) }" @click.stop="toggleFavorite(p)">
                {{ isFavorite(p.id) ? '❤️' : '🤍' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty">
        <div class="empty-icon">🔎</div>
        <h3>没有找到相关商品</h3>
        <p>换个关键词试试</p>
      </div>
    </main>

    <footer class="footer">© 2026 校园二手集市</footer>
  </div>
</template>

<style scoped>
.market-page { min-height: 100vh; background: #fafbfc; }
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.publish-btn { background: #0f172a; color: #fff; padding: 8px 16px; border-radius: 20px; font-size: 13px; }
.avatar-btn { font-size: 20px; }

.container { max-width: 1200px; margin: 0 auto; padding: 32px 24px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-head h1 { font-size: 24px; font-weight: 700; }
.search-box { display: flex; background: #fff; border: 1px solid #e2e8f0; border-radius: 20px; padding: 4px; }
.search-box input { border: none; outline: none; padding: 6px 14px; width: 240px; font-size: 14px; }
.search-box button { border: none; background: #2563eb; color: #fff; padding: 6px 16px; border-radius: 16px; cursor: pointer; }

.cat-bar { display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap; }
.cat-bar button { border: none; background: #fff; border: 1px solid #e2e8f0; padding: 7px 16px; border-radius: 16px; font-size: 13px; cursor: pointer; color: #64748b; transition: all .2s; }
.cat-bar button:hover { border-color: #2563eb; color: #2563eb; }
.cat-bar button.active { background: #2563eb; color: #fff; border-color: #2563eb; }

.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.goods-card { background: #fff; border-radius: 16px; overflow: hidden; cursor: pointer; transition: all .2s; }
.goods-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.goods-img { height: 160px; display: flex; align-items: center; justify-content: center; font-size: 56px; background: #f1f5f9; }
.goods-body { padding: 12px 14px 14px; }
.goods-title { font-size: 14px; color: #1e293b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.goods-meta { font-size: 12px; color: #94a3b8; margin-top: 4px; }
.goods-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.goods-price { color: #e11d48; font-size: 18px; font-weight: 700; }
.fav { border: none; background: none; font-size: 16px; cursor: pointer; }

.empty { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty h3 { color: #475569; margin-bottom: 8px; }
.empty p { color: #94a3b8; font-size: 14px; }

.footer { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; border-top: 1px solid #f0f0f0; margin-top: 40px; }

@media (max-width: 900px) {
  .goods-grid { grid-template-columns: repeat(2, 1fr); }
  .nav-links { display: none; }
  .page-head { flex-direction: column; gap: 16px; align-items: flex-start; }
}
</style>
