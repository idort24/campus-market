<template>
  <div class="profile-page">

    <header class="navbar">
      <div class="logo">
        🛍️ 校园二手集市
      </div>

      <nav class="nav-menu">

        <router-link to="/">
          首页
        </router-link>

        <router-link to="/product">
          商品市场
        </router-link>

        <router-link to="/publish">
          发布商品
        </router-link>

        <router-link to="/categories">
          分类管理
        </router-link>

        <router-link to="/ai">
          AI助手
        </router-link>

        <router-link to="/file-transfer">
          文件传输
        </router-link>

        <router-link to="/profile">
          个人中心
        </router-link>

      </nav>
    </header>


    <main class="profile-content">

      <div class="profile-card">

        <div class="avatar">
          👤
        </div>

        <h1>个人中心</h1>

        <p>欢迎来到校园二手集市个人中心</p>

        <div class="info">

          <div>
            <span>用户名</span>
            <strong>校园用户</strong>
          </div>

          <div>
            <span>我的商品</span>
            <strong>0 件</strong>
          </div>

          <div>
            <span>我的收藏</span>
            <strong>0 件</strong>
          </div>

        </div>

      </div>

    </main>

  </div>
</template>


<style scoped>

.profile-page {
  min-height: 100vh;
  background: #f7f8fc;
  color: #222;
}


/* 顶部导航 */

.navbar {
  height: 70px;
  background: white;

  display: flex;
  align-items: center;
  justify-content: space-between;

  padding: 0 10%;

  border-bottom: 1px solid #eee;
}

.logo {
  font-size: 22px;
  font-weight: bold;
}

.nav-menu {
  display: flex;
  gap: 35px;
}

.nav-menu a {
  text-decoration: none;
  color: #666;
}

.nav-menu a.router-link-active {
  color: #6c5ce7;
  font-weight: bold;
}


/* 页面 */

.profile-content {
  padding: 80px 10%;
  display: flex;
  justify-content: center;
}


/* 卡片 */

.profile-card {
  width: 600px;
  background: white;
  border-radius: 20px;
  padding: 50px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}

.avatar {
  width: 90px;
  height: 90px;

  margin: 0 auto 20px;

  border-radius: 50%;

  background: #eee9ff;

  display: flex;
  align-items: center;
  justify-content: center;

  font-size: 45px;
}

.profile-card h1 {
  font-size: 32px;
  margin-bottom: 10px;
}

.profile-card p {
  color: #888;
}


/* 信息 */

.info {
  margin-top: 35px;

  display: grid;
  grid-template-columns: repeat(3, 1fr);

  gap: 20px;
}

.info div {
  background: #f7f5ff;

  padding: 20px;

  border-radius: 12px;
}

.info span {
  display: block;

  color: #888;

  font-size: 14px;

  margin-bottom: 8px;
}

.info strong {
  color: #6c5ce7;
  font-size: 18px;
}


@media (max-width: 700px) {

  .navbar {
    padding: 0 5%;
  }

  .nav-menu {
    gap: 15px;
  }

  .profile-card {
    width: auto;
    padding: 30px;
  }

  .info {
    grid-template-columns: 1fr;
  }

}

</style>