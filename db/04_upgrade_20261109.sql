-- ============================================================
-- campus_market 增量修复脚本
-- 文件：04_upgrade_20261109.sql
-- 用途：把 2026-11-09 之前建好的旧库，补齐到 01/02 脚本定义的标准
-- 适用：库里已有 17 张表、16 条 API 权限、2 个测试账号的情况
-- 执行：mysql -u root -p campus_market < 04_upgrade_20261109.sql
--
-- 本脚本只做三件事，全部是 UPDATE / INSERT IGNORE，不改任何表结构、不动业务数据：
--   1. 修正学生账号的 user_type（0 → 1）
--   2. 补 account_status 字典（旧库只有 4 类字典）
--   3. 补 7 条 MENU 权限并绑定角色（旧库权限全是 API，一条菜单都没有）
--
-- 【刻意不做的事】不重建 sys_user_role / sys_role_permission / goods_collect
-- 的唯一键。原因见 README 第 3 条：这三个坑的根因在后端的逻辑删除写法，
-- 光改索引只会把「第三次报错」提前成「第二次报错」，反而更糟。
-- ============================================================

USE `campus_market`;
SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 修正 user_type
-- 旧脚本把学生账号的 user_type 写成了 0，不在合法取值范围内（1学生/9管理员）
-- ------------------------------------------------------------
UPDATE `sys_user`
SET `user_type` = 1
WHERE `user_type` NOT IN (1, 9) AND `is_deleted` = 0;

-- ------------------------------------------------------------
-- 2. 补充字典：账号状态
-- sys_user.account_status 的注释写了"关联字典"，但旧库没有这一类和它的取值
-- ------------------------------------------------------------
INSERT IGNORE INTO `dict_type` (`dict_code`, `dict_name`, `sort`, `remark`) VALUES
('account_status', '账号状态', 5, '用户账号状态，sys_user.account_status 关联此字典');

INSERT IGNORE INTO `dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('account_status', '正常',     'NORMAL', 1),
('account_status', '警告',     'WARN',   2),
('account_status', '限制发布', 'LIMIT',  3),
('account_status', '封禁',     'BAN',    4);

-- ------------------------------------------------------------
-- 3. 补充 7 条 MENU 权限
-- 旧库 16 条权限全是 API 类型，一条菜单权限都没有。
-- 前端目前用静态路由 + roleCode 判断，还没消费菜单数据，
-- 但答辩要讲"菜单由权限表驱动"，后续 /user/menus 接口也要用，先补齐。
-- ------------------------------------------------------------
INSERT IGNORE INTO `sys_permission` (`perm_code`, `perm_name`, `perm_type`, `parent_id`, `perm_path`, `sort`) VALUES
('menu:user',     '用户管理', 'MENU', 0, '/admin/users',      21),
('menu:goods',    '商品审核', 'MENU', 0, '/admin/goods',      22),
('menu:report',   '举报处理', 'MENU', 0, '/admin/reports',    23),
('menu:category', '分类管理', 'MENU', 0, '/admin/categories', 24),
('menu:role',     '角色权限', 'MENU', 0, '/admin/roles',      25),
('menu:my-goods', '我的发布', 'MENU', 0, '/my-publishes',     26),
('menu:my-order', '我的订单', 'MENU', 0, '/my-orders',        27);

-- ADMIN 绑定全部 7 个菜单
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `sys_role` r
JOIN `sys_permission` p ON p.perm_type = 'MENU' AND p.is_deleted = 0
WHERE r.role_code = 'ADMIN' AND r.is_deleted = 0;

-- STUDENT 只绑定用户端两个菜单
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `sys_role` r
JOIN `sys_permission` p ON p.is_deleted = 0
WHERE r.role_code = 'STUDENT' AND r.is_deleted = 0
  AND p.perm_code IN ('menu:my-goods', 'menu:my-order');

-- ============================================================
-- 升级完成，执行 03_check.sql 验一遍
-- ============================================================
