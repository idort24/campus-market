<script setup>
import { ref, onMounted } from 'vue'
import { ElTree } from 'element-plus'
import { fetchCategoryTree, fetchCategoryTable } from '../api/category.js'

// el-tree 用的嵌套数据
const treeData = ref([])
// 邻接表原始数据（用于点节点时查 parent_id、子节点等）
const flatTable = ref([])

// 当前选中的节点信息
const selected = ref(null)
// el-tree 高亮当前选中节点
const treeKey = ref(null)

async function loadData() {
  treeData.value = await fetchCategoryTree()
  flatTable.value = await fetchCategoryTable()
}

// 点击树节点
function handleNodeClick(data, node) {
  // data = 当前节点（{id, category_name, parent_id, children}）
  const parentName =
    data.parent_id && data.parent_id !== 0
      ? flatTable.value.find((r) => r.id === data.parent_id)?.category_name ?? '—'
      : '（根分类，无父节点）'

  const childNames = (data.children || []).map((c) => c.category_name)

  selected.value = {
    id: data.id,
    name: data.category_name,
    parentId: data.parent_id,
    parentName,
    children: childNames.length ? childNames : ['（无子分类）'],
    path: getPath(node)
  }
  treeKey.value = data.id
}

// 从根到当前节点的路径，例如：电子产品 / 耳机
function getPath(node) {
  const names = []
  let cur = node
  while (cur && cur.data && cur.data.category_name) {
    names.unshift(cur.data.category_name)
    cur = cur.parent
  }
  return names.join(' / ')
}

onMounted(loadData)
</script>

<template>
  <div class="cat-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <nav class="nav-menu">
        <router-link to="/">首页</router-link>
        <router-link to="/product">商品市场</router-link>
        <router-link to="/publish">发布商品</router-link>
        <router-link to="/categories" active-class="active">分类管理</router-link>
        <router-link to="/ai">AI助手</router-link>
        <router-link to="/file-transfer">文件传输</router-link>
        <router-link to="/profile">个人中心</router-link>
      </nav>
    </header>

    <main class="cat-container">
      <section class="cat-header">
        <h1>商品分类管理</h1>
        <p>基于 Element Plus Tree 组件 · 当前使用本地模拟数据，后续对接 MySQL 分类表</p>
      </section>

      <div class="cat-layout">
        <!-- 左：分类树 -->
        <div class="tree-card">
          <h2>📂 分类树</h2>
          <p class="hint">点击节点查看详情，可展开/折叠父分类</p>
          <el-tree
            :data="treeData"
            :props="{ label: 'category_name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          />
        </div>

        <!-- 右：选中详情 -->
        <div class="info-card">
          <h2>📌 当前选中</h2>

          <div v-if="selected" class="info-body">
            <div class="info-row">
              <span class="label">分类名称</span>
              <span class="value name">{{ selected.name }}</span>
            </div>
            <div class="info-row">
              <span class="label">分类 ID</span>
              <span class="value">{{ selected.id }}</span>
            </div>
            <div class="info-row">
              <span class="label">父分类 ID</span>
              <span class="value">{{ selected.parentId || '0（根节点）' }}</span>
            </div>
            <div class="info-row">
              <span class="label">父分类名</span>
              <span class="value">{{ selected.parentName }}</span>
            </div>
            <div class="info-row">
              <span class="label">层级路径</span>
              <span class="value path">{{ selected.path }}</span>
            </div>
            <div class="info-row">
              <span class="label">子分类</span>
              <span class="value">{{ selected.children.join('、') }}</span>
            </div>
          </div>

          <div v-else class="empty-tip">
            👈 点击左侧任意分类节点
          </div>
        </div>
      </div>

      <!-- 数据格式说明 -->
      <div class="schema-card">
        <h3>🔌 与后端 MySQL 邻接表的对接约定</h3>
        <p>
          分类表使用 <code>id</code> + <code>parent_id</code> 组织层级：
          根分类 <code>parent_id = 0</code>，子分类的 <code>parent_id</code> 指向父分类的 <code>id</code>。
          后端接口 <code>GET /api/categories</code> 返回这张邻接表，前端用 <code>buildCategoryTree()</code> 转成树。
        </p>
        <pre>{{ JSON.stringify(flatTable.slice(0, 4), null, 2) }}</pre>
      </div>
    </main>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }

.cat-page {
  min-height: 100vh;
  background: #faf9f7;
  color: #333;
}

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
}

.logo-icon { font-size: 28px; }

.nav-menu { display: flex; gap: 26px; }

.nav-menu a {
  color: #666;
  text-decoration: none;
  font-size: 15px;
  padding: 8px 0;
}

.nav-menu a:hover,
.nav-menu a.active { color: #c2410c; }

.cat-container {
  width: 84%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 0 60px;
}

.cat-header h1 {
  margin: 0 0 8px;
  font-size: 30px;
}

.cat-header p {
  margin: 0 0 28px;
  color: #888;
}

.cat-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
}

.tree-card, .info-card, .schema-card {
  background: #fff;
  border-radius: 16px;
  padding: 26px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
}

.tree-card h2, .info-card h2 {
  margin: 0 0 6px;
  font-size: 19px;
}

.hint {
  margin: 0 0 16px;
  color: #999;
  font-size: 13px;
}

:deep(.el-tree) {
  background: transparent;
  font-size: 15px;
}

:deep(.el-tree-node__content) {
  height: 38px;
}

.info-body { margin-top: 16px; }

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
  font-size: 14px;
}

.info-row:last-child { border-bottom: none; }

.label {
  width: 100px;
  color: #888;
  flex-shrink: 0;
}

.value {
  color: #333;
  font-weight: 500;
}

.value.name {
  color: #c2410c;
  font-size: 17px;
}

.value.path { color: #555; }

.empty-tip {
  margin-top: 30px;
  text-align: center;
  color: #aaa;
  padding: 40px 0;
}

.schema-card {
  margin-top: 22px;
}

.schema-card h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.schema-card p {
  color: #666;
  font-size: 14px;
  line-height: 1.8;
}

.schema-card code {
  background: #f3f0ff;
  color: #c2410c;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

.schema-card pre {
  margin: 14px 0 0;
  background: #faf9f7;
  padding: 14px;
  border-radius: 8px;
  font-size: 13px;
  overflow-x: auto;
}

@media (max-width: 800px) {
  .cat-layout { grid-template-columns: 1fr; }
  .navbar { padding: 0 4%; }
  .nav-menu { gap: 12px; }
  .nav-menu a { font-size: 13px; }
}
</style>
