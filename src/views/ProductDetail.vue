<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProductById } from '../api/product.js'
import { addFavorite, removeFavorite } from '../api/favorites.js'

const route = useRoute()
const router = useRouter()

const product = ref(null)
const loading = ref(false)
const isFav = ref(false)

// 根据路由中的商品 ID 从接口获取商品
async function loadDetail() {
  loading.value = true
  try {
    product.value = await fetchProductById(route.params.id)
  } catch (e) {
    ElMessage.error(String(e))
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)

// 收藏 / 取消收藏
async function toggleFavorite() {
  if (!product.value) return
  try {
    if (isFav.value) {
      await removeFavorite(product.value.id)
      isFav.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      isFav.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    ElMessage.error(String(e))
  }
}

// 返回商品市场
function goBack() {
  router.push('/product')
}
</script>

<template>
  <div class="detail-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="nav-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🛍️</span><span class="logo-text">校园集市</span>
        </div>
        <nav class="nav-links">
          <router-link to="/" exact-active-class="active">首页</router-link>
          <router-link to="/product" active-class="active">商品市场</router-link>
          <router-link to="/publish" active-class="active">发布商品</router-link>
          <router-link to="/categories" active-class="active">分类浏览</router-link>
          <router-link to="/ai" active-class="active">AI助手</router-link>
          <router-link to="/profile" active-class="active">个人中心</router-link>
          <router-link to="/admin" active-class="active">管理后台</router-link>
        </nav>
        <div class="nav-actions">
          <button class="back-btn" @click="goBack">← 返回</button>
        </div>
      </div>
    </header>

    <!-- 商品详情主体 -->
    <main class="detail-container">
      <div v-if="product" class="detail-card">
        <!-- 商品图片区域 -->
        <div class="product-image">
          {{ product.icon }}
        </div>

        <!-- 商品信息区域 -->
        <div class="product-info">
          <span class="category">
            {{ product.category }}
          </span>

          <h1>{{ product.name }}</h1>

          <div class="price">¥{{ product.price }}</div>

          <h3>商品描述</h3>
          <p class="description">
            {{ product.description }}
          </p>

          <!-- 商品补充信息 -->
          <div class="product-extra">
            <p>
              <span class="extra-label">商品成色：</span>
              {{ product.condition }}
            </p>

            <p>
              <span class="extra-label">卖家：</span>
              {{ product.seller }}
            </p>

            <p>
              <span class="extra-label">交易地点：</span>
              {{ product.location }}
            </p>
          </div>

          <!-- 提示信息 -->
          <div class="notice">
            当前数据来自前端接口层（Mock 模式），接入后端后将读取真实商品数据。
          </div>

          <div class="detail-actions">
            <button
              class="fav-btn"
              :class="{ active: isFav }"
              @click="toggleFavorite"
            >
              {{ isFav ? '❤️ 已收藏' : '🤍 收藏' }}
            </button>

            <button class="back-btn" @click="goBack">
              返回商品列表
            </button>
          </div>
        </div>
      </div>

      <!-- 商品不存在时的提示 -->
      <div v-else class="not-found">
        <h2>没有找到该商品</h2>
        <p>该商品可能不存在，请返回商品市场查看。</p>

        <button class="back-btn" @click="goBack">
          返回商品市场
        </button>
      </div>
    </main>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.detail-page {
  min-height: 100vh;
  background: #faf9f7;
  color: #333;
}

/* 顶部导航 */
.navbar { position: sticky; top: 0; z-index: 100; background: rgba(255,255,255,0.92); backdrop-filter: blur(16px); border-bottom: 1px solid #f0f0f0; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; justify-content: space-between; }
.logo { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 18px; font-weight: 800; }
.logo-icon { font-size: 22px; }
.nav-links { display: flex; gap: 20px; }
.nav-links a { color: #64748b; font-size: 14px; }
.nav-links a:hover, .nav-links a.active { color: #0f172a; font-weight: 600; }
.nav-actions { display: flex; align-items: center; gap: 12px; }
.back-btn { background: #fff; border: 1px solid #e2e8f0; padding: 6px 14px; border-radius: 16px; font-size: 13px; cursor: pointer; color: #475569; }
.back-btn:hover { border-color: #2563eb; color: #2563eb; }

/* 页面主体 */
.detail-container {
  width: 84%;
  max-width: 1100px;
  margin: 40px auto;
}

/* 商品详情卡片 */
.detail-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  padding: 35px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
}

/* 商品图片 */
.product-image {
  min-height: 350px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f5f4, #fafaf9);
  border-radius: 12px;
  font-size: 120px;
}

/* 商品信息 */
.product-info {
  padding: 10px 0;
}

.category {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 15px;
  background: #ffedd5;
  color: #c2410c;
  font-size: 13px;
}

.product-info h1 {
  margin: 20px 0;
  font-size: 30px;
  overflow-wrap: anywhere;
}

.price {
  margin-bottom: 30px;
  color: #ff6b6b;
  font-size: 32px;
  font-weight: bold;
}

.product-info h3 {
  margin-bottom: 12px;
}

.product-info p {
  color: #666;
  line-height: 1.8;
}

.description {
  overflow-wrap: anywhere;
}

/* 商品补充信息 */
.product-extra {
  margin-top: 20px;
  padding: 15px;
  background: #faf9f7;
  border-radius: 10px;
}

.product-extra p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
  overflow-wrap: anywhere;
}

.extra-label {
  color: #333;
  font-weight: 600;
}

/* 演示提示 */
.notice {
  margin: 25px 0;
  padding: 12px;
  background: #faf9f7;
  border-radius: 8px;
  color: #777;
  font-size: 13px;
  line-height: 1.7;
}

/* 返回按钮 */
.back-btn {
  border: none;
  padding: 10px 18px;
  border-radius: 8px;
  background: #c2410c;
  color: #fff;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #9a3412;
}

/* 详情页操作按钮 */
.detail-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.fav-btn {
  border: 1px solid #ffb3b3;
  background: #fff;
  color: #ff6b6b;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: 0.2s;
}

.fav-btn:hover {
  background: #fff0f0;
}

.fav-btn.active {
  background: #fff0f0;
  border-color: #ff6b6b;
}

/* 商品不存在 */
.not-found {
  padding: 60px 20px;
  text-align: center;
  background: #fff;
  border-radius: 16px;
}

.not-found p {
  margin: 20px 0;
  color: #777;
}

/* 手机端适配 */
@media (max-width: 700px) {
  .navbar {
    padding: 0 5%;
  }

  .logo {
    font-size: 17px;
  }

  .detail-container {
    width: 92%;
    margin: 25px auto;
  }

  .detail-card {
    grid-template-columns: 1fr;
    padding: 20px;
    gap: 20px;
  }

  .product-image {
    min-height: 240px;
    font-size: 90px;
  }

  .product-info h1 {
    font-size: 25px;
  }

  .price {
    font-size: 28px;
  }
}
</style>
