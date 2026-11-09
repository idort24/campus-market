-- ============================================================
-- 增量升级 06：sys_user 补 phone 列
-- 文件：06_upgrade_phone_20261109.sql
-- 日期：2026-11-09
-- 执行：mysql --default-character-set=utf8mb4 < 06_upgrade_phone_20261109.sql
-- 注意：ALTER 语句不可重复执行，本脚本只跑一次。
-- ============================================================
-- 【背景】
-- 前端切真后端联调实测，POST /api/v1/user/login 返回 500，
-- 后端日志堆栈根因：
--     java.sql.SQLSyntaxErrorException: Unknown column 'phone' in 'field list'
-- 后端实体 com.campusmarket.entity.SysUser 声明了 phone 字段
-- （源码注释：private String phone; // 手机号 phone（注册必填，唯一索引）），
-- 而 01_schema.sql 建表时漏了这一列，导致：
--   - 密码登录：查 sys_user 时 SELECT 含 phone 列 -> SQL 异常 -> 500
--   - 短信登录：按 phone 反查账号 -> 同样 500
--   - 注册：写入 phone -> 同样 500
-- 影响面：整个登录/注册链路不可用。
-- ============================================================
-- 【为什么允许 NULL】
-- MySQL 唯一索引允许多个 NULL，存量用户没有手机号时不会互相冲突；
-- 新注册用户由后端校验"注册必填"，不必在建表层面强制 NOT NULL。
-- ============================================================

USE `campus_market`;

SET NAMES utf8mb4;

ALTER TABLE `sys_user`
  ADD COLUMN `phone` varchar(20) DEFAULT NULL
    COMMENT '手机号，唯一，短信登录账号；注册必填，允许为空以兼容存量数据'
    AFTER `student_no`,
  ADD UNIQUE KEY `uk_phone` (`phone`);

-- 给两个测试账号补手机号（短信登录时账号就是手机号）
UPDATE `sys_user` SET `phone` = '13800000001'
 WHERE `student_no` = '2024110640' AND (`phone` IS NULL OR `phone` = '');

UPDATE `sys_user` SET `phone` = '13800000002'
 WHERE `student_no` = '2024777777' AND (`phone` IS NULL OR `phone` = '');

-- 自检输出
SELECT id, student_no, phone, nick_name, user_type FROM `sys_user`;
