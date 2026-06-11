-- ============================================================
-- 权限管理系统数据库初始化脚本 v2
-- 数据库: auth_system
-- 菜单结构:
--   - 首页
--   - 系统管理（用户管理/角色管理/菜单管理）
--   - 手机卡管理（在用手机卡/备用手机卡/数据总览、代理商管理）
--   - 服务器管理（在用服务器/备用服务器/服务器总览）
--   - 实名人员管理（实名人员列表）
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
    perm_code VARCHAR(128) DEFAULT NULL COMMENT '权限编码',
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

-- ------------------------------------------------------------
-- 代理商表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS phone_agent;
CREATE TABLE phone_agent (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '代理商ID',
    agent_name VARCHAR(128) NOT NULL COMMENT '代理商名称',
    contact VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    address VARCHAR(255) DEFAULT NULL COMMENT '地址',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理商表';

-- ------------------------------------------------------------
-- 实名人员表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS phone_realname;
CREATE TABLE phone_realname (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '实名人员ID',
    real_name VARCHAR(64) NOT NULL COMMENT '真实姓名',
    id_card VARCHAR(32) DEFAULT NULL COMMENT '身份证号',
    phone VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    department VARCHAR(128) DEFAULT NULL COMMENT '所属部门',
    status TINYINT DEFAULT 1 COMMENT '状态 1:启用 0:禁用',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实名人员表';

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
('ROLE_OPERATOR', '运营员', '负责手机卡和服务器运营操作', 1),
('ROLE_VIEWER', '查看员', '仅可查看数据', 1);

-- 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3);

-- ============================================================
-- 菜单数据
-- 一级菜单ID分配: 100=首页, 10=系统管理, 20=手机卡管理, 30=服务器管理
-- ============================================================
INSERT INTO sys_menu (id, menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES

-- ========== 首页 ==========
(100, '首页', '/home', 'HomeFilled', 0, 0, 1, NULL, 1),

-- ========== 系统管理 ==========
(10, '系统管理', '', 'Setting', 0, 1, 1, '', 1),
(101, '用户管理', '/system/user', 'User', 10, 1, 1, 'system:user:view', 1),
(102, '角色管理', '/system/role', 'UserFilled', 10, 2, 1, 'system:role:view', 1),
(103, '菜单管理', '/system/menu', 'Menu', 10, 3, 1, 'system:menu:view', 1),

-- ========== 手机卡管理 ==========
(20, '手机卡管理', '/phone', 'Iphone', 0, 2, 1, '', 1),
(201, '在用手机卡', '/phone/active', 'Connection', 20, 1, 1, 'phone:active:view', 1),
(202, '备用手机卡', '/phone/backup', 'Tickets', 20, 2, 1, 'phone:backup:view', 1),
(203, '数据总览', '/phone/overview', 'DataAnalysis', 20, 3, 1, 'phone:overview:view', 1),
(204, '代理商管理', '/phone/agent', 'UserFilled', 20, 4, 1, 'phone:agent:view', 1),

-- ========== 服务器管理 ==========
(30, '服务器管理', '/server', 'Monitor', 0, 3, 1, '', 1),
(301, '在用服务器', '/server/active', 'Cpu', 30, 1, 1, 'server:active:view', 1),
(302, '备用服务器', '/server/backup', 'Box', 30, 2, 1, 'server:backup:view', 1),
(303, '服务器总览', '/server/overview', 'DataLine', 30, 3, 1, 'server:overview:view', 1),

-- ========== 实名人员管理 ==========
(40, '实名人员管理', '', 'Avatar', 0, 4, 1, '', 1),
(401, '实名人员列表', '/realname/list', 'User', 40, 1, 1, 'realname:list:view', 1);

-- ============================================================
-- 系统管理按钮权限
-- ============================================================
INSERT INTO sys_menu (menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
-- 用户管理按钮(parent_id=101)
('用户查询', '', '', 101, 1, 2, 'system:user:view', 1),
('用户新增', '', '', 101, 2, 2, 'system:user:add', 1),
('用户编辑', '', '', 101, 3, 2, 'system:user:edit', 1),
('用户删除', '', '', 101, 4, 2, 'system:user:delete', 1),

-- 角色管理按钮(parent_id=102)
('角色查询', '', '', 102, 1, 2, 'system:role:view', 1),
('角色新增', '', '', 102, 2, 2, 'system:role:add', 1),
('角色编辑', '', '', 102, 3, 2, 'system:role:edit', 1),
('角色删除', '', '', 102, 4, 2, 'system:role:delete', 1),

-- 菜单管理按钮(parent_id=103)
('菜单查询', '', '', 103, 1, 2, 'system:menu:view', 1),
('菜单新增', '', '', 103, 2, 2, 'system:menu:add', 1),
('菜单编辑', '', '', 103, 3, 2, 'system:menu:edit', 1),
('菜单删除', '', '', 103, 4, 2, 'system:menu:delete', 1);

-- ============================================================
-- 手机卡管理按钮权限
-- ============================================================
INSERT INTO sys_menu (menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
-- 在用手机卡按钮(parent_id=201)
('在用卡查询', '', '', 201, 1, 2, 'phone:active:view', 1),
('在用卡新增', '', '', 201, 2, 2, 'phone:active:add', 1),
('在用卡编辑', '', '', 201, 3, 2, 'phone:active:edit', 1),
('在用卡删除', '', '', 201, 4, 2, 'phone:active:delete', 1),

-- 备用手机卡按钮(parent_id=202)
('备用卡查询', '', '', 202, 1, 2, 'phone:backup:view', 1),
('备用卡新增', '', '', 202, 2, 2, 'phone:backup:add', 1),
('备用卡编辑', '', '', 202, 3, 2, 'phone:backup:edit', 1),
('备用卡删除', '', '', 202, 4, 2, 'phone:backup:delete', 1),

-- 代理商管理按钮(parent_id=204)
('代理商查询', '', '', 204, 1, 2, 'phone:agent:view', 1),
('代理商新增', '', '', 204, 2, 2, 'phone:agent:add', 1),
('代理商编辑', '', '', 204, 3, 2, 'phone:agent:edit', 1),
('代理商删除', '', '', 204, 4, 2, 'phone:agent:delete', 1),

-- 数据总览按钮(parent_id=203)
('数据总览查询', '', '', 203, 1, 2, 'phone:overview:view', 1),
('数据总览导出', '', '', 203, 2, 2, 'phone:overview:export', 1);

-- ============================================================
-- 服务器管理按钮权限
-- ============================================================
INSERT INTO sys_menu (menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
-- 在用服务器按钮(parent_id=301)
('在用服务器查询', '', '', 301, 1, 2, 'server:active:view', 1),
('在用服务器新增', '', '', 301, 2, 2, 'server:active:add', 1),
('在用服务器编辑', '', '', 301, 3, 2, 'server:active:edit', 1),
('在用服务器删除', '', '', 301, 4, 2, 'server:active:delete', 1),

-- 备用服务器按钮(parent_id=302)
('备用服务器查询', '', '', 302, 1, 2, 'server:backup:view', 1),
('备用服务器新增', '', '', 302, 2, 2, 'server:backup:add', 1),
('备用服务器编辑', '', '', 302, 3, 2, 'server:backup:edit', 1),
('备用服务器删除', '', '', 302, 4, 2, 'server:backup:delete', 1),

-- 服务器总览按钮(parent_id=303)
('服务器总览查询', '', '', 303, 1, 2, 'server:overview:view', 1),
('服务器总览导出', '', '', 303, 2, 2, 'server:overview:export', 1);

-- ============================================================
-- 实名人员管理按钮权限
-- ============================================================
INSERT INTO sys_menu (menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
-- 实名人员列表按钮(parent_id=401)
('实名人员查询', '', '', 401, 1, 2, 'realname:list:view', 1),
('实名人员新增', '', '', 401, 2, 2, 'realname:list:add', 1),
('实名人员编辑', '', '', 401, 3, 2, 'realname:list:edit', 1),
('实名人员删除', '', '', 401, 4, 2, 'realname:list:delete', 1);

-- ============================================================
-- 角色-菜单关联
-- ============================================================

-- 超级管理员: 拥有全部菜单/按钮
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 运营员: 首页 + 系统管理(仅查看用户) + 手机卡管理(含全部按钮) + 服务器管理(含全部按钮)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 100),  -- 首页
(2, 10),   -- 系统管理
(2, 101), (2, 101+1), (2, 101+2), (2, 101+3),  -- 用户管理(含按钮)
(2, 102),  -- 角色管理(仅查看)
(2, 103),  -- 菜单管理(仅查看)
(2, 20),   -- 手机卡管理
(2, 201), (2, 201+1), (2, 201+2), (2, 201+3),  -- 在用手机卡(含按钮)
(2, 202), (2, 202+1), (2, 202+2), (2, 202+3),  -- 备用手机卡(含按钮)
(2, 203), (2, 203+1), (2, 203+2),               -- 数据总览(含按钮)
(2, 204), (2, 204+1), (2, 204+2), (2, 204+3),  -- 代理商管理(含按钮)
(2, 30),   -- 服务器管理
(2, 301), (2, 301+1), (2, 301+2), (2, 301+3), -- 在用服务器(含按钮)
(2, 302), (2, 302+1), (2, 302+2), (2, 302+3), -- 备用服务器(含按钮)
(2, 303), (2, 303+1), (2, 303+2),              -- 服务器总览(含按钮)
(2, 40),   -- 实名人员管理
(2, 401), (2, 401+1), (2, 401+2), (2, 401+3); -- 实名人员列表(含按钮)

-- 查看员: 首页 + 系统管理(仅查看) + 手机卡管理(仅查看) + 服务器管理(仅查看)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 100),  -- 首页
(3, 10),   -- 系统管理
(3, 101), (3, 101+1),  -- 用户管理(仅查看+新增)
(3, 102),  -- 角色管理(仅查看)
(3, 103),  -- 菜单管理(仅查看)
(3, 20),   -- 手机卡管理
(3, 201),  -- 在用手机卡(仅查看)
(3, 202),  -- 备用手机卡(仅查看)
(3, 203),  -- 数据总览(仅查看)
(3, 204),  -- 代理商管理(仅查看)
(3, 30),   -- 服务器管理
(3, 301),  -- 在用服务器(仅查看)
(3, 302),  -- 备用服务器(仅查看)
(3, 303),  -- 服务器总览(仅查看)
(3, 40),   -- 实名人员管理(仅查看)
(3, 401);  -- 实名人员列表(仅查看)

-- ============================================================
-- 默认账号
--   admin / admin123  (超级管理员 - 全部权限)
--   operator / admin123 (运营员 - 手机卡/服务器，含编辑)
--   viewer / admin123 (查看员 - 仅查看，无编辑按钮)
-- ============================================================
