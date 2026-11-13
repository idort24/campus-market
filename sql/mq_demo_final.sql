-- mq_demo_final.sql
-- 二手市场消息队列 + 统计大屏：数据库最终版
-- 对应课堂作业要求：消息异步落库、幂等、六项大屏指标、Redis 会话与热点预热
-- 执行：mysql -u root -p --default-character-set=utf8mb4 < mq_demo_final.sql

CREATE DATABASE IF NOT EXISTS mq_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE mq_demo;

-- ============================================================
-- 1. 事件明细表：消费者落库的目标表
-- ============================================================
DROP TABLE IF EXISTS event_log;
CREATE TABLE event_log (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增，同时充当快照版本号来源',
  event_id    VARCHAR(64)     NOT NULL                COMMENT '事件唯一编号，重投时不变，幂等依据',
  session_id  VARCHAR(64)     NOT NULL                COMMENT '会话编号，一人在多设备算多个会话',
  event_type  VARCHAR(32)     NOT NULL                COMMENT '浏览view/收藏favorite/下单order',
  item_id     BIGINT          NOT NULL                COMMENT '商品ID',
  user_id     BIGINT          NOT NULL                COMMENT '用户ID',
  node_name   VARCHAR(32)     DEFAULT NULL            COMMENT '产生事件的Tomcat节点，证明负载均衡生效',
  event_time  DATETIME        NOT NULL                COMMENT '事件发生时间，分钟趋势按它聚合',
  payload     JSON            DEFAULT NULL            COMMENT '扩展字段',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '落库时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_event_id (event_id),
  KEY idx_type_time (event_type, event_time),
  KEY idx_session (session_id),
  KEY idx_item (item_id),
  KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '事件明细表';

-- ============================================================
-- 2. 统计快照表：大屏只读这张表，带版本号防旧数据覆盖
-- ============================================================
DROP TABLE IF EXISTS stat_snapshot;
CREATE TABLE stat_snapshot (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  stat_key      VARCHAR(64)     NOT NULL COMMENT 'total / session / type:view / trend:2026-11-13 17:30 / version',
  stat_value    BIGINT          NOT NULL DEFAULT 0 COMMENT '指标值',
  version       BIGINT          NOT NULL DEFAULT 0 COMMENT '快照版本号 = 计算时的 max(event_log.id)',
  snapshot_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_stat_key (stat_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '统计快照表';

-- ============================================================
-- 3. 生产计数器：统计积压用，积压 = 生产总数 - 已消费数
-- ============================================================
DROP TABLE IF EXISTS mq_counter;
CREATE TABLE mq_counter (
  counter_key   VARCHAR(32) NOT NULL COMMENT '固定为 produced',
  counter_value BIGINT      NOT NULL DEFAULT 0 COMMENT '生产者累计发送条数',
  updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (counter_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '生产计数表';

-- ============================================================
-- 4. 造演示数据：120 条已消费事件 + 130 条生产计数（差 10 条即演示积压）
-- ============================================================
INSERT INTO event_log (event_id, session_id, event_type, item_id, user_id, node_name, event_time, payload)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 120
)
SELECT
  UUID(),
  CONCAT('sess-', 2001 + FLOOR(RAND() * 20)),
  ELT(1 + FLOOR(RAND() * 3), 'view', 'favorite', 'order'),
  1001 + FLOOR(RAND() * 10),
  2001 + FLOOR(RAND() * 20),
  IF(n % 2 = 0, 'tomcat-1', 'tomcat-2'),
  NOW() - INTERVAL FLOOR(RAND() * 1800) SECOND,
  JSON_OBJECT('src', 'seed')
FROM seq;

REPLACE INTO mq_counter (counter_key, counter_value) VALUES ('produced', 130);

-- ============================================================
-- 5. 生成统计快照（消费者算完写这张表，版本号取 max(id)）
-- ============================================================
SET @ver = (SELECT COALESCE(MAX(id), 0) FROM event_log);

INSERT INTO stat_snapshot (stat_key, stat_value, version, snapshot_time)
SELECT 'version', @ver, @ver, NOW()
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), version = VALUES(version), snapshot_time = VALUES(snapshot_time);

INSERT INTO stat_snapshot (stat_key, stat_value, version, snapshot_time)
SELECT 'total', COUNT(*), @ver, NOW() FROM event_log
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), version = VALUES(version), snapshot_time = VALUES(snapshot_time);

INSERT INTO stat_snapshot (stat_key, stat_value, version, snapshot_time)
SELECT 'session', COUNT(DISTINCT session_id), @ver, NOW() FROM event_log
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), version = VALUES(version), snapshot_time = VALUES(snapshot_time);

INSERT INTO stat_snapshot (stat_key, stat_value, version, snapshot_time)
SELECT CONCAT('type:', event_type), COUNT(*), @ver, NOW() FROM event_log GROUP BY event_type
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), version = VALUES(version), snapshot_time = VALUES(snapshot_time);

INSERT INTO stat_snapshot (stat_key, stat_value, version, snapshot_time)
SELECT CONCAT('trend:', t.minute_key), COUNT(*), @ver, NOW()
FROM (SELECT DATE_FORMAT(event_time, '%Y-%m-%d %H:%i') AS minute_key FROM event_log) t
GROUP BY t.minute_key
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), version = VALUES(version), snapshot_time = VALUES(snapshot_time);

INSERT INTO stat_snapshot (stat_key, stat_value, version, snapshot_time)
SELECT 'backlog',
       (SELECT counter_value FROM mq_counter WHERE counter_key = 'produced') - (SELECT COUNT(*) FROM event_log),
       @ver, NOW()
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value), version = VALUES(version), snapshot_time = VALUES(snapshot_time);

-- ============================================================
-- 6. 六项指标自检：这六段就是大屏六个数字的口径
-- ============================================================
SELECT '1_已消费事件数' AS 指标, COUNT(*) AS 值 FROM event_log;

SELECT '2_会话去重' AS 指标, COUNT(DISTINCT session_id) AS 值 FROM event_log;

SELECT '3_类型分布' AS 指标, event_type AS 维度, COUNT(*) AS 值 FROM event_log GROUP BY event_type;

SELECT '4_分钟趋势' AS 指标, t.minute_key AS 分钟, COUNT(*) AS 值
FROM (SELECT DATE_FORMAT(event_time, '%Y-%m-%d %H:%i') AS minute_key FROM event_log) t
GROUP BY t.minute_key ORDER BY t.minute_key DESC LIMIT 5;

SELECT '5_队列积压' AS 指标,
       (SELECT counter_value FROM mq_counter WHERE counter_key = 'produced') - COUNT(*) AS 值
FROM event_log;

SELECT '6_快照版本号' AS 指标, MAX(id) AS 值 FROM event_log;

-- 前面几条是明细算法，下面这条是快照读数（大屏实际读的）
SELECT stat_key, stat_value, version FROM stat_snapshot ORDER BY stat_key;
