-- ============================================================
-- 增量升级 07：补齐菜单权限到 11 条（对齐 docs/api-auth.md 第四节）
-- 文件：07_menu_permissions_20261109.sql
-- 日期：2026-11-09
-- 执行：mysql --default-character-set=utf8mb4 < 07_menu_permissions_20261109.sql
-- 可重复执行：perm_code 有唯一键，关联用 INSERT IGNORE
-- ============================================================
-- 【背景】
-- 库里原有 7 条 MENU 权限，覆盖了 5 条后台菜单 + 2 条学生菜单；
-- docs/api-auth.md 第四节要求 11 条（学生 6 条 + 管理员 5 条）。
-- 缺的 4 条全是学生端菜单，导致 GET /api/v1/user/menus 返回的菜单
-- 不完整，前端动态菜单建不出「商品集市 / 发布闲置 / 我的收藏 / 智能助手」。
--
-- 这 4 条的 perm_path 全部对应前端 router/index.js 里真实存在的路由：
--   /product      -> ProductMarket.vue
--   /publish      -> PublishProduct.vue
--   /my-favorites -> MyFavorites.vue
--   /ai           -> AiAssistant.vue
-- ============================================================

USE `campus_market`;

SET NAMES utf8mb4;

-- 一、补 4 条学生端菜单权限
INSERT IGNORE INTO `sys_permission`
  (`perm_code`, `perm_name`, `perm_type`, `parent_id`, `perm_path`, `sort`)
VALUES
  ('menu:user:product',   '商品集市', 'MENU', 0, '/product',      1),
  ('menu:user:publish',   '发布闲置', 'MENU', 0, '/publish',      2),
  ('menu:user:favorites', '我的收藏', 'MENU', 0, '/my-favorites', 4),
  ('menu:user:ai',        '智能助手', 'MENU', 0, '/ai',           6);

-- 二、管理员（role_id=1）拿到全部 11 条菜单
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.`id` FROM `sys_permission` p
 WHERE p.`perm_type` = 'MENU'
   AND p.`perm_code` IN ('menu:user:product','menu:user:publish','menu:user:favorites','menu:user:ai');

-- 三、普通学生（role_id=2）拿到 6 条学生端菜单
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.`id` FROM `sys_permission` p
 WHERE p.`perm_type` = 'MENU'
   AND p.`perm_code` IN ('menu:user:product','menu:user:publish','menu:user:favorites','menu:user:ai');

-- 四、自检：MENU 总数应为 11，管理员 11 条、学生 6 条
SELECT COUNT(*) AS `menu_total` FROM `sys_permission` WHERE `perm_type` = 'MENU';

SELECT rp.`role_id`, COUNT(*) AS `menu_cnt`
  FROM `sys_role_permission` rp
  JOIN `sys_permission` p ON p.`id` = rp.`permission_id`
 WHERE p.`perm_type` = 'MENU'
 GROUP BY rp.`role_id`
 ORDER BY rp.`role_id`;

SELECT p.`id`, p.`perm_code`, p.`perm_path`, p.`sort`
  FROM `sys_permission` p
 WHERE p.`perm_type` = 'MENU'
 ORDER BY p.`sort`, p.`id`;
