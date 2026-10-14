<template>
  <div class="home-page">

    <!-- ==================== 顶部导航 ==================== -->
    <header class="navbar">

      <!-- 左侧 Logo -->
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <!-- 右侧导航 -->
      <nav class="nav-menu">

        <!-- 首页 -->
        <router-link
          to="/"
          exact-active-class="active"
        >
          首页
        </router-link>

        <!-- 商品市场 -->
        <router-link
          to="/product"
          active-class="active"
        >
          商品市场
        </router-link>

        <!-- 发布商品 -->
        <router-link
          to="/publish"
          active-class="active"
        >
          发布商品
        </router-link>

        <!-- 分类管理 -->
        <router-link
          to="/categories"
          active-class="active"
        >
          分类管理
        </router-link>

        <!-- AI助手 -->
        <router-link
          to="/ai"
          active-class="active"
        >
          AI助手
        </router-link>

        <!-- 个人中心 -->
        <router-link
          to="/profile"
          active-class="active"
        >
          个人中心
        </router-link>

      </nav>

    </header>


    <!-- ==================== 首页主体 ==================== -->
    <main>

      <!-- ==================== 欢迎区域 ==================== -->
      <section class="hero">

        <!-- 左边文字 -->
        <div class="hero-left">

          <p class="welcome">
            👋 欢迎来到校园二手集市
          </p>

          <h1>
            让闲置物品
            <br />
            <span>重新找到价值</span>
          </h1>

          <p class="desc">
            专为大学生打造的校园二手交易平台，
            轻松寻找、发布和分享校园好物。
          </p>

          <!-- 两个按钮 -->
          <div class="buttons">

            <!-- 去商品市场 -->
            <router-link
              to="/product"
              class="primary-btn"
            >
              🛍️ 开始逛逛
            </router-link>

            <!-- 去 AI -->
            <router-link
              to="/ai"
              class="secondary-btn"
            >
              🤖 AI智能助手
            </router-link>

          </div>

        </div>


        <!-- 右边卡片 -->
        <div class="hero-card">

          <div class="card-icon">
            📚
          </div>

          <h2>
            校园好物
          </h2>

          <p class="card-description">
            教材 · 电子产品 · 生活用品
          </p>

          <div class="products">

            <div>
              📖 高数教材
            </div>

            <div>
              🎧 蓝牙耳机
            </div>

            <div>
              🪑 宿舍用品
            </div>

          </div>

        </div>

      </section>


      <!-- ==================== 功能区域 ==================== -->
      <section class="features">

        <div class="section-title">
          <p>EXPLORE</p>
          <h2>你想做什么？</h2>
        </div>


        <div class="feature-grid">

          <!-- 浏览商品 -->
          <div class="feature-card">

            <div class="feature-icon">
              🛒
            </div>

            <h3>
              浏览商品
            </h3>

            <p>
              发现校园二手商品
            </p>

            <router-link
              to="/product"
              class="feature-link"
            >
              去逛逛 →
            </router-link>

          </div>


          <!-- AI助手 -->
          <div class="feature-card">

            <div class="feature-icon">
              🤖
            </div>

            <h3>
              AI助手
            </h3>

            <p>
              智能寻找商品
            </p>

            <router-link
              to="/ai"
              class="feature-link"
            >
              开始咨询 →
            </router-link>

          </div>


          <!-- 发布商品 -->
          <div class="feature-card">

            <div class="feature-icon">
              📦
            </div>

            <h3>
              发布商品
            </h3>

            <p>
              发布你的闲置物品
            </p>

            <router-link
              to="/publish"
              class="feature-button"
            >
              发布商品 →
            </router-link>

          </div>


          <!-- 个人中心 -->
          <div class="feature-card">

            <div class="feature-icon">
              👤
            </div>

            <h3>
              个人中心
            </h3>

            <p>
              查看我的信息
            </p>

            <router-link
              to="/profile"
              class="feature-link"
            >
              进入我的 →
            </router-link>

          </div>

        </div>

      </section>


      <!-- ==================== 热门商品 ==================== -->
      <section class="recommend">

        <div class="section-title">

          <p>RECOMMEND</p>

          <h2>
            热门商品
          </h2>

        </div>


        <div class="product-grid">

          <!-- 商品1 -->
          <div class="product-card">

            <div class="product-image">
              📚
            </div>

            <div class="product-info">

              <h3>
                高等数学教材
              </h3>

              <p>
                九成新 · 大一教材
              </p>

              <strong>
                ¥20
              </strong>

            </div>

          </div>


          <!-- 商品2 -->
          <div class="product-card">

            <div class="product-image">
              🎧
            </div>

            <div class="product-info">

              <h3>
                蓝牙耳机
              </h3>

              <p>
                使用半年 · 功能正常
              </p>

              <strong>
                ¥50
              </strong>

            </div>

          </div>


          <!-- 商品3 -->
          <div class="product-card">

            <div class="product-image">
              🪑
            </div>

            <div class="product-info">

              <h3>
                宿舍椅子
              </h3>

              <p>
                成色良好 · 自提
              </p>

              <strong>
                ¥30
              </strong>

            </div>

          </div>

        </div>

      </section>

    </main>


    <!-- ==================== 页脚 ==================== -->
    <footer>

      © 2026 校园二手集市 · 让闲置物品流动起来

    </footer>

  </div>
</template>


<script setup>

// 发布商品目前还没有单独页面
// 所以点击后先跳到商品市场
import { useRouter } from 'vue-router'

const router = useRouter()

const goProduct = () => {
  router.push('/product')
}

</script>


<style scoped>

/* ==================== 整体 ==================== */

.home-page {
  min-height: 100vh;
  background: #f7f8fc;
  color: #222;
}


/* ==================== 顶部导航 ==================== */

.navbar {
  height: 70px;

  background: white;

  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 0 10%;

  border-bottom: 1px solid #eeeeee;

  box-sizing: border-box;
}


.logo {
  display: flex;
  align-items: center;

  gap: 10px;

  font-size: 22px;

  font-weight: 700;

  white-space: nowrap;
}


.logo-icon {
  font-size: 28px;
}


.nav-menu {
  display: flex;

  align-items: center;

  gap: 35px;
}


.nav-menu a {
  color: #666;

  text-decoration: none;

  cursor: pointer;

  font-size: 16px;

  transition: 0.2s;
}


.nav-menu a:hover {
  color: #6c5ce7;
}


/* 当前页面 */

.nav-menu a.active {
  color: #6c5ce7;

  font-weight: 700;
}


/* ==================== Hero ==================== */

.hero {
  padding: 80px 10%;

  display: flex;

  justify-content: space-between;

  align-items: center;

  gap: 60px;

  background: linear-gradient(
    135deg,
    #f0edff,
    #ffffff
  );

  box-sizing: border-box;
}


.hero-left {
  max-width: 600px;
}


.welcome {
  color: #6c5ce7;

  font-size: 17px;

  font-weight: 700;

  margin-bottom: 15px;
}


.hero h1 {
  margin: 0;

  font-size: 55px;

  line-height: 1.2;

  font-weight: 700;
}


.hero h1 span {
  color: #6c5ce7;
}


.desc {
  margin-top: 25px;

  font-size: 18px;

  color: #666;

  line-height: 1.8;
}


/* ==================== 按钮 ==================== */

.buttons {
  margin-top: 30px;

  display: flex;

  gap: 20px;

  align-items: center;
}


.primary-btn,
.secondary-btn {
  display: inline-block;

  padding: 14px 25px;

  border-radius: 10px;

  text-decoration: none;

  font-size: 15px;

  transition: 0.2s;

  box-sizing: border-box;
}


.primary-btn {
  background: #6c5ce7;

  color: white;
}


.primary-btn:hover {
  background: #5848d6;

  transform: translateY(-2px);
}


.secondary-btn {
  background: white;

  color: #6c5ce7;

  border: 1px solid #6c5ce7;
}


.secondary-btn:hover {
  background: #f3f1ff;

  transform: translateY(-2px);
}


/* ==================== 右侧卡片 ==================== */

.hero-card {
  width: 320px;

  padding: 35px;

  background: white;

  border-radius: 25px;

  text-align: center;

  box-shadow:
    0 15px 40px rgba(0, 0, 0, 0.08);

  box-sizing: border-box;
}


.card-icon {
  font-size: 70px;
}


.hero-card h2 {
  margin: 10px 0;

  font-size: 28px;
}


.card-description {
  color: #777;

  font-size: 16px;
}


.products {
  margin-top: 25px;

  display: flex;

  flex-direction: column;

  gap: 10px;
}


.products div {
  background: #f5f5ff;

  padding: 12px;

  border-radius: 10px;

  text-align: left;

  transition: 0.2s;
}


.products div:hover {
  background: #ece9ff;
}


/* ==================== 功能区域 ==================== */

.features {
  padding: 70px 10%;

  box-sizing: border-box;
}


.section-title {
  text-align: center;

  margin-bottom: 35px;
}


.section-title p {
  color: #6c5ce7;

  font-size: 13px;

  letter-spacing: 2px;

  margin-bottom: 5px;
}


.section-title h2 {
  margin: 0;

  font-size: 35px;
}


.feature-grid {
  display: grid;

  grid-template-columns: repeat(4, 1fr);

  gap: 20px;
}


.feature-card {
  background: white;

  padding: 25px;

  border-radius: 15px;

  box-shadow:
    0 5px 20px rgba(0, 0, 0, 0.04);

  transition: 0.2s;
}


.feature-card:hover {
  transform: translateY(-5px);

  box-shadow:
    0 10px 25px rgba(0, 0, 0, 0.08);
}


.feature-icon {
  font-size: 40px;

  margin-bottom: 10px;
}


.feature-card h3 {
  margin: 8px 0;

  font-size: 20px;
}


.feature-card p {
  color: #777;

  line-height: 1.6;

  min-height: 50px;
}


.feature-link,
.feature-button {
  display: inline-block;

  color: #6c5ce7;

  text-decoration: none;

  border: none;

  background: none;

  cursor: pointer;

  padding: 0;

  font-size: 15px;

  font-weight: 700;
}


.feature-link:hover,
.feature-button:hover {
  color: #4d3fc4;
}


/* ==================== 推荐商品 ==================== */

.recommend {
  padding: 70px 10%;

  box-sizing: border-box;
}


.product-grid {
  display: grid;

  grid-template-columns: repeat(3, 1fr);

  gap: 25px;
}


.product-card {
  background: white;

  border-radius: 16px;

  overflow: hidden;

  box-shadow:
    0 5px 20px rgba(0, 0, 0, 0.05);

  transition: 0.2s;
}


.product-card:hover {
  transform: translateY(-5px);

  box-shadow:
    0 10px 25px rgba(0, 0, 0, 0.08);
}


.product-image {
  height: 180px;

  display: flex;

  align-items: center;

  justify-content: center;

  background: #f0efff;

  font-size: 70px;
}


.product-info {
  padding: 20px;
}


.product-info h3 {
  margin-top: 0;

  font-size: 20px;
}


.product-info p {
  color: #777;
}


.product-info strong {
  color: #6c5ce7;

  font-size: 22px;
}


/* ==================== 页脚 ==================== */

footer {
  background: white;

  padding: 30px;

  text-align: center;

  color: #888;

  border-top: 1px solid #eeeeee;
}


/* ==================== 响应式 ==================== */

@media (max-width: 1000px) {

  .hero {
    flex-direction: column;

    text-align: center;
  }


  .hero-card {
    width: 100%;

    max-width: 400px;
  }


  .buttons {
    justify-content: center;
  }


  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }


  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }

}


@media (max-width: 700px) {

  .navbar {
    padding: 0 5%;
  }


  .nav-menu {
    gap: 15px;
  }


  .nav-menu a {
    font-size: 14px;
  }


  .hero {
    padding: 60px 5%;
  }


  .hero h1 {
    font-size: 40px;
  }


  .features,
  .recommend {
    padding: 50px 5%;
  }


  .feature-grid,
  .product-grid {
    grid-template-columns: 1fr;
  }

}


@media (max-width: 500px) {

  .logo {
    font-size: 17px;
  }


  .logo-icon {
    font-size: 22px;
  }


  .nav-menu {
    gap: 8px;
  }


  .nav-menu a {
    font-size: 12px;
  }


  .hero h1 {
    font-size: 34px;
  }


  .buttons {
    flex-direction: column;
  }


  .primary-btn,
  .secondary-btn {
    width: 100%;

    text-align: center;
  }

}

</style>