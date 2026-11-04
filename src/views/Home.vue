<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const goProduct = () => router.push('/product')
</script>

<template>
  <div class="home-page">
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>
      <nav class="nav-menu">
        <router-link to="/" exact-active-class="active">首页</router-link>
        <router-link to="/product" active-class="active">商品市场</router-link>
        <router-link to="/publish" active-class="active">发布商品</router-link>
        <router-link to="/categories" active-class="active">分类浏览</router-link>
        <router-link to="/ai" active-class="active">AI助手</router-link>
        <router-link to="/profile" active-class="active">个人中心</router-link>
        <router-link to="/admin" active-class="active">管理后台</router-link>
      </nav>
    </header>

    <main>
      <section class="hero">
        <div class="hero-left">
          <p class="welcome">👋 欢迎来到校园二手集市</p>
          <h1>让闲置物品<br><span>重新找到价值</span></h1>
          <p class="desc">专为大学生打造的校园二手交易平台，轻松寻找、发布和分享校园好物。</p>
          <div class="buttons">
            <router-link to="/product" class="primary-btn">🛍️ 开始逛逛</router-link>
            <router-link to="/ai" class="secondary-btn">🤖 AI智能助手</router-link>
          </div>
        </div>
        <div class="hero-card">
          <div class="card-icon">📚</div>
          <h2>校园好物</h2>
          <p class="card-description">教材 · 电子产品 · 生活用品</p>
          <div class="products">
            <div>📖 高数教材</div>
            <div>🎧 蓝牙耳机</div>
            <div>🪑 宿舍用品</div>
          </div>
        </div>
      </section>

      <section class="features">
        <div class="section-title">
          <p>EXPLORE</p>
          <h2>你想做什么？</h2>
        </div>
        <div class="feature-grid">
          <div class="feature-card">
            <div class="feature-icon">🛒</div>
            <h3>浏览商品</h3>
            <p>发现校园二手商品</p>
            <router-link to="/product" class="feature-link">去逛逛 →</router-link>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🤖</div>
            <h3>AI助手</h3>
            <p>智能寻找商品</p>
            <router-link to="/ai" class="feature-link">开始咨询 →</router-link>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📦</div>
            <h3>发布商品</h3>
            <p>发布你的闲置物品</p>
            <router-link to="/publish" class="feature-link">发布商品 →</router-link>
          </div>
          <div class="feature-card">
            <div class="feature-icon">👤</div>
            <h3>个人中心</h3>
            <p>查看我的信息</p>
            <router-link to="/profile" class="feature-link">进入我的 →</router-link>
          </div>
        </div>
      </section>

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

    <footer>© 2026 校园二手集市 · 让闲置物品流动起来</footer>
  </div>
</template>

<style scoped>
.home-page { min-height: 100vh; background: var(--bg); color: var(--text); }

/* 导航 */
.navbar {
  position: sticky; top: 0; z-index: 100;
  height: 64px; background: rgba(255,255,255,0.9); backdrop-filter: blur(12px);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 5%; border-bottom: 1px solid var(--border);
}
.logo { display: flex; align-items: center; gap: 8px; font-size: 18px; font-weight: 700; color: var(--text); }
.logo-icon { font-size: 24px; }
.nav-menu { display: flex; gap: 28px; }
.nav-menu a { color: var(--text-secondary); font-size: 14px; transition: color 0.2s; }
.nav-menu a:hover { color: var(--primary); }
.nav-menu a.active { color: var(--primary); font-weight: 600; }

/* Hero */
.hero {
  padding: 80px 5%; display: flex; justify-content: space-between; align-items: center; gap: 60px;
  background: linear-gradient(180deg, var(--primary-light) 0%, var(--bg) 100%);
}
.hero-left { max-width: 580px; }
.welcome { color: var(--primary); font-size: 15px; font-weight: 600; margin-bottom: 16px; }
.hero h1 { margin: 0; font-size: 48px; line-height: 1.2; font-weight: 800; letter-spacing: -1px; }
.hero h1 span { color: var(--primary); }
.desc { margin-top: 20px; font-size: 16px; color: var(--text-secondary); line-height: 1.7; }
.buttons { margin-top: 32px; display: flex; gap: 14px; }
.primary-btn, .secondary-btn {
  display: inline-block; padding: 12px 24px; border-radius: var(--radius-sm);
  font-size: 14px; font-weight: 500; transition: all 0.2s;
}
.primary-btn { background: var(--primary); color: #fff; }
.primary-btn:hover { background: var(--primary-hover); transform: translateY(-1px); }
.secondary-btn { background: #fff; color: var(--primary); border: 1px solid var(--border); }
.secondary-btn:hover { border-color: var(--primary); background: var(--primary-light); }

.hero-card {
  width: 320px; padding: 32px; background: #fff; border-radius: 20px;
  text-align: center; box-shadow: var(--shadow-lg);
}
.card-icon { font-size: 56px; }
.hero-card h2 { margin: 12px 0 4px; font-size: 22px; }
.card-description { color: var(--text-light); font-size: 14px; }
.products { margin-top: 20px; display: flex; flex-direction: column; gap: 8px; }
.products div { background: var(--bg); padding: 10px 14px; border-radius: var(--radius-sm); text-align: left; font-size: 14px; }

/* 功能区 */
.features { padding: 60px 5%; }
.section-title { text-align: center; margin-bottom: 32px; }
.section-title p { color: var(--primary); font-size: 12px; letter-spacing: 2px; margin-bottom: 4px; font-weight: 600; }
.section-title h2 { margin: 0; font-size: 28px; font-weight: 700; }
.feature-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.feature-card {
  background: #fff; padding: 24px; border-radius: 16px; border: 1px solid var(--border);
  transition: all 0.2s;
}
.feature-card:hover { transform: translateY(-3px); box-shadow: var(--shadow); border-color: var(--primary-light); }
.feature-icon { font-size: 36px; margin-bottom: 8px; }
.feature-card h3 { margin: 6px 0; font-size: 17px; }
.feature-card p { color: var(--text-secondary); font-size: 13px; min-height: 40px; }
.feature-link { display: inline-block; color: var(--primary); font-size: 14px; font-weight: 500; margin-top: 8px; }

/* 推荐商品 */
.recommend { padding: 60px 5%; }
.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.product-card {
  background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid var(--border);
  transition: all 0.2s;
}
.product-card:hover { transform: translateY(-3px); box-shadow: var(--shadow); }
.product-image { height: 160px; display: flex; align-items: center; justify-content: center; background: var(--primary-light); font-size: 56px; }
.product-info { padding: 16px; }
.product-info h3 { margin: 0 0 4px; font-size: 16px; }
.product-info p { color: var(--text-light); font-size: 13px; }
.product-info strong { color: var(--primary); font-size: 20px; }

footer { background: #fff; padding: 24px; text-align: center; color: var(--text-light); border-top: 1px solid var(--border); font-size: 13px; }

@media (max-width: 1000px) {
  .hero { flex-direction: column; text-align: center; }
  .buttons { justify-content: center; }
  .feature-grid { grid-template-columns: repeat(2, 1fr); }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 700px) {
  .hero h1 { font-size: 34px; }
  .feature-grid, .product-grid { grid-template-columns: 1fr; }
  .nav-menu { gap: 16px; }
  .nav-menu a { font-size: 13px; }
}
</style>
