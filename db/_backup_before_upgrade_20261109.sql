
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `demo_file_blob`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo_file_blob` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件主键',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  `file_content` blob COMMENT '文件二进制BLOB字段，存放图片，本项目业务不使用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='【演示用】BLOB存储文件，仅用于方案对比';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `demo_file_blob` WRITE;
/*!40000 ALTER TABLE `demo_file_blob` DISABLE KEYS */;
INSERT INTO `demo_file_blob` VALUES (1,'demo.txt','text/plain',_binary 'Hello','2026-10-14 16:00:06');
/*!40000 ALTER TABLE `demo_file_blob` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `dict_data` WRITE;
/*!40000 ALTER TABLE `dict_data` DISABLE KEYS */;
INSERT INTO `dict_data` VALUES (1,'goods_status','上架','ON_SHELF',1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(2,'goods_status','下架','OFF_SHELF',2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(3,'goods_status','已售出','SOLD',3,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(4,'audit_status','待审核','PENDING',1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(5,'audit_status','审核通过','PASS',2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(6,'audit_status','审核驳回','REJECT',3,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(7,'goods_quality','全新','NEW',1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(8,'goods_quality','九成新','90',2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(9,'goods_quality','八成新','80',3,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(10,'goods_quality','七成新','70',4,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(11,'goods_quality','有磨损','USED',5,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(12,'order_status','待确认','WAIT_CONFIRM',1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(13,'order_status','待交易','WAIT_TRADE',2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(14,'order_status','交易完成','FINISH',3,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(15,'order_status','已取消','CANCEL',4,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0);
/*!40000 ALTER TABLE `dict_data` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_code` varchar(100) NOT NULL COMMENT '字典类型编码',
  `dict_name` varchar(100) NOT NULL COMMENT '字典类型名称',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注，预留扩展',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `dict_type` WRITE;
/*!40000 ALTER TABLE `dict_type` DISABLE KEYS */;
INSERT INTO `dict_type` VALUES (1,'goods_status','商品状态',1,'商品上下架、售出状态','2026-10-13 16:27:05','2026-10-13 16:27:05',0),(2,'audit_status','审核状态',2,'商品审核结果','2026-10-13 16:27:05','2026-10-13 16:27:05',0),(3,'goods_quality','商品成色',3,'新旧成色字典','2026-10-13 16:27:05','2026-10-13 16:27:05',0),(4,'order_status','订单意向状态',4,'线下意向订单状态','2026-10-13 16:27:05','2026-10-13 16:27:05',0);
/*!40000 ALTER TABLE `dict_type` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `evaluation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `evaluation` WRITE;
/*!40000 ALTER TABLE `evaluation` DISABLE KEYS */;
/*!40000 ALTER TABLE `evaluation` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods` WRITE;
/*!40000 ALTER TABLE `goods` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `goods_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类树形表(邻接表)';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_category` WRITE;
/*!40000 ALTER TABLE `goods_category` DISABLE KEYS */;
INSERT INTO `goods_category` VALUES (1,'教材书籍',0,1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(2,'电子产品',0,2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(3,'服饰鞋包',0,3,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(4,'生活用品',0,4,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(5,'体育器材',0,5,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(6,'计算机教材',1,1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(7,'考研资料',1,2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(8,'手机',2,1,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0),(9,'耳机',2,2,NULL,'2026-10-13 16:27:05','2026-10-13 16:27:05',0);
/*!40000 ALTER TABLE `goods_category` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `goods_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '收藏用户id',
  `goods_id` bigint NOT NULL COMMENT '商品id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_goods` (`user_id`,`goods_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_collect` WRITE;
/*!40000 ALTER TABLE `goods_collect` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods_collect` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `goods_stat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `goods_stat` WRITE;
/*!40000 ALTER TABLE `goods_stat` DISABLE KEYS */;
/*!40000 ALTER TABLE `goods_stat` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `rag_history_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `rag_history_record` WRITE;
/*!40000 ALTER TABLE `rag_history_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `rag_history_record` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `report_work_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `report_work_order` WRITE;
/*!40000 ALTER TABLE `report_work_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_work_order` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'permissionId权限主键',
  `perm_code` varchar(100) NOT NULL COMMENT '权限编码，唯一，如goods:publish/user:manage',
  `perm_name` varchar(50) NOT NULL COMMENT '权限名称',
  `perm_type` varchar(20) NOT NULL COMMENT '权限类型：MENU菜单/BUTTON按钮/API接口',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID，0代表根',
  `perm_path` varchar(200) DEFAULT NULL COMMENT '前端路由或后端接口路径',
  `sort` int DEFAULT '0' COMMENT '排序号',
  `ext_json` json DEFAULT NULL COMMENT '扩展字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `perm_code` (`perm_code`),
  KEY `idx_perm_parent` (`parent_id`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
INSERT INTO `sys_permission` VALUES (1,'goods:view','浏览商品','API',0,'GET /api/v1/goods',1,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(2,'goods:publish','发布商品','API',0,'POST /api/v1/goods',2,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(3,'goods:edit','编辑商品','API',0,'PUT /api/v1/goods/{id}',3,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(4,'goods:delete','删除商品','API',0,'DELETE /api/v1/goods/{id}',4,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(5,'goods:status','上下架商品','API',0,'PUT /api/v1/goods/{id}/status',5,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(6,'goods:audit','商品审核','API',0,'PUT /api/v1/admin/goods/{id}/audit',6,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(7,'favorites:manage','收藏管理','API',0,'POST /api/v1/favorites',7,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(8,'order:manage','订单管理','API',0,'POST /api/v1/orders',8,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(9,'evaluation:create','发布评价','API',0,'POST /api/v1/evaluations',9,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(10,'message:send','发送消息','API',0,'POST /api/v1/messages',10,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(11,'report:create','提交举报','API',0,'POST /api/v1/reports',11,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(12,'report:handle','举报处理','API',0,'PUT /api/v1/reports/{id}/status',12,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(13,'user:manage','用户管理','API',0,'GET /api/v1/admin/users',13,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(14,'user:status','用户封禁','API',0,'PUT /api/v1/admin/users/{id}/status',14,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(15,'category:manage','分类管理','API',0,'POST /api/v1/categories',15,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(16,'dict:manage','字典管理','API',0,'GET /api/v1/dicts',16,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0);
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  UNIQUE KEY `role_code` (`role_code`),
  KEY `idx_role_status` (`status`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'ADMIN','管理员','系统管理员，拥有全部权限',1,1,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0),(2,'STUDENT','普通学生','普通学生用户，基础买卖权限',2,1,NULL,'2026-10-27 16:33:48','2026-10-27 16:33:48',0);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint NOT NULL COMMENT '角色ID，关联sys_role.id',
  `permission_id` bigint NOT NULL COMMENT '权限ID，关联sys_permission.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`,`permission_id`,`is_deleted`),
  KEY `idx_perm_id` (`permission_id`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
INSERT INTO `sys_role_permission` VALUES (1,1,1,'2026-10-27 16:33:48',0),(2,1,2,'2026-10-27 16:33:48',0),(3,1,3,'2026-10-27 16:33:48',0),(4,1,4,'2026-10-27 16:33:48',0),(5,1,5,'2026-10-27 16:33:48',0),(6,1,6,'2026-10-27 16:33:48',0),(7,1,7,'2026-10-27 16:33:48',0),(8,1,8,'2026-10-27 16:33:48',0),(9,1,9,'2026-10-27 16:33:48',0),(10,1,10,'2026-10-27 16:33:48',0),(11,1,11,'2026-10-27 16:33:48',0),(12,1,12,'2026-10-27 16:33:48',0),(13,1,13,'2026-10-27 16:33:48',0),(14,1,14,'2026-10-27 16:33:48',0),(15,1,15,'2026-10-27 16:33:48',0),(16,1,16,'2026-10-27 16:33:48',0),(32,2,9,'2026-10-27 16:33:48',0),(33,2,7,'2026-10-27 16:33:48',0),(34,2,4,'2026-10-27 16:33:48',0),(35,2,3,'2026-10-27 16:33:48',0),(36,2,2,'2026-10-27 16:33:48',0),(37,2,5,'2026-10-27 16:33:48',0),(38,2,1,'2026-10-27 16:33:48',0),(39,2,10,'2026-10-27 16:33:48',0),(40,2,8,'2026-10-27 16:33:48',0),(41,2,11,'2026-10-27 16:33:48',0);
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键userId',
  `student_no` varchar(50) NOT NULL COMMENT '学号，唯一',
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
  UNIQUE KEY `student_no` (`student_no`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `nick_name` (`nick_name`),
  KEY `idx_user_student` (`student_no`,`is_deleted`),
  KEY `idx_user_nickname` (`nick_name`,`is_deleted`),
  KEY `idx_user_status` (`account_status`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'2024110640','zhangay@test.com','$2b$12$k4q.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq','张爱阳',NULL,NULL,NULL,100,NULL,9,'NORMAL',NULL,NULL,NULL,NULL,'2026-11-04 21:22:14','2026-11-05 11:02:16',0),(2,'2024777777','2024777777@qq.com','$2b$12$k4q.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq','测试学生',NULL,NULL,NULL,100,NULL,0,'NORMAL',NULL,NULL,NULL,NULL,'2026-11-05 10:58:00','2026-11-05 10:58:00',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联sys_user.id',
  `role_id` bigint NOT NULL COMMENT '角色ID，关联sys_role.id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`,`is_deleted`),
  KEY `idx_role_id` (`role_id`,`is_deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1,1,1,'2026-11-04 21:22:14',0),(2,2,2,'2026-11-05 10:58:09',0);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `trade_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `trade_order` WRITE;
/*!40000 ALTER TABLE `trade_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `trade_order` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

