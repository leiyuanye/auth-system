-- ============================================================
-- 权限管理系统数据库初始化脚本
-- 数据库: auth_system
-- ============================================================

CREATE DATABASE IF NOT EXISTS auth_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auth_system;

-- ------------------------------------------------------------
-- 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码(MD5加密)',
    real_name VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 角色表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ------------------------------------------------------------
-- 用户-角色关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- ------------------------------------------------------------
-- 菜单表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    menu_path VARCHAR(128) DEFAULT NULL COMMENT '菜单路径/路由',
    menu_icon VARCHAR(64) DEFAULT NULL COMMENT '菜单图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父级菜单ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    menu_type TINYINT DEFAULT 1 COMMENT '类型 1:菜单 2:按钮',
    perm_code VARCHAR(128) DEFAULT NULL COMMENT '权限编码(如 phone:edit)',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单/权限表';

-- ------------------------------------------------------------
-- 角色-菜单(权限)关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单(权限)关联表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 用户: admin / admin123 (密码MD5=0192023a7bbd73250516f069df18b500)
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 1),
('operator', '0192023a7bbd73250516f069df18b500', '运营员', 1),
('viewer', '0192023a7bbd73250516f069df18b500', '查看员', 1);

-- 角色
INSERT INTO sys_role (role_code, role_name, description, status) VALUES
('ROLE_ADMIN', '超级管理员', '拥有所有权限', 1),
('ROLE_OPERATOR', '运营员', '运营数据相关操作', 1),
('ROLE_VIEWER', '查看员', '仅可查看', 1);

-- 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),   -- admin -> 超级管理员
(2, 2),   -- operator -> 运营员
(3, 3);   -- viewer -> 查看员

-- 菜单数据(显式指定 id，便于 parent_id 引用)
INSERT INTO sys_menu (id, menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
-- 首页(一级)
(100, '首页', '/home', 'HomeFilled', 0, 0, 1, NULL, 1),

-- 一级菜单
(1, '系统管理', '', 'Setting', 0, 1, 1, '', 1),
(2, '手机卡管理', '/phone', 'Phone', 0, 2, 1, '', 1),
(3, '运营数据', '/operation', 'DataAnalysis', 0, 3, 1, '', 1),

-- 系统管理 - 子菜单
(11, '用户管理', '/system/user', 'User', 1, 1, 1, 'system:user:view', 1),
(12, '角色管理', '/system/role', 'UserFilled', 1, 2, 1, 'system:role:view', 1),
(13, '菜单管理', '/system/menu', 'Menu', 1, 3, 1, 'system:menu:view', 1),

-- 手机卡管理 - 子菜单
(21, '卡列表', '/phone/list', 'Document', 2, 1, 1, 'phone:list:view', 1),

-- 运营数据 - 子菜单
(31, '数据概览', '/operation/summary', 'Histogram', 3, 1, 1, 'operation:summary:view', 1),
(32, '每日报表', '/operation/report', 'Document', 3, 2, 1, 'operation:report:view', 1),

-- 用户管理按钮权限(parent_id=11)
(111, '用户查询', '', '', 11, 1, 2, 'system:user:view', 1),
(112, '用户新增', '', '', 11, 2, 2, 'system:user:add', 1),
(113, '用户编辑', '', '', 11, 3, 2, 'system:user:edit', 1),
(114, '用户删除', '', '', 11, 4, 2, 'system:user:delete', 1),

-- 角色管理按钮权限(parent_id=12)
(121, '角色查询', '', '', 12, 1, 2, 'system:role:view', 1),
(122, '角色新增', '', '', 12, 2, 2, 'system:role:add', 1),
(123, '角色编辑', '', '', 12, 3, 2, 'system:role:edit', 1),
(124, '角色删除', '', '', 12, 4, 2, 'system:role:delete', 1),

-- 菜单管理按钮权限(parent_id=13)
(131, '菜单查询', '', '', 13, 1, 2, 'system:menu:view', 1),
(132, '菜单新增', '', '', 13, 2, 2, 'system:menu:add', 1),
(133, '菜单编辑', '', '', 13, 3, 2, 'system:menu:edit', 1),
(134, '菜单删除', '', '', 13, 4, 2, 'system:menu:delete', 1),

-- 手机卡管理按钮权限(parent_id=2)
(201, '手机卡查询', '', '', 2, 1, 2, 'phone:list:view', 1),
(202, '手机卡新增', '', '', 2, 2, 2, 'phone:list:add', 1),
(203, '手机卡编辑', '', '', 2, 3, 2, 'phone:list:edit', 1),
(204, '手机卡删除', '', '', 2, 4, 2, 'phone:list:delete', 1),

-- 运营数据按钮权限(parent_id=3)
(301, '概览查询', '', '', 3, 1, 2, 'operation:summary:view', 1),
(302, '概览编辑', '', '', 3, 2, 2, 'operation:summary:edit', 1),
(303, '报表查询', '', '', 3, 3, 2, 'operation:report:view', 1),
(304, '报表编辑', '', '', 3, 4, 2, 'operation:report:edit', 1);

-- 角色-菜单关联(超级管理员拥有全部菜单/按钮权限)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 运营员: 首页 + 手机卡管理(含按钮) + 运营数据(含按钮)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 100),
(2, 2), (2, 21), (2, 201), (2, 202), (2, 203), (2, 204),
(2, 3), (2, 31), (2, 32), (2, 301), (2, 302), (2, 303), (2, 304);

-- 查看员: 首页 + 手机卡管理(仅查看) + 运营数据(仅查看)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 100),
(3, 2), (3, 21), (3, 201),
(3, 3), (3, 31), (3, 32), (3, 301), (3, 303);

-- ============================================================
-- 完成提示
-- ============================================================
-- 启动项目后访问: http://localhost:8080
-- 默认登录账号:
--   admin / admin123  (超级管理员 - 全部权限)
--   operator / admin123 (运营员 - 手机卡+运营数据,含编辑)
--   viewer / admin123 (查看员 - 仅查看,无编辑按钮)
