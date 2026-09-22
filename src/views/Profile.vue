<template>
  <div class="profile-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <div>
        <h1>个人中心</h1>
        <p>管理你的校园二手市场账户</p>
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="user-card">
      <div class="avatar">
        何
      </div>

      <div class="user-info">
        <h2>何曼妮</h2>
        <p>校园二手市场用户</p>
        <span class="user-tag">校园用户</span>
      </div>

      <button class="edit-btn" @click="showMessage('个人资料编辑功能暂未开放')">
        编辑资料
      </button>
    </div>

    <!-- 数据统计 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-number">8</div>
        <div class="stat-label">我的发布</div>
      </div>

      <div class="stat-item">
        <div class="stat-number">12</div>
        <div class="stat-label">我的收藏</div>
      </div>

      <div class="stat-item">
        <div class="stat-number">5</div>
        <div class="stat-label">已购买</div>
      </div>

      <div class="stat-item">
        <div class="stat-number">3</div>
        <div class="stat-label">待处理</div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="section">
      <h2 class="section-title">我的服务</h2>

      <div class="menu-grid">
        <div
          class="menu-card"
          @click="showMessage('我的发布功能')"
        >
          <div class="menu-icon">📦</div>
          <div>
            <h3>我的发布</h3>
            <p>查看我发布的商品</p>
          </div>
          <span class="arrow">›</span>
        </div>

        <div
          class="menu-card"
          @click="showMessage('我的收藏功能')"
        >
          <div class="menu-icon">❤️</div>
          <div>
            <h3>我的收藏</h3>
            <p>查看收藏的商品</p>
          </div>
          <span class="arrow">›</span>
        </div>

        <div
          class="menu-card"
          @click="showMessage('购买记录功能')"
        >
          <div class="menu-icon">🛒</div>
          <div>
            <h3>购买记录</h3>
            <p>查看我的购买记录</p>
          </div>
          <span class="arrow">›</span>
        </div>

        <div
          class="menu-card"
          @click="showMessage('消息中心功能')"
        >
          <div class="menu-icon">💬</div>
          <div>
            <h3>消息中心</h3>
            <p>查看商品交易消息</p>
          </div>
          <span class="arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 账户设置 -->
    <div class="section">
      <h2 class="section-title">账户设置</h2>

      <div class="setting-card">
        <div
          class="setting-item"
          @click="showMessage('账号安全功能')"
        >
          <div class="setting-left">
            <span class="setting-icon">🔒</span>
            <div>
              <h3>账号安全</h3>
              <p>管理账号安全设置</p>
            </div>
          </div>
          <span class="arrow">›</span>
        </div>

        <div
          class="setting-item"
          @click="showMessage('通知设置功能')"
        >
          <div class="setting-left">
            <span class="setting-icon">🔔</span>
            <div>
              <h3>通知设置</h3>
              <p>管理消息通知</p>
            </div>
          </div>
          <span class="arrow">›</span>
        </div>

        <div
          class="setting-item"
          @click="showMessage('帮助中心功能')"
        >
          <div class="setting-left">
            <span class="setting-icon">❓</span>
            <div>
              <h3>帮助中心</h3>
              <p>查看使用帮助</p>
            </div>
          </div>
          <span class="arrow">›</span>
        </div>
      </div>
    </div>

    <!-- 提示 -->
    <div
      v-if="message"
      class="message"
    >
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const message = ref('')

const showMessage = (text) => {
  message.value = text

  setTimeout(() => {
    message.value = ''
  }, 2000)
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: 40px;
  background: #f5f7fa;
  box-sizing: border-box;
  color: #303133;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 15px;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 28px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: #409eff;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: bold;
  margin-right: 20px;
}

.user-info {
  flex: 1;
}

.user-info h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.user-info p {
  margin: 0 0 10px;
  color: #909399;
}

.user-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  background: #ecf5ff;
  color: #409eff;
  font-size: 13px;
}

.edit-btn {
  border: none;
  background: #409eff;
  color: #ffffff;
  padding: 10px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.edit-btn:hover {
  opacity: 0.9;
}

.stats-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 28px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.stat-item {
  text-align: center;
  border-right: 1px solid #ebeef5;
}

.stat-item:last-child {
  border-right: none;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.section {
  margin-bottom: 28px;
}

.section-title {
  margin: 0 0 16px;
  font-size: 20px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.menu-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}

.menu-card:hover {
  transform: translateY(-2px);
}

.menu-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.menu-card h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.menu-card p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.arrow {
  margin-left: auto;
  font-size: 28px;
  color: #c0c4cc;
}

.setting-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.05);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-left {
  display: flex;
  align-items: center;
}

.setting-icon {
  font-size: 22px;
  margin-right: 16px;
}

.setting-item h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.setting-item p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.message {
  position: fixed;
  left: 50%;
  bottom: 40px;
  transform: translateX(-50%);
  padding: 12px 24px;
  background: #303133;
  color: #ffffff;
  border-radius: 8px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 20px;
  }

  .user-card {
    flex-wrap: wrap;
  }

  .edit-btn {
    margin-top: 20px;
  }

  .stats-card {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .stat-item {
    border-right: none;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>