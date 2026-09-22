<script setup>
import { ref, computed } from 'vue'

const searchText = ref('')
const activeCategory = ref('全部')

const categories = ['全部', '数码产品', '教材书籍', '生活用品', '运动用品']

const products = [
  {
    id: 1,
    name: '高等数学教材',
    price: 20,
    category: '教材书籍',
    icon: '📚',
    description: '九成新，适合大一数学学习'
  },
  {
    id: 2,
    name: '蓝牙耳机',
    price: 50,
    category: '数码产品',
    icon: '🎧',
    description: '音质清晰，使用状态良好'
  },
  {
    id: 3,
    name: '宿舍椅子',
    price: 30,
    category: '生活用品',
    icon: '🪑',
    description: '宿舍闲置椅子，结实耐用'
  },
  {
    id: 4,
    name: '机械键盘',
    price: 80,
    category: '数码产品',
    icon: '⌨️',
    description: '手感舒适，适合学习和办公'
  },
  {
    id: 5,
    name: '英语四级词汇书',
    price: 15,
    category: '教材书籍',
    icon: '📖',
    description: '备考四级必备，几乎全新'
  },
  {
    id: 6,
    name: '宿舍台灯',
    price: 25,
    category: '生活用品',
    icon: '💡',
    description: '三档亮度，晚上学习很方便'
  },
  {
    id: 7,
    name: '篮球',
    price: 45,
    category: '运动用品',
    icon: '🏀',
    description: '使用次数较少，适合校园运动'
  },
  {
    id: 8,
    name: '运动水杯',
    price: 18,
    category: '运动用品',
    icon: '🥤',
    description: '大容量水杯，方便携带'
  }
]

const filteredProducts = computed(() => {
  return products.filter((product) => {
    const matchCategory =
      activeCategory.value === '全部' ||
      product.category === activeCategory.value

    const matchSearch =
      product.name.includes(searchText.value) ||
      product.description.includes(searchText.value)

    return matchCategory && matchSearch
  })
})
</script>

<template>
  <div class="market-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <nav class="nav-menu">

  <router-link to="/">
    首页
  </router-link>

  <router-link to="/product">
    商品市场
  </router-link>

  <router-link to="/ai">
    AI助手
  </router-link>

  <router-link to="/profile">
    个人中心
  </router-link>

</nav>
    </header>

    <!-- 页面主体 -->
    <main class="market-container">
      <!-- 标题区域 -->
      <section class="market-header">
        <div>
          <h1>商品市场</h1>
          <p>发现校园里的好物，让闲置物品重新发挥价值</p>
        </div>

        <div class="search-box">
          <span>🔍</span>
          <input
            v-model="searchText"
            type="text"
            placeholder="搜索你想要的商品..."
          />
        </div>
      </section>

      <!-- 分类 -->
      <section class="category-section">
        <span class="category-title">商品分类：</span>

        <button
          v-for="category in categories"
          :key="category"
          :class="{ active: activeCategory === category }"
          @click="activeCategory = category"
        >
          {{ category }}
        </button>
      </section>

      <!-- 商品区域 -->
      <section class="product-section">
        <div class="section-title">
          <h2>全部商品</h2>
          <span>共 {{ filteredProducts.length }} 件商品</span>
        </div>

        <div v-if="filteredProducts.length > 0" class="product-grid">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="product-card"
          >
            <div class="product-image">
              {{ product.icon }}
            </div>

            <div class="product-info">
              <span class="product-category">
                {{ product.category }}
              </span>

              <h3>{{ product.name }}</h3>

              <p>{{ product.description }}</p>

              <div class="product-bottom">
                <span class="price">¥{{ product.price }}</span>
                <button class="detail-btn">查看详情</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 没有商品时显示 -->
        <div v-else class="empty">
          <div>🔎</div>
          <h3>没有找到相关商品</h3>
          <p>换个关键词试试看吧</p>
        </div>
      </section>
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>校园二手集市 · 让闲置物品流动起来</p>
    </footer>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.market-page {
  min-height: 100vh;
  background: #f7f7fb;
  color: #333;
}

/* 顶部导航 */
.navbar {
  height: 70px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 21px;
  font-weight: bold;
  color: #333;
}

.logo-icon {
  font-size: 28px;
}

.nav-menu {
  display: flex;
  gap: 35px;
}

.nav-menu a {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  font-size: 15px;
  padding: 8px 0;
}

.nav-menu a:hover,
.nav-menu a.active {
  color: #6c5ce7;
}

/* 页面主体 */
.market-container {
  width: 84%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 45px 0 60px;
}

/* 标题 */
.market-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 35px;
}

.market-header h1 {
  margin: 0 0 10px;
  font-size: 34px;
  color: #333;
}

.market-header p {
  margin: 0;
  color: #888;
}

/* 搜索框 */
.search-box {
  width: 350px;
  height: 48px;
  background: #fff;
  border: 1px solid #e8e8ef;
  border-radius: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
}

.search-box span {
  font-size: 18px;
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #333;
}

.search-box input::placeholder {
  color: #aaa;
}

/* 分类 */
.category-section {
  background: #fff;
  padding: 20px 24px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 35px;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.04);
}

.category-title {
  color: #666;
  font-size: 14px;
  margin-right: 5px;
}

.category-section button {
  border: none;
  background: #f5f5f8;
  color: #666;
  padding: 9px 18px;
  border-radius: 20px;
  cursor: pointer;
  transition: 0.2s;
}

.category-section button:hover {
  color: #6c5ce7;
}

.category-section button.active {
  background: #6c5ce7;
  color: white;
}

/* 商品标题 */
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title h2 {
  margin: 0;
  font-size: 24px;
}

.section-title span {
  color: #999;
  font-size: 14px;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
}

/* 商品卡片 */
.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: 0.25s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 170px;
  background: linear-gradient(135deg, #eeeaff, #f5f3ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 70px;
}

.product-info {
  padding: 18px;
}

.product-category {
  color: #6c5ce7;
  font-size: 12px;
  background: #f0edff;
  padding: 4px 9px;
  border-radius: 10px;
}

.product-info h3 {
  margin: 13px 0 8px;
  font-size: 17px;
  color: #333;
}

.product-info p {
  margin: 0;
  height: 40px;
  color: #999;
  font-size: 13px;
  line-height: 20px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18px;
}

.price {
  color: #ff6b6b;
  font-size: 21px;
  font-weight: bold;
}

.detail-btn {
  border: none;
  background: #6c5ce7;
  color: white;
  padding: 7px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
}

.detail-btn:hover {
  background: #5b4bd6;
}

/* 没有商品 */
.empty {
  text-align: center;
  padding: 80px 20px;
  background: #fff;
  border-radius: 16px;
}

.empty div {
  font-size: 50px;
}

.empty h3 {
  margin: 15px 0 8px;
}

.empty p {
  color: #999;
}

/* 底部 */
.footer {
  background: #fff;
  text-align: center;
  padding: 25px;
  color: #999;
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 900px) {
  .navbar {
    padding: 0 4%;
  }

  .market-container {
    width: 92%;
  }

  .market-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 25px;
  }

  .search-box {
    width: 100%;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .nav-menu {
    gap: 12px;
  }

  .nav-menu a {
    font-size: 13px;
  }

  .category-section {
    flex-wrap: wrap;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>