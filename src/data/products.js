// 统一的商品模拟数据（响应式）
// 后续接入后端时，这个文件里的数据会被 src/api/product.js 里的真实接口替换。
import { ref } from 'vue'

// 全部分类（发布表单和筛选按钮共用）
export const CATEGORIES = ['数码产品', '教材书籍', '生活用品', '运动用品']

// 商品成色可选项
export const CONDITIONS = ['全新', '几乎全新', '九成新', '八成新', '七成及以下']

// 商品列表（响应式）：发布新商品后，所有用到它的页面会自动更新
export const productList = ref([
  {
    id: 1,
    name: '高等数学教材',
    price: 20,
    category: '教材书籍',
    icon: '📚',
    description: '九成新，适合大一数学学习',
    condition: '九成新',
    seller: '校园用户001',
    location: '学校图书馆附近'
  },
  {
    id: 2,
    name: '蓝牙耳机',
    price: 50,
    category: '数码产品',
    icon: '🎧',
    description: '音质清晰，使用状态良好',
    condition: '九成新',
    seller: '校园用户002',
    location: '学校宿舍区'
  },
  {
    id: 3,
    name: '宿舍椅子',
    price: 30,
    category: '生活用品',
    icon: '🪑',
    description: '宿舍闲置椅子，结实耐用',
    condition: '八成新',
    seller: '校园用户003',
    location: '学校生活区'
  },
  {
    id: 4,
    name: '机械键盘',
    price: 80,
    category: '数码产品',
    icon: '⌨️',
    description: '手感舒适，适合学习和办公',
    condition: '九成新',
    seller: '校园用户004',
    location: '学校教学楼附近'
  },
  {
    id: 5,
    name: '英语四级词汇书',
    price: 15,
    category: '教材书籍',
    icon: '📖',
    description: '备考四级必备，几乎全新',
    condition: '几乎全新',
    seller: '校园用户005',
    location: '学校图书馆附近'
  },
  {
    id: 6,
    name: '宿舍台灯',
    price: 25,
    category: '生活用品',
    icon: '💡',
    description: '三档亮度，晚上学习很方便',
    condition: '八成新',
    seller: '校园用户006',
    location: '学校宿舍区'
  },
  {
    id: 7,
    name: '篮球',
    price: 45,
    category: '运动用品',
    icon: '🏀',
    description: '使用次数较少，适合校园运动',
    condition: '九成新',
    seller: '校园用户007',
    location: '学校操场附近'
  },
  {
    id: 8,
    name: '运动水杯',
    price: 18,
    category: '运动用品',
    icon: '🥤',
    description: '大容量水杯，方便携带',
    condition: '几乎全新',
    seller: '校园用户008',
    location: '学校宿舍区'
  }
])

// 下一个可用 id（本地自增；接入后端后由后端生成）
let nextId = 9

// 根据分类选一个默认图标
const CATEGORY_ICONS = {
  '数码产品': '📱',
  '教材书籍': '📚',
  '生活用品': '🛋️',
  '运动用品': '⚽'
}

// 新增商品（本地 mock 实现；接入后端后由 api/product.js 改成 POST）
export function addProductLocally(form) {
  const product = {
    id: nextId++,
    name: form.name.trim(),
    price: Number(form.price),
    category: form.category,
    icon: form.icon || CATEGORY_ICONS[form.category] || '📦',
    description: form.description.trim(),
    condition: form.condition,
    seller: form.seller?.trim() || '我（本校学生）',
    location: form.location.trim()
  }
  // 新商品插到最前面，发布后立刻在列表里看到
  productList.value.unshift(product)
  return product
}
