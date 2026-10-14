<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 当前直接使用本地响应式数据源；接入后端后改为：
//   import { fetchProductById } from '../../api/product.js'
import { productList } from '../data/products.js'

const route = useRoute()
const router = useRouter()

// 根据路由中的商品 ID 获取商品信息
const product = computed(() => {
  return productList.value.find(
    item => item.id === Number(route.params.id)
  )
})

// 返回商品市场
function goBack() {
  router.push('/product')
}
</script>

<template>
  <div class="detail-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="logo">
        <span>🛍️</span>
        <span>校园二手集市</span>
      </div>

      <button class="back-btn" @click="goBack">
        返回商品市场
      </button>
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
            当前为前端演示数据，暂未连接真实卖家和交易接口。
          </div>

          <button class="back-btn" @click="goBack">
            返回商品列表
          </button>
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
.navbar {
  height: 70px;
  padding: 0 8%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
}

.logo span:first-child {
  font-size: 26px;
}

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
