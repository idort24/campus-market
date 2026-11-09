-- ============================================================
-- 校园轻集市 campus_market 初始化自检脚本
-- 文件：03_check.sql
-- 用法：mysql -u root -p campus_market < 03_check.sql
-- 全部结论为 OK 才算初始化成功；出现 FAIL 时看 README 的排查表
-- ============================================================

USE `campus_market`;

-- ------------------------------------------------------------
-- 一、汇总报表：一眼看出哪一项不合格
-- ------------------------------------------------------------
SELECT '建表数量' AS 检查项, '17' AS 期望, CAST(COUNT(*) AS CHAR) AS 实际,
       IF(COUNT(*) = 17, 'OK', 'FAIL') AS 结论
FROM information_schema.tables
WHERE table_schema = 'campus_market' AND table_type = 'BASE TABLE'

UNION ALL
SELECT '权限总数', '23', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 23, 'OK', 'FAIL')
FROM `sys_permission` WHERE is_deleted = 0

UNION ALL
SELECT 'API类权限', '16', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 16, 'OK', 'FAIL')
FROM `sys_permission` WHERE is_deleted = 0 AND perm_type = 'API'

UNION ALL
SELECT 'MENU类权限', '7', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 7, 'OK', 'FAIL')
FROM `sys_permission` WHERE is_deleted = 0 AND perm_type = 'MENU'

UNION ALL
SELECT '6个管理权限码齐全', '6', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 6, 'OK', 'FAIL')
FROM `sys_permission`
WHERE is_deleted = 0
  AND perm_code IN ('user:manage','user:status','goods:audit','report:handle','category:manage','dict:manage')

UNION ALL
SELECT 'ADMIN角色权限数', '23', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 23, 'OK', 'FAIL')
FROM `sys_role_permission` rp
JOIN `sys_role` r ON r.id = rp.role_id
WHERE r.role_code = 'ADMIN' AND rp.is_deleted = 0

UNION ALL
SELECT 'STUDENT角色权限数', '12', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 12, 'OK', 'FAIL')
FROM `sys_role_permission` rp
JOIN `sys_role` r ON r.id = rp.role_id
WHERE r.role_code = 'STUDENT' AND rp.is_deleted = 0

UNION ALL
SELECT '学生越权管理码数(应为0)', '0', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 0, 'OK', 'FAIL')
FROM `sys_role_permission` rp
JOIN `sys_role` r ON r.id = rp.role_id
JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE r.role_code = 'STUDENT' AND rp.is_deleted = 0
  AND p.perm_code IN ('user:manage','user:status','goods:audit','report:handle','category:manage','dict:manage')

UNION ALL
SELECT '用户数', '2', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 2, 'OK', 'FAIL')
FROM `sys_user` WHERE is_deleted = 0

UNION ALL
SELECT '密码哈希长度均为60', '2', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 2, 'OK', 'FAIL')
FROM `sys_user` WHERE is_deleted = 0 AND CHAR_LENGTH(password) = 60

UNION ALL
SELECT '管理员user_type=9', '1', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 1, 'OK', 'FAIL')
FROM `sys_user` WHERE is_deleted = 0 AND student_no = '2024110640' AND user_type = 9

UNION ALL
SELECT '学生user_type=1', '1', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 1, 'OK', 'FAIL')
FROM `sys_user` WHERE is_deleted = 0 AND student_no = '2024777777' AND user_type = 1

UNION ALL
SELECT '用户角色绑定数', '2', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 2, 'OK', 'FAIL')
FROM `sys_user_role` WHERE is_deleted = 0

UNION ALL
SELECT '字典类型数', '5', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 5, 'OK', 'FAIL')
FROM `dict_type` WHERE is_deleted = 0

UNION ALL
SELECT '字典数据数', '19', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 19, 'OK', 'FAIL')
FROM `dict_data` WHERE is_deleted = 0

UNION ALL
SELECT '商品分类数', '9', CAST(COUNT(*) AS CHAR), IF(COUNT(*) = 9, 'OK', 'FAIL')
FROM `goods_category` WHERE is_deleted = 0

UNION ALL
SELECT '关联表唯一键已建(3个)', '3', CAST(COUNT(DISTINCT index_name) AS CHAR),
       IF(COUNT(DISTINCT index_name) = 3, 'OK', 'FAIL')
FROM information_schema.statistics
WHERE table_schema = 'campus_market'
  AND non_unique = 0
  AND index_name IN ('uk_user_role','uk_role_perm','uk_user_goods');

-- ------------------------------------------------------------
-- 二、账号登录信息（人工核对用）
-- ------------------------------------------------------------
SELECT
  u.student_no   AS 学号,
  u.nick_name    AS 昵称,
  u.email        AS 邮箱,
  u.user_type    AS 用户类型,
  GROUP_CONCAT(DISTINCT r.role_code) AS 角色,
  CHAR_LENGTH(u.password) AS 密码长度,
  LEFT(u.password, 7)     AS 哈希前缀
FROM `sys_user` u
LEFT JOIN `sys_user_role` ur ON ur.user_id = u.id AND ur.is_deleted = 0
LEFT JOIN `sys_role` r       ON r.id = ur.role_id
WHERE u.is_deleted = 0
GROUP BY u.id, u.student_no, u.nick_name, u.email, u.user_type, u.password;

-- ------------------------------------------------------------
-- 三、管理员应能看到的菜单（前端左侧栏的数据来源）
-- ------------------------------------------------------------
SELECT p.perm_code AS 权限码, p.perm_name AS 菜单名, p.perm_path AS 路由
FROM `sys_role` r
JOIN `sys_role_permission` rp ON rp.role_id = r.id AND rp.is_deleted = 0
JOIN `sys_permission` p       ON p.id = rp.permission_id AND p.is_deleted = 0
WHERE r.role_code = 'ADMIN' AND p.perm_type = 'MENU'
ORDER BY p.sort;

-- ------------------------------------------------------------
-- 四、学生应能看到的菜单
-- ------------------------------------------------------------
SELECT p.perm_code AS 权限码, p.perm_name AS 菜单名, p.perm_path AS 路由
FROM `sys_role` r
JOIN `sys_role_permission` rp ON rp.role_id = r.id AND rp.is_deleted = 0
JOIN `sys_permission` p       ON p.id = rp.permission_id AND p.is_deleted = 0
WHERE r.role_code = 'STUDENT' AND p.perm_type = 'MENU'
ORDER BY p.sort;
