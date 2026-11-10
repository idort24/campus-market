-- ============================================================
-- 校园轻集市 campus_market 初始化数据脚本
-- 文件：02_seed.sql
-- 前置：先执行 01_schema.sql 建表
-- 幂等：全部使用 INSERT IGNORE，重复执行不会重复插入、不会报错
-- 执行方式：mysql -u root -p < 02_seed.sql
-- ============================================================

USE `campus_market`;
SET NAMES utf8mb4;

-- ============================================================
-- 一、角色（2 个内置角色，后端 RbacServiceImpl 里禁止删除）
-- ============================================================
INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `role_desc`, `sort`, `status`) VALUES
('ADMIN',   '管理员',   '系统管理员，拥有全部权限',   1, 1),
('STUDENT', '普通学生', '普通学生用户，基础买卖权限', 2, 1);

-- ============================================================
-- 二、权限
-- 分两类：
--   API  16 条 = 后端接口权限码，同时也是前端 v-permission 的按钮权限码
--   MENU  7 条 = 前端左侧菜单，perm_path 存 vue-router 的路由地址
--
-- 【红线】以下 6 个码必须与 AuthInterceptor.ADMIN_PERMS 完全一致，
--         改一个字符，管理端接口就会全部 403：
--         user:manage  user:status  goods:audit
--         report:handle  category:manage  dict:manage
-- ============================================================

-- 2.1 API 类权限（原有 16 条，保持不变）
INSERT IGNORE INTO `sys_permission` (`perm_code`, `perm_name`, `perm_type`, `parent_id`, `perm_path`, `sort`) VALUES
('goods:view',        '浏览商品',   'API', 0, 'GET /api/v1/goods',                    1),
('goods:publish',     '发布商品',   'API', 0, 'POST /api/v1/goods',                   2),
('goods:edit',        '编辑商品',   'API', 0, 'PUT /api/v1/goods/{id}',               3),
('goods:delete',      '删除商品',   'API', 0, 'DELETE /api/v1/goods/{id}',            4),
('goods:status',      '上下架商品', 'API', 0, 'PUT /api/v1/goods/{id}/status',        5),
('goods:audit',       '商品审核',   'API', 0, 'PUT /api/v1/admin/goods/{id}/audit',   6),
('favorites:manage',  '收藏管理',   'API', 0, 'POST /api/v1/favorites',               7),
('order:manage',      '订单管理',   'API', 0, 'POST /api/v1/orders',                  8),
('evaluation:create', '发布评价',   'API', 0, 'POST /api/v1/evaluations',             9),
('message:send',      '发送消息',   'API', 0, 'POST /api/v1/messages',               10),
('report:create',     '提交举报',   'API', 0, 'POST /api/v1/reports',                11),
('report:handle',     '举报处理',   'API', 0, 'PUT /api/v1/reports/{id}/status',     12),
('user:manage',       '用户管理',   'API', 0, 'GET /api/v1/admin/users',              13),
('user:status',       '用户封禁',   'API', 0, 'PUT /api/v1/admin/users/{id}/status',  14),
('category:manage',   '分类管理',   'API', 0, 'POST /api/v1/categories',             15),
('dict:manage',       '字典管理',   'API', 0, 'GET /api/v1/dicts',                   16);

-- 2.2 MENU 类权限（前端路由菜单，perm_path 与 router/index.js 的路径保持一致）
-- 共 11 条：学生端 6 条 + 后台 5 条，对齐 docs/api-auth.md 第四节
INSERT IGNORE INTO `sys_permission` (`perm_code`, `perm_name`, `perm_type`, `parent_id`, `perm_path`, `sort`) VALUES
('menu:user:product',   '商品集市', 'MENU', 0, '/product',           1),
('menu:user:publish',   '发布闲置', 'MENU', 0, '/publish',           2),
('menu:my-goods',       '我的发布', 'MENU', 0, '/my-publishes',     26),
('menu:user:favorites', '我的收藏', 'MENU', 0, '/my-favorites',      4),
('menu:my-order',       '我的订单', 'MENU', 0, '/my-orders',        27),
('menu:user:ai',        '智能助手', 'MENU', 0, '/ai',                6),
('menu:user',           '用户管理', 'MENU', 0, '/admin/users',      21),
('menu:goods',          '商品审核', 'MENU', 0, '/admin/goods',      22),
('menu:report',         '举报处理', 'MENU', 0, '/admin/reports',    23),
('menu:category',       '分类管理', 'MENU', 0, '/admin/categories', 24),
('menu:role',           '角色权限', 'MENU', 0, '/admin/roles',      25);

-- ============================================================
-- 三、角色-权限绑定
-- 用 perm_code 关联，不依赖自增 id，重建库后也不会错位
-- ============================================================

-- 3.1 ADMIN：拥有全部权限（23 条）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `sys_role` r
JOIN `sys_permission` p ON p.is_deleted = 0
WHERE r.role_code = 'ADMIN' AND r.is_deleted = 0;

-- 3.2 STUDENT：基础买卖权限 10 条 + 用户端菜单 6 条（共 16 条）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `sys_role` r
JOIN `sys_permission` p ON p.is_deleted = 0
WHERE r.role_code = 'STUDENT' AND r.is_deleted = 0
  AND p.perm_code IN (
    'goods:view', 'goods:publish', 'goods:edit', 'goods:delete', 'goods:status',
    'favorites:manage', 'order:manage', 'evaluation:create', 'message:send',
    'report:create',
    'menu:user:product', 'menu:user:publish', 'menu:my-goods',
    'menu:user:favorites', 'menu:my-order', 'menu:user:ai'
  );

-- ============================================================
-- 四、字典
-- 【注意】dict_type / dict_data / goods_category 这三张表没有唯一键，
--        下面三段不具备幂等性：重复执行 02 会插出重复行。
--        正确用法是 01 建表 → 02 灌数据，只跑一轮。
--        若要重来，先重新执行 01（会 DROP 重建表），再跑 02。
-- ============================================================

-- 4.1 字典类型（原有 4 类 + 新增账号状态 1 类）
INSERT IGNORE INTO `dict_type` (`dict_code`, `dict_name`, `sort`, `remark`) VALUES
('goods_status',   '商品状态', 1, '商品上下架、售出状态'),
('audit_status',   '审核状态', 2, '商品审核结果'),
('goods_quality',  '商品成色', 3, '新旧成色字典'),
('order_status',   '订单意向状态', 4, '线下意向订单状态'),
('account_status', '账号状态', 5, '用户账号状态，sys_user.account_status 关联此字典');

-- 4.2 字典数据（原有 15 条 + 账号状态 4 条）
INSERT IGNORE INTO `dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort`) VALUES
('goods_status', '上架',   'ON_SHELF', 1),
('goods_status', '下架',   'OFF_SHELF', 2),
('goods_status', '已售出', 'SOLD', 3),
('audit_status', '待审核',   'PENDING', 1),
('audit_status', '审核通过', 'PASS', 2),
('audit_status', '审核驳回', 'REJECT', 3),
('goods_quality', '全新',   'NEW', 1),
('goods_quality', '九成新', '90', 2),
('goods_quality', '八成新', '80', 3),
('goods_quality', '七成新', '70', 4),
('goods_quality', '有磨损', 'USED', 5),
('order_status', '待确认',   'WAIT_CONFIRM', 1),
('order_status', '待交易',   'WAIT_TRADE', 2),
('order_status', '交易完成', 'FINISH', 3),
('order_status', '已取消',   'CANCEL', 4),
('account_status', '正常',     'NORMAL', 1),
('account_status', '警告',     'WARN', 2),
('account_status', '限制发布', 'LIMIT', 3),
('account_status', '封禁',     'BAN', 4);

-- ============================================================
-- 五、商品分类（一级 5 个 + 二级 4 个）
-- ============================================================
INSERT IGNORE INTO `goods_category` (`category_name`, `parent_id`, `sort`) VALUES
('教材书籍', 0, 1),
('电子产品', 0, 2),
('服饰鞋包', 0, 3),
('生活用品', 0, 4),
('体育器材', 0, 5),
('计算机教材', 1, 1),
('考研资料',   1, 2),
('手机',       2, 1),
('耳机',       2, 2);

-- ============================================================
-- 六、测试账号（密码统一 123456）
--
-- 【最容易踩的坑】BCrypt 密文以 $2b$12$ 开头，里面有三段 $ 会被
-- PowerShell 当成变量解析，直接把前缀吃掉，哈希变成 32 字符的坏串，
-- 导入后登录必失败。所以下面这行必须原样保存，不要用 echo/双引号拼接。
--
-- 密文：$2b$12$k4q.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq
-- 明文：123456（已用 BCrypt 校验通过）
-- 校验方式：SELECT LENGTH(password) FROM sys_user;  应为 60
-- ============================================================

INSERT IGNORE INTO `sys_user`
  (`student_no`, `phone`, `email`, `password`, `nick_name`, `department`, `grade`, `user_type`, `account_status`) VALUES
('2024110640', '13800000001', 'zhangay@test.com',      '$2b$12$k4q.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq', '张爱阳',   '软件工程', '2024级', 9, 'NORMAL'),
('2024777777', '13800000002', '2024777777@qq.com',     '$2b$12$k4q.tZh/r11iFIsF3/63U.KuU.m8C1PEVU4uR3nVr3.zVVAmGEwMq', '测试学生', '软件工程', '2024级', 1, 'NORMAL');

-- ============================================================
-- 七、测试账号的角色绑定
-- 按 student_no 关联，不写死 user_id / role_id
-- ============================================================

-- 管理员账号 → ADMIN
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.role_code = 'ADMIN'
WHERE u.student_no = '2024110640';

-- 学生账号 → STUDENT
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.role_code = 'STUDENT'
WHERE u.student_no = '2024777777';

-- ============================================================
-- 初始化完成。跑完执行 03_check.sql 自检。
-- ============================================================
