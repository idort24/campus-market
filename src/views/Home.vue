<template>
  <div class="home-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <nav class="nav-menu">
        <a class="active">首页</a>
        <a>商品市场</a>
        <a href="#/ai">AI助手</a>
        <a>个人中心</a>
      </nav>
    </header>

    <!-- 首页主要内容 -->
    <main>
      <!-- 欢迎区域 -->
      <section class="hero">
        <div class="hero-content">
          <p class="welcome">👋 欢迎来到校园二手集市</p>

          <h1>
            让闲置物品<br />
            <span>重新找到价值</span>
          </h1>

          <p class="hero-description">
            专为大学生打造的校园二手交易平台，
            轻松寻找、发布和分享校园好物。
          </p>

          <div class="hero-buttons">
            <button class="primary-btn">🛍️ 开始逛逛</button>
            <a class="secondary-btn" href="#/ai">🤖 AI 智能助手</a>
          </div>
        </div>

        <div class="hero-card">
          <div class="card-icon">📚</div>
          <h3>校园好物</h3>
          <p>教材 · 电子产品 · 生活用品</p>

          <div class="mini-products">
            <div>📖 高数教材</div>
            <div>🎧 蓝牙耳机</div>
            <div>🪑 宿舍用品</div>
          </div>
        </div>
      </section>

      <!-- 功能入口 -->
      <section class="features">
        <div class="section-title">
          <p>EXPLORE</p>
          <h2>你想做什么？</h2>
        </div>

        <div class="feature-grid">
          <div class="feature-card">
            <div class="feature-icon">🛒</div>
            <h3>浏览商品</h3>
            <p>发现身边同学发布的二手好物</p>
            <button>去逛逛 →</button>
          </div>

          <div class="feature-card">
            <div class="feature-icon">🤖</div>
            <h3>AI 智能助手</h3>
            <p>告诉 AI 你的需求，快速寻找商品</p>
            <a href="#/ai">开始咨询 →</a>
          </div>

          <div class="feature-card">
            <div class="feature-icon">📦</div>
            <h3>发布商品</h3>
            <p>把闲置物品发布出来，寻找新主人</p>
            <button>发布商品 →</button>
          </div>

          <div class="feature-card">
            <div class="feature-icon">👤</div>
            <h3>个人中心</h3>
            <p>查看我的商品、收藏和交易记录</p>
            <button>进入我的 →</button>
          </div>
        </div>
      </section>

      <!-- 推荐商品 -->
      <section class="recommend">
        <div class="section-title">
          <p>RECOMMEND</p>
          <h2>热门商品</h2>
        </div>

        <div class="product-grid">
          <div class="product-card">
            <div class="product-image">📚</div>
            <div class="product-info">
              <h3>高等数学教材</h3>
              <p>九成新 · 大一教材</p>
              <strong>¥20</strong>
            </div>
          </div>

          <div class="product-card">
            <div class="product-image">🎧</div>
            <div class="product-info">
              <h3>蓝牙耳机</h3>
              <p>使用半年 · 功能正常</p>
              <strong>¥50</strong>
            </div>
          </div>

          <div class="product-card">
            <div class="product-image">🪑</div>
            <div class="product-info">
              <h3>宿舍椅子</h3>
              <p>成色良好 · 自提</p>
              <strong>¥30</strong>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer>
      <p>© 2026 校园二手集市 · 让闲置物品流动起来</p>
    </footer>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f7f8fc;
  color: #202124;
}

/* 导航栏 */
.navbar {
  height: 70px;
  padding: 0 8%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border-bottom: 1px solid #eeeeee;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
}

.logo-icon {
  font-size: 28px;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-menu a {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  font-size: 15px;
}

.nav-menu .active {
  color: #6c5ce7;
  font-weight: 700;
}

/* 欢迎区域 */
.hero {
  padding: 80px 10%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
  background: linear-gradient(135deg, #f2efff, #ffffff);
}

.hero-content {
  max-width: 600px;
}

.welcome {
  color: #6c5ce7;
  font-weight: 600;
  font-size: 16px;
}

.hero h1 {
  margin: 15px 0;
  font-size: 52px;
  line-height: 1.2;
}

.hero h1 span {
  color: #6c5ce7;
}

.hero-description {
  color: #666;
  font-size: 17px;
  line-height: 1.8;
}

.hero-buttons {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.primary-btn,
.secondary-btn {
  padding: 13px 24px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  font-size: 15px;
  text-decoration: none;
  display: inline-block;
}

.primary-btn {
  background: #6c5ce7;
  color: white;
}

.secondary-btn {
  background: white;
  color: #6c5ce7;
  border: 1px solid #6c5ce7;
}

/* 右侧卡片 */
.hero-card {
  width: 330px;
  padding: 35px;
  border-radius: 25px;
  background: white;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.card-icon {
  font-size: 70px;
}

.hero-card h3 {
  font-size: 25px;
}

.hero-card p {
  color: #777;
}

.mini-products {
  margin-top: 25px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.mini-products div {
  padding: 12px;
  border-radius: 10px;
  background: #f7f7fb;
  text-align: left;
}

/* 功能区域 */
.features,
.recommend {
  padding: 70px 10%;
}

.section-title {
  text-align: center;
  margin-bottom: 35px;
}

.section-title p {
  color: #6c5ce7;
  font-size: 13px;
  letter-spacing: 2px;
}

.section-title h2 {
  font-size: 32px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.feature-card {
  background: white;
  padding: 25px;
  border-radius: 16px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.04);
}

.feature-icon {
  font-size: 38px;
}

.feature-card h3 {
  margin-bottom: 8px;
}

.feature-card p {
  color: #777;
  line-height: 1.6;
  min-height: 50px;
}

.feature-card button,
.feature-card a {
  border: none;
  background: none;
  color: #6c5ce7;
  cursor: pointer;
  padding: 0;
  font-weight: 600;
  text-decoration: none;
}

/* 商品 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
}

.product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
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

.product-info p {
  color: #777;
}

.product-info strong {
  color: #6c5ce7;
  font-size: 22px;
}

/* 页脚 */
footer {
  padding: 30px;
  text-align: center;
  color: #888;
  background: white;
}

/* 简单适配手机 */
@media (max-width: 900px) {
  .hero {
    flex-direction: column;
  }

  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-grid {
    grid-template-columns: 1fr;
  }

  .nav-menu {
    display: none;
  }
}

@media (max-width: 600px) {
  .hero h1 {
    font-size: 38px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>