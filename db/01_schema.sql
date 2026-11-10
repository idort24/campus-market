-- ============================================================
-- 校园轻集市 campus_market 建表脚本（全量）
-- 文件：01_schema.sql
-- 说明：17 张表，与后端 com.campusmarket.entity 下的实体一一对应
-- 字符集 utf8mb4 / 排序 utf8mb4_0900_ai_ci，与现有库保持一致
-- 执行方式：mysql -u root -p < 01_schema.sql
-- 幂等：每张表执行前 DROP，重复执行不会残留旧结构
-- ============================================================

CREATE DATABASE IF NOT EXISTS `campus_market`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `campus_market`;

SET NAMES utf8mb4;

-- ============================================================
-- 一、RBAC 权限体系（5 张表）
-- ============================================================

-- 1. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键userId',
  `student_no` varchar(50) NOT NULL COMMENT '学号，唯一，密码登录账号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号，唯一，短信登录账号；注册必填，允许为空以兼容存量数据',
  `email` varchar(100) NOT NULL COMMENT '校园邮箱，唯一',
  `password` varchar(128) NOT NULL COMMENT 'bcrypt加密密码，不存明文',
  `nick_name` varchar(30) NOT NULL COMMENT '昵称全局唯一',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像OSS地址',
  `department` varchar(100) DEFAULT NULL COMMENT '院系',
  `grade` varchar(50) DEFAULT NULL COMMENT '年级',
  `credit_score` int DEFAULT '100' COMMENT '信誉分',
  `credit_level` varchar(20) DEFAULT NULL COMMENT '信誉等级，对接字典',
  `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '1普通学生 9管理员',
  `account_status` varchar(30) NOT NULL DEFAULT 'NORMAL' COMMENT '账号状态：NORMAL正常/WARN警告/LIMIT限制发布/BAN封禁，关联字典',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `logout_time` datetime DEFAULT NULL COMMENT '账号注销时间，不为空代表已注销',
  `logout_mask_info` json DEFAULT NULL COMMENT '注销后脱敏保留信息',
  `ext_json` json DEFAULT NULL COMMENT '用户扩展字段，后续新增用户属性直接放json，不用alter table',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_nick_name` (`nick_name`),
  KEY `idx_user_student` (`student_no`,`is_deleted`),
  KEY `idx_user_nickname` (`nick_name`,`is_deleted`),
  KEY `idx_user_status` (`account_status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- 2. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'roleId角色主键',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码，唯一，如ADMIN/STUDENT',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称，如管理员/普通学生',
  `role_desc` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '1启用 0停用',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_role_status` (`status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';

-- 3. 权限表
-- perm_type 三种取值，含义必须分清：
--   MENU   = 前端左侧菜单项，perm_path 存路由地址（如 /admin/users）
--   API    = 后端接口权限，同时被前端 v-permission 指令当作按钮权限码使用
--   BUTTON = 纯按钮权限（当前未使用，保留扩展）
-- perm_code 是鉴权唯一依据，AuthInterceptor 拿它和权限服务返回的集合做精确匹配
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'permissionId权限主键',
  `perm_code` varchar(100) NOT NULL COMMENT '权限编码，唯一，如goods:audit/menu:user',
  `perm_name` varchar(50) NOT NULL COMMENT '权限名称',
  `perm_type` varchar(20) NOT NULL COMMENT '权限类型：MENU菜单/BUTTON按钮/API接口',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID，0代表根',
  `perm_path` varchar(200) DEFAULT NULL COMMENT '前端路由(MENU)或后端接口路径(API)',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_perm_parent` (`parent_id`,`is_deleted`),
  KEY `idx_perm_type` (`perm_type`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限表';

-- 4. 用户-角色关联表
-- 唯一键沿用 (user_id, role_id, is_deleted) 的原设计。
-- 【已知缺陷，见 README 第 3 条】后端用逻辑删除 + 全量覆盖重分配，
-- 同一对用户角色被分配/回收第三次时会撞唯一键（实测 1062），
-- 根治办法在后端改成物理删除，不是在本表改索引。
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联sys_user.id',
  `role_id` bigint NOT NULL COMMENT '角色ID，关联sys_role.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`,`is_deleted`),
  KEY `idx_role_id` (`role_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

-- 5. 角色-权限关联表
-- 唯一键同上，缺陷类型与修法同 sys_user_role。
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID，关联sys_role.id',
  `permission_id` bigint NOT NULL COMMENT '权限ID，关联sys_permission.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`,`permission_id`,`is_deleted`),
  KEY `idx_perm_id` (`permission_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

-- ============================================================
-- 二、业务表（12 张）
-- ============================================================

-- 6. 商品分类（邻接表树形）
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID，0代表根节点',
  `sort` int DEFAULT '0' COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段：分类图标、描述等，预留',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_cat_parent` (`parent_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类树形表(邻接表)';

-- 7. 商品表
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'goodsId商品主键',
  `seller_id` bigint NOT NULL COMMENT '卖家用户ID，关联sys_user.id',
  `category_id` bigint NOT NULL COMMENT '商品分类ID，关联goods_category.id',
  `title` varchar(100) NOT NULL COMMENT '商品标题',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价，≥0.01',
  `quality` varchar(30) NOT NULL COMMENT '成色，字典取值',
  `images` json DEFAULT NULL COMMENT '图片OSS地址数组，["url1","url2"]',
  `trade_place` varchar(100) DEFAULT NULL COMMENT '线下交易地点',
  `can_bargain` tinyint(1) DEFAULT '0' COMMENT '0不可议价，1允许议价',
  `audit_status` varchar(30) NOT NULL COMMENT '待审核/审核通过/审核驳回，字典',
  `goods_status` varchar(30) NOT NULL COMMENT '上架/下架/已售出，字典',
  `ai_generate_flag` tinyint(1) DEFAULT '0' COMMENT '0手动填写，1AI生成文案',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核驳回理由',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `sell_time` datetime DEFAULT NULL COMMENT '售出时间',
  `ext_json` json DEFAULT NULL COMMENT '商品扩展字段，未来加属性直接写JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_goods_seller` (`seller_id`,`is_deleted`),
  KEY `idx_goods_category` (`category_id`,`audit_status`,`goods_status`,`is_deleted`),
  KEY `idx_goods_audit` (`audit_status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='二手商品表';

-- 8. 商品收藏表
-- 唯一键沿用原设计（含 is_deleted）。取消收藏后再次收藏、反复第三次时会撞键，
-- 与 sys_user_role 同类缺陷，修法见 README 第 3 条。
DROP TABLE IF EXISTS `goods_collect`;
CREATE TABLE `goods_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '收藏用户id',
  `goods_id` bigint NOT NULL COMMENT '商品id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏表';

-- 9. 商品预聚合统计表
DROP TABLE IF EXISTS `goods_stat`;
CREATE TABLE `goods_stat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `total_count` bigint DEFAULT '0' COMMENT '商品总数',
  `audit_pass_count` bigint DEFAULT '0' COMMENT '审核通过数量',
  `up_count` bigint DEFAULT '0' COMMENT '上架商品数',
  `sold_count` bigint DEFAULT '0' COMMENT '已售出数量',
  `stat_time` datetime NOT NULL COMMENT '统计快照时间',
  `ext_json` json DEFAULT NULL COMMENT '预留统计扩展指标',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_stat_time` (`stat_time`,`category_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品预聚合统计表，大数据查询优化';

-- 10. 意向交易订单表
DROP TABLE IF EXISTS `trade_order`;
CREATE TABLE `trade_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'orderId订单主键',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `goods_id` bigint NOT NULL COMMENT '关联商品ID',
  `trade_place` varchar(100) DEFAULT NULL COMMENT '约定线下交易地点',
  `order_status` varchar(30) NOT NULL COMMENT '待确认/待交易/已完成/已取消，字典',
  `order_note` varchar(200) DEFAULT NULL COMMENT '订单备注',
  `confirm_time` datetime DEFAULT NULL COMMENT '卖家确认时间',
  `finish_time` datetime DEFAULT NULL COMMENT '交易完成时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `ext_json` json DEFAULT NULL COMMENT '订单扩展属性',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_buyer` (`buyer_id`,`is_deleted`),
  KEY `idx_order_seller` (`seller_id`,`is_deleted`),
  KEY `idx_order_goods` (`goods_id`,`order_status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='意向交易订单表（仅记录线下意向，无线上支付）';

-- 11. 交易双向评价表
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'evalId评价主键',
  `order_id` bigint NOT NULL COMMENT '关联订单id',
  `from_user_id` bigint NOT NULL COMMENT '评价人ID',
  `to_user_id` bigint NOT NULL COMMENT '被评价人ID',
  `score` tinyint NOT NULL COMMENT '星级1~5',
  `eval_content` varchar(300) DEFAULT NULL COMMENT '评价文字',
  `tag` varchar(200) DEFAULT NULL COMMENT '交易标签，逗号分隔',
  `is_valid` tinyint(1) DEFAULT '1' COMMENT '1有效，0管理员标记恶意评价无效，逻辑隐藏',
  `ext_json` json DEFAULT NULL COMMENT '扩展',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_eval_order` (`order_id`,`is_deleted`),
  KEY `idx_eval_to_user` (`to_user_id`,`is_valid`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='交易双向评价表';

-- 12. 聊天消息表（含 Agent 自动回复、风险词标记）
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'msgId消息主键',
  `sender_id` bigint NOT NULL COMMENT '发送方userId',
  `receiver_id` bigint NOT NULL COMMENT '接收方userId',
  `msg_content` varchar(800) DEFAULT NULL COMMENT '文本消息',
  `msg_image` varchar(255) DEFAULT NULL COMMENT '图片OSS地址',
  `is_ai_message` tinyint(1) DEFAULT '0' COMMENT '0用户消息，1Agent智能助手自动回复',
  `read_flag` tinyint(1) DEFAULT '0' COMMENT '0未读，1已读',
  `risk_hint` tinyint(1) DEFAULT '0' COMMENT '0无风险标记，1命中诈骗风险词',
  `ext_json` json DEFAULT NULL COMMENT '扩展，存放AI溯源日志片段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_msg_sender_receiver` (`sender_id`,`receiver_id`,`read_flag`,`is_deleted`),
  KEY `idx_msg_receiver` (`receiver_id`,`read_flag`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表，含Agent自动回复';

-- 13. 举报工单表
DROP TABLE IF EXISTS `report_work_order`;
CREATE TABLE `report_work_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'reportId举报工单主键',
  `reporter_user_id` bigint NOT NULL COMMENT '举报人ID',
  `target_type` varchar(30) NOT NULL COMMENT '举报对象类型 USER用户 / GOODS商品',
  `target_id` bigint NOT NULL COMMENT '被举报对象ID',
  `report_reason` varchar(500) NOT NULL COMMENT '举报理由',
  `audit_result` varchar(30) DEFAULT NULL COMMENT '处理结果',
  `admin_remark` varchar(500) DEFAULT NULL COMMENT '管理员处理备注',
  `status` varchar(30) NOT NULL COMMENT '待处理 / 已处置 / 驳回，字典',
  `deal_time` datetime DEFAULT NULL COMMENT '处理时间',
  `ext_json` json DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_report_target` (`target_type`,`target_id`,`status`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户举报工单表';

-- 14. 字典类型表
DROP TABLE IF EXISTS `dict_type`;
CREATE TABLE `dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_code` varchar(100) NOT NULL COMMENT '字典类型编码',
  `dict_name` varchar(100) NOT NULL COMMENT '字典类型名称',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注，预留扩展',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- 15. 字典数据表
DROP TABLE IF EXISTS `dict_data`;
CREATE TABLE `dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type` varchar(100) NOT NULL COMMENT '关联字典类型编码',
  `dict_label` varchar(100) NOT NULL COMMENT '显示文本',
  `dict_value` varchar(100) NOT NULL COMMENT '存储数值',
  `sort` int DEFAULT '0' COMMENT '排序',
  `ext_json` json DEFAULT NULL COMMENT '扩展JSON字段，预留额外属性，可修改不用改表',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- 16. RAG 历史成交知识库元数据表（向量存外部 ChromaDB，本表只存元数据）
DROP TABLE IF EXISTS `rag_history_record`;
CREATE TABLE `rag_history_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'recordId知识库主键',
  `goods_category_id` bigint DEFAULT NULL COMMENT '关联商品分类ID',
  `goods_desc` varchar(500) DEFAULT NULL COMMENT '物品简短描述',
  `quality` varchar(30) DEFAULT NULL COMMENT '成色',
  `deal_price` decimal(10,2) NOT NULL COMMENT '历史成交价格',
  `deal_time` datetime NOT NULL COMMENT '成交时间',
  `sample_source` varchar(100) DEFAULT NULL COMMENT '数据来源：IMPORT导入/SYSTEM系统',
  `is_valid` tinyint(1) DEFAULT '1' COMMENT '1参与检索，0无效样本过滤',
  `ext_json` json DEFAULT NULL COMMENT '附加元数据，用于向量检索过滤条件',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_rag_cat` (`goods_category_id`,`is_valid`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RAG历史成交知识库元数据表，向量存ChromaDB';

-- 17. 演示用 BLOB 文件表（仅用于答辩对比"存库 vs 存磁盘"，业务不使用）
DROP TABLE IF EXISTS `demo_file_blob`;
CREATE TABLE `demo_file_blob` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件主键',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_content` blob COMMENT '文件二进制BLOB字段，存放图片，本项目业务不使用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='【演示用】BLOB存储文件，仅用于方案对比';

-- ============================================================
-- 建表完成：共 17 张
-- ============================================================
