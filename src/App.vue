<template>
  <router-view />
  <!-- AI 悬浮球 -->
  <router-link to="/ai" class="fab-ai" title="AI 助手">
    <span class="fab-ai-icon">🤖</span>
  </router-link>
</template>

<style>
.fab-ai {
  position: fixed; right: 28px; bottom: 28px; z-index: 999;
  width: 56px; height: 56px; border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #06b6d4);
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 24px rgba(37,99,235,0.35);
  transition: all .2s; text-decoration: none;
}
.fab-ai:hover { transform: scale(1.1); }
.fab-ai-icon { font-size: 26px; }
</style>
