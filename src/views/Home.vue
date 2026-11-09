<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { ROOT_CATEGORIES } from '../data/categories.js'
import { fetchProducts } from '../api/product.js'

const router = useRouter()
const search = ref('')

// 首页"为你推荐"走产品接口层：USE_MOCK=true 取本地数据，切 false 后取后端 /goods
// 注意不要直接 import data/products.js，那样切了开关首页也不会变
const recommend = ref([])
const REC_BG = ['#dbeafe', '#dcfce7', '#fef3c7', '#fce7f3']

async function loadRecommend() {
  try {
    const list = await fetchProducts({ pageSize: 4 })
    recommend.value = (list || []).slice(0, 4)
  } catch (e) {
    recommend.value = []
  }
}

function doSearch() {
  router.push({ path: '/product', query: search.value ? { keyword: search.value } : {} })
}

// 权限被拦截后回首页时给一次提示（守卫通过 sessionStorage 传递）
onMounted(() => {
  loadRecommend()
  const denied = sessionStorage.getItem('cm_denied')
  if (denied) {
    sessionStorage.removeItem('cm_denied')
    if (denied === 'admin') {
      ElMessage.warning('当前账号没有管理后台访问权限')
    } else {
      ElMessage.warning(`当前账号缺少权限：${denied}`)
    }
  }
})
</script>

<template>
  <div class="home-page">
    <NavBar />

    <!-- Hero 搜索区 -->
    <section class="hero">
      <div class="hero-inner">
        <h1>校园闲置，<em>就近淘</em></h1>
        <p class="hero-sub">教材 · 电子产品 · 生活用品，同校交易更放心</p>
        <div class="search-bar">
          <input v-model="search" @keyup.enter="doSearch" placeholder="搜搜你想要的…" />
          <button @click="doSearch">搜索</button>
        </div>
        <div class="hero-tags">
          <span># 高数教材</span><span># 蓝牙耳机</span><span># 宿舍好物</span><span># 考研资料</span>
        </div>
      </div>
    </section>

    <!-- 分类入口 -->
    <section class="section">
      <div class="section-head"><h2>热门分类</h2></div>
      <div class="cat-grid">
        <div
          v-for="c in ROOT_CATEGORIES"
          :key="c.id"
          class="cat-item"
          @click="router.push({ path: '/categories', query: { id: c.id } })"
        >
          <div class="cat-icon">{{ c.icon }}</div><span>{{ c.name }}</span>
        </div>
        <div class="cat-item" @click="router.push('/categories')">
          <div class="cat-icon">➕</div><span>全部分类</span>
        </div>
      </div>
    </section>

    <!-- 商品流 -->
    <section class="section">
      <div class="section-head">
        <h2>为你推荐</h2>
        <router-link to="/product" class="more-link">查看全部 →</router-link>
      </div>
      <div class="goods-grid">
        <div
          v-for="(p, i) in recommend"
          :key="p.id"
          class="goods-card"
          @click="router.push(`/product/${p.id}`)"
        >
          <div class="goods-img" :style="{ background: REC_BG[i % 4] }">{{ p.icon }}</div>
          <div class="goods-body">
            <div class="goods-title">{{ p.name }}</div>
            <div class="goods-meta">{{ p.category }} · {{ p.location }}</div>
            <div class="goods-price">¥{{ p.price }}</div>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">© 2026 校园二手集市 · 让闲置流动起来</footer>
  </div>
</template>

<style scoped>
.home-page { min-height: 100vh; background: #fafbfc; }

/* Hero */
.hero {
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdf4 100%);
  padding: 64px 24px 56px;
}
.hero-inner { max-width: 720px; margin: 0 auto; text-align: center; }
.hero h1 { font-size: 40px; font-weight: 800; color: #0f172a; letter-spacing: -1px; line-height: 1.2; margin: 0; }
.hero h1 em { font-style: normal; color: #2563eb; }
.hero-sub { color: #64748b; margin-top: 12px; font-size: 15px; }
.search-bar {
  margin: 28px auto 0; max-width: 520px;
  display: flex; background: #fff; border-radius: 24px; padding: 4px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06); border: 1px solid #e2e8f0;
}
.search-bar input {
  flex: 1; border: none; outline: none; padding: 10px 16px; font-size: 14px; background: transparent;
}
.search-bar button {
  background: #2563eb; color: #fff; border: none; padding: 10px 24px;
  border-radius: 20px; font-size: 14px; font-weight: 500; cursor: pointer;
}
.search-bar button:hover { background: #1d4ed8; }
.hero-tags { margin-top: 16px; display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.hero-tags span { font-size: 12px; color: #94a3b8; }

/* 通用 section */
.section { max-width: 1200px; margin: 0 auto; padding: 40px 24px; }
.section-head { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 20px; }
.section-head h2 { font-size: 20px; font-weight: 700; color: #0f172a; margin: 0; }
.more-link { font-size: 13px; color: #64748b; text-decoration: none; }
.more-link:hover { color: #2563eb; }

/* 分类宫格 */
.cat-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 16px; }
.cat-item { text-align: center; cursor: pointer; }
.cat-item:hover .cat-icon { transform: scale(1.08); }
.cat-icon {
  width: 56px; height: 56px; margin: 0 auto 8px;
  background: #fff; border-radius: 16px; display: flex; align-items: center; justify-content: center;
  font-size: 26px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); transition: transform .2s;
}
.cat-item span { font-size: 12px; color: #475569; }

/* 商品流 */
.goods-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.goods-card {
  background: #fff; border-radius: 16px; overflow: hidden; cursor: pointer;
  transition: transform .2s, box-shadow .2s;
}
.goods-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.goods-img { height: 160px; display: flex; align-items: center; justify-content: center; font-size: 56px; }
.goods-body { padding: 12px 14px 14px; }
.goods-title { font-size: 14px; color: #1e293b; line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.goods-meta { font-size: 12px; color: #94a3b8; margin-top: 4px; }
.goods-price { margin-top: 8px; color: #e11d48; font-size: 18px; font-weight: 700; }
.goods-price del { color: #cbd5e1; font-size: 12px; font-weight: 400; margin-left: 4px; }

.footer { text-align: center; padding: 32px; color: #94a3b8; font-size: 13px; border-top: 1px solid #f0f0f0; margin-top: 20px; }

@media (max-width: 900px) {
  .cat-grid { grid-template-columns: repeat(4, 1fr); }
  .goods-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
