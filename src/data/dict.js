// ============================================================
// 字典码值映射（对齐数据库 dict_data 表）
// 后端接口就绪后，这里的值可以改成从 /api/v1/dict-data/{type_code} 动态加载
// ============================================================

// 商品成色 goods_quality
// 键是库里存的值（NEW/90/80/70/USED），值是页面显示的中文
export const QUALITY_MAP = {
  NEW: '全新',
  '90': '九成新',
  '80': '八成新',
  '70': '七成新',
  USED: '有磨损'
}

// 成色从新到旧的展示顺序。
// 注意：不能直接 Object.values(QUALITY_MAP)——'90'/'80'/'70' 这类整数形态的键
// 会被 JS 对象的枚举规则移到最前面，下拉框顺序就会变成"七成新→全新"。
const QUALITY_ORDER = ['NEW', '90', '80', '70', 'USED']

// 发布商品时表单选的是中文，提交给后端必须换成字典码值
export const QUALITY_CODE_BY_TEXT = Object.fromEntries(
  QUALITY_ORDER.map((code) => [QUALITY_MAP[code], code])
)

// 发布页成色下拉的可选项：直接用字典里的 5 项，避免和库口径不一致
export const QUALITY_OPTIONS = QUALITY_ORDER.map((code) => QUALITY_MAP[code])

// 商品状态 goods_status
export const GOODS_STATUS_MAP = {
  ON_SHELF: '上架',
  OFF_SHELF: '下架',
  SOLD: '已售出'
}

// 审核状态 audit_status
export const AUDIT_STATUS_MAP = {
  PENDING: '待审核',
  PASS: '审核通过',
  REJECT: '审核驳回'
}

// 订单状态 order_status
export const ORDER_STATUS_MAP = {
  WAIT_CONFIRM: '待确认',
  WAIT_TRADE: '待交易',
  FINISH: '交易完成',
  CANCEL: '已取消'
}

// 状态对应的 el-tag 类型
export const AUDIT_STATUS_TAG = {
  PENDING: 'warning',
  PASS: 'success',
  REJECT: 'danger'
}

export const ORDER_STATUS_TAG = {
  WAIT_CONFIRM: 'warning',
  WAIT_TRADE: 'primary',
  FINISH: 'success',
  CANCEL: 'info'
}
