// ============================================================
// 字典码值映射（对齐数据库 dict_data 表）
// 后端接口就绪后，这里的值可以改成从 /api/v1/dict-data/{type_code} 动态加载
// ============================================================

// 商品成色 goods_quality
export const QUALITY_MAP = {
  NEW: '全新',
  '90': '九成新',
  '80': '八成新',
  '70': '七成新',
  USED: '有磨损'
}

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
