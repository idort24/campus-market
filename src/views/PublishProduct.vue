<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createProduct } from '../api/product.js'
import { CATEGORIES, CONDITIONS } from '../data/products.js'

const router = useRouter()

// 表单数据
const form = reactive({
  name: '',
  price: '',
  category: '',
  condition: '',
  description: '',
  location: ''
})

// 校验错误信息（字段名 -> 提示文案）
const errors = reactive({})
const submitting = ref(false)
const submitError = ref('')

function validate() {
  Object.keys(errors).forEach((k) => delete errors[k])

  if (!form.name.trim()) {
    errors.name = '请填写商品名称'
  }

  if (form.price === '' || form.price === null) {
    errors.price = '请填写价格'
  } else if (isNaN(Number(form.price)) || Number(form.price) <= 0) {
    errors.price = '价格必须是大于 0 的数字'
  }

  if (!form.category) {
    errors.category = '请选择分类'
  }

  if (!form.condition) {
    errors.condition = '请选择成色'
  }

  if (!form.location.trim()) {
    errors.location = '请填写交易地点'
  }

  return Object.keys(errors).length === 0
}

async function onSubmit() {
  submitError.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    await createProduct({ ...form })
    // 发布成功，跳回商品市场
    router.push('/product')
  } catch (e) {
    submitError.value = e?.message || '发布失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

function onCancel() {
  router.push('/product')
}
</script>

<template>
  <div class="publish-page">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="logo">
        <span class="logo-icon">🛍️</span>
        <span>校园二手集市</span>
      </div>

      <nav class="nav-menu">
        <router-link to="/">首页</router-link>
        <router-link to="/product">商品市场</router-link>
        <router-link to="/publish" active-class="active">发布商品</router-link>
        <router-link to="/categories">分类管理</router-link>
        <router-link to="/ai">AI助手</router-link>
        <router-link to="/profile">个人中心</router-link>
      </nav>
    </header>

    <!-- 表单主体 -->
    <main class="publish-container">
      <section class="publish-header">
        <h1>发布闲置商品</h1>
        <p>填写下面的信息，让你的闲置物品找到新主人</p>
      </section>

      <form class="publish-card" @submit.prevent="onSubmit">
        <!-- 商品名称 -->
        <div class="form-item">
          <label>商品名称 <span class="required">*</span></label>
          <input
            v-model="form.name"
            type="text"
            placeholder="例如：高等数学教材（第七版）"
            :class="{ invalid: errors.name }"
          />
          <p v-if="errors.name" class="error-msg">{{ errors.name }}</p>
        </div>

        <!-- 价格 + 分类 -->
        <div class="form-row">
          <div class="form-item">
            <label>价格（元） <span class="required">*</span></label>
            <input
              v-model="form.price"
              type="number"
              min="0"
              step="0.01"
              placeholder="例如：20"
              :class="{ invalid: errors.price }"
            />
            <p v-if="errors.price" class="error-msg">{{ errors.price }}</p>
          </div>

          <div class="form-item">
            <label>商品分类 <span class="required">*</span></label>
            <select v-model="form.category" :class="{ invalid: errors.category }">
              <option value="" disabled>请选择分类</option>
              <option v-for="c in CATEGORIES" :key="c" :value="c">{{ c }}</option>
            </select>
            <p v-if="errors.category" class="error-msg">{{ errors.category }}</p>
          </div>
        </div>

        <!-- 成色 -->
        <div class="form-item">
          <label>商品成色 <span class="required">*</span></label>
          <select v-model="form.condition" :class="{ invalid: errors.condition }">
            <option value="" disabled>请选择成色</option>
            <option v-for="c in CONDITIONS" :key="c" :value="c">{{ c }}</option>
          </select>
          <p v-if="errors.condition" class="error-msg">{{ errors.condition }}</p>
        </div>

        <!-- 交易地点 -->
        <div class="form-item">
          <label>交易地点 <span class="required">*</span></label>
          <input
            v-model="form.location"
            type="text"
            placeholder="例如：学校图书馆附近 / 某宿舍楼下"
            :class="{ invalid: errors.location }"
          />
          <p v-if="errors.location" class="error-msg">{{ errors.location }}</p>
        </div>

        <!-- 商品描述 -->
        <div class="form-item">
          <label>商品描述</label>
          <textarea
            v-model="form.description"
            rows="4"
            placeholder="补充说明一下成色、入手时间、为什么出手等（选填）"
          ></textarea>
        </div>

        <!-- 整体错误 -->
        <p v-if="submitError" class="submit-error">{{ submitError }}</p>

        <!-- 按钮 -->
        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="onCancel">取消</button>
          <button type="submit" class="submit-btn" :disabled="submitting">
            {{ submitting ? '发布中...' : '立即发布' }}
          </button>
        </div>
      </form>

      <p class="tip">
        💡 当前为前端演示，商品会先存到本地；接入后端后将自动保存到服务器。
      </p>
    </main>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }

.publish-page {
  min-height: 100vh;
  background: #faf9f7;
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
}

.logo-icon { font-size: 28px; }

.nav-menu { display: flex; gap: 30px; }

.nav-menu a {
  color: #666;
  text-decoration: none;
  font-size: 15px;
  padding: 8px 0;
}

.nav-menu a:hover,
.nav-menu a.active { color: #c2410c; }

/* 主体 */
.publish-container {
  width: 84%;
  max-width: 720px;
  margin: 0 auto;
  padding: 45px 0 60px;
}

.publish-header h1 {
  margin: 0 0 8px;
  font-size: 32px;
}

.publish-header p {
  margin: 0 0 30px;
  color: #888;
}

.publish-card {
  background: #fff;
  border-radius: 16px;
  padding: 35px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05);
}

.form-item { margin-bottom: 22px; }

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #444;
  font-weight: 600;
}

.required { color: #ff6b6b; }

input, select, textarea {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #e3e3ec;
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  background: #fafafc;
  outline: none;
  transition: border-color 0.2s;
}

input:focus, select:focus, textarea:focus {
  border-color: #c2410c;
  background: #fff;
}

input.invalid, select.invalid { border-color: #ff6b6b; }

textarea { resize: vertical; }

.error-msg {
  margin: 6px 0 0;
  color: #ff6b6b;
  font-size: 12px;
}

.submit-error {
  margin: 0 0 15px;
  padding: 10px 14px;
  background: #fff0f0;
  color: #d64545;
  border-radius: 8px;
  font-size: 13px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.cancel-btn, .submit-btn {
  padding: 11px 26px;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: 0.2s;
}

.cancel-btn {
  background: #f1f1f6;
  color: #666;
}

.cancel-btn:hover { background: #e6e6ee; }

.submit-btn {
  background: #c2410c;
  color: #fff;
}

.submit-btn:hover:not(:disabled) { background: #9a3412; }

.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.tip {
  margin-top: 20px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

@media (max-width: 600px) {
  .navbar { padding: 0 4%; }
  .nav-menu { gap: 12px; }
  .nav-menu a { font-size: 13px; }
  .publish-container { width: 92%; }
  .publish-card { padding: 22px; }
  .form-row { grid-template-columns: 1fr; gap: 0; }
}
</style>
