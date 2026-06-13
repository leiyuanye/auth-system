CREATE DATABASE IF NOT EXISTS auth_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auth_system;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(128) NOT NULL,
    real_name VARCHAR(64) DEFAULT NULL,
    email VARCHAR(128) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    menu_name VARCHAR(64) NOT NULL,
    menu_path VARCHAR(128) DEFAULT NULL,
    menu_icon VARCHAR(64) DEFAULT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    menu_type TINYINT DEFAULT 1,
    perm_code VARCHAR(128) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS sys_operate_log;
CREATE TABLE sys_operate_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    module_name VARCHAR(128) NOT NULL,
    operate_type VARCHAR(32) NOT NULL,
    data_id BIGINT DEFAULT NULL,
    data_name VARCHAR(255) DEFAULT NULL,
    field_changed VARCHAR(512) DEFAULT NULL,
    old_value TEXT,
    new_value TEXT,
    operator VARCHAR(64) DEFAULT NULL,
    operate_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(512) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_module (module_name),
    KEY idx_operator (operator),
    KEY idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS phone_agent;
CREATE TABLE phone_agent (
    id BIGINT NOT NULL AUTO_INCREMENT,
    agent_name VARCHAR(128) NOT NULL,
    contact VARCHAR(64) DEFAULT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS phone_realname;
CREATE TABLE phone_realname (
    id BIGINT NOT NULL AUTO_INCREMENT,
    real_name VARCHAR(64) NOT NULL,
    phone VARCHAR(32) DEFAULT NULL,
    department VARCHAR(128) DEFAULT NULL,
    scan_status TINYINT DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS phone_card;
CREATE TABLE phone_card (
    id BIGINT NOT NULL AUTO_INCREMENT,
    card_number VARCHAR(64) NOT NULL,
    agent_id BIGINT DEFAULT NULL,
    agent_name VARCHAR(128) DEFAULT NULL,
    phone_number VARCHAR(32) DEFAULT NULL,
    realname_id BIGINT DEFAULT NULL,
    realname_name VARCHAR(64) DEFAULT NULL,
    department VARCHAR(128) DEFAULT NULL,
    package VARCHAR(128) DEFAULT NULL,
    card_status TINYINT DEFAULT 1,
    card_type TINYINT DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_card_number (card_number),
    KEY idx_agent_id (agent_id),
    KEY idx_realname_id (realname_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS sys_server;
CREATE TABLE sys_server (
    id BIGINT NOT NULL AUTO_INCREMENT,
    server_name VARCHAR(128) DEFAULT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    server_type VARCHAR(64) DEFAULT NULL,
    location VARCHAR(128) DEFAULT NULL,
    specs VARCHAR(128) DEFAULT NULL,
    mfa_key VARCHAR(255) DEFAULT NULL,
    server_status TINYINT DEFAULT 1,
    remote_account VARCHAR(64) DEFAULT NULL,
    remote_pwd VARCHAR(128) DEFAULT NULL,
    backend_account VARCHAR(64) DEFAULT NULL,
    backend_pwd VARCHAR(128) DEFAULT NULL,
    remark VARCHAR(512) DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_time DATE DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_ip (ip_address),
    KEY idx_server_status (server_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '0192023a7bbd73250516f069df18b500', '超级管理员', 1),
('operator', '0192023a7bbd73250516f069df18b500', '运营员', 1),
('viewer', '0192023a7bbd73250516f069df18b500', '查看员', 1);

INSERT INTO sys_role (role_code, role_name, description, status) VALUES
('ROLE_ADMIN', '超级管理员', '拥有所有权限', 1),
('ROLE_OPERATOR', '运营员', '负责手机卡和服务器运营操作', 1),
('ROLE_VIEWER', '查看员', '仅可查看数据', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 3);

INSERT INTO phone_agent (agent_name, contact, phone, address, status, remark) VALUES
('XX科技有限公司', '张经理', '13800000001', '北京市朝阳区', 1, '主要代理商'),
('YY通信服务中心', '李主任', '13800000002', '上海市浦东新区', 1, '长期合作'),
('ZZ网络科技', '王主管', '13800000003', '广州市天河区', 1, '代理商');

INSERT INTO phone_realname (real_name, phone, department, scan_status, remark) VALUES
('张三', '13800138001', '销售部', 2, '销售代表'),
('李四', '13800138002', '技术部', 2, '后端工程师'),
('王五', '13800138003', '运营部', 1, '无法识别人脸'),
('赵六', '13800138004', '客服部', 3, '需多次识别'),
('孙七', '13800138005', '市场部', 2, '市场专员');

INSERT INTO phone_card (card_number, agent_id, agent_name, phone_number, realname_id, realname_name, department, package, card_status, card_type, remark) VALUES
('89860123456789001', 1, 'XX科技有限公司', '13800138001', 1, '张三', '销售部', '59元/月', 1, 1, '销售部在用'),
('89860123456789002', 2, 'YY通信服务中心', '13800138002', 2, '李四', '技术部', '79元/月', 1, 1, '技术部在用'),
('89860123456789003', 3, 'ZZ网络科技', '13800138003', 3, '王五', '运营部', '99元/月', 2, 1, '需二次实名'),
('89860123456789004', 1, 'XX科技有限公司', '13800138004', 4, '赵六', '客服部', '59元/月', 3, 1, '欠费待处理'),
('89860123456789005', 2, 'YY通信服务中心', '13800138005', 5, '孙七', '市场部', '49元/月', 1, 1, '市场部在用'),
('89860987654321001', 1, 'XX科技有限公司', '13811112222', NULL, NULL, '', '39元/月', 1, 2, '库存备用卡-01'),
('89860987654321002', 2, 'YY通信服务中心', '13811113333', NULL, NULL, '', '49元/月', 1, 2, '库存备用卡-02'),
('89860987654321003', 3, 'ZZ网络科技', '13811114444', NULL, NULL, '', '59元/月', 2, 2, '需二次实名的备用卡');

INSERT INTO sys_server (server_name, ip_address, server_type, location, specs, mfa_key, server_status, remote_account, remote_pwd, backend_account, backend_pwd, remark, expire_time) VALUES
('DB-Master-01', '10.0.1.101', '腾讯云', '广州', '数据库组', 'JBSWY3DPEHPK3PXP', 1, 'root', 'dbRoot2026', 'admin', 'admin@db', '主数据库服务器', '2026-12-31 23:59:59'),
('APP-Server-01', '10.0.1.102', '阿里云', '杭州', '应用组', 'K9RX8TMQZ7HPK3A1', 1, 'root', 'appRoot2026', 'app_admin', 'app@2026', '业务应用服务器', '2026-10-15 23:59:59'),
('Cache-Server-01', '10.0.1.103', '华为云', '北京', '缓存组', 'M2NV4ZTL6HPK8BX9', 2, 'redis_admin', 'CacheAdmin2026', 'admin', 'admin@cache', 'Redis缓存服务器（维护中）', '2025-08-20 23:59:59'),
('Backup-Server-01', '10.0.1.104', '物理服务器', '上海', '备份组', 'P7QW6YHR1HPK2CDE', 4, 'backup_admin', 'BkAdmin2026', 'admin', 'admin@backup', '定时备份服务器（已到期）', '2025-01-15 23:59:59'),
('Spare-Server-01', '10.0.2.101', '腾讯云', '广州', '数据库组', 'A1BC2DEF3HPK4GH5', 3, 'root', 'spareRoot2026', 'admin', 'admin@spare', '备用数据库服务器', '2027-06-30 23:59:59'),
('Spare-Server-02', '10.0.2.102', '阿里云', '杭州', '应用组', 'B2CD3EFG4HPK5IJ6', 1, 'root', 'appSpare2026', 'admin', 'admin@spare', '备用应用服务器', '2026-09-01 23:59:59'),
('Spare-Server-03', '10.0.2.103', '华为云', '成都', '缓存组', 'C3DE4FGH5HPK6JK7', 1, 'redis_admin', 'cacheSpare2026', 'admin', 'admin@spare', '备用缓存服务器', '2027-03-20 23:59:59');

DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT NOT NULL AUTO_INCREMENT,
    dict_type VARCHAR(64) NOT NULL,
    dict_key VARCHAR(64) NOT NULL,
    dict_value VARCHAR(128) NOT NULL,
    sort_order INT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
('server_type', '腾讯云', '腾讯云', 1),
('server_type', '阿里云', '阿里云', 2),
('server_type', '华为云', '华为云', 3),
('server_type', '物理服务器', '物理服务器', 4),
('server_type', '其他', '其他', 5),
('server_group', '数据库组', '数据库组', 1),
('server_group', '应用组', '应用组', 2),
('server_group', '缓存组', '缓存组', 3),
('server_group', '备份组', '备份组', 4),
('server_group', '其他', '其他', 5),
('server_status', '1', '运行中', 1),
('server_status', '2', '维护中', 2),
('server_status', '3', '已下线', 3),
('server_status', '4', '到期', 4);

INSERT INTO sys_menu (id, menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
(100, '首页', '/home', 'HomeFilled', 0, 0, 1, NULL, 1),
(10, '系统管理', '/system', 'Setting', 0, 1, 1, '', 1),
(101, '用户管理', '/system/user', 'User', 10, 1, 1, 'system:user:view', 1),
(102, '角色管理', '/system/role', 'UserFilled', 10, 2, 1, 'system:role:view', 1),
(103, '菜单管理', '/system/menu', 'Menu', 10, 3, 1, 'system:menu:view', 1),
(104, '日志管理', '/system/log', 'Document', 10, 4, 1, 'system:log:view', 1),
(105, '数据字典', '/system/dict', 'Collection', 10, 5, 1, 'system:dict:view', 1),
(20, '手机卡管理', '/phone', 'Iphone', 0, 2, 1, '', 1),
(201, '在用手机卡', '/phone/active', 'Connection', 20, 1, 1, 'phone:active:view', 1),
(202, '备用手机卡', '/phone/backup', 'Tickets', 20, 2, 1, 'phone:backup:view', 1),
(203, '数据总览', '/phone/overview', 'DataAnalysis', 20, 3, 1, 'phone:overview:view', 1),
(204, '代理商管理', '/phone/agent', 'UserFilled', 20, 4, 1, 'phone:agent:view', 1),
(30, '服务器管理', '/server', 'Monitor', 0, 3, 1, '', 1),
(301, '服务器', '/server/list', 'Monitor', 30, 1, 1, 'server:active:view', 1),
(303, '服务器总览', '/server/overview', 'DataLine', 30, 3, 1, 'server:overview:view', 1),
(40, '实名人员管理', '/realname', 'Avatar', 0, 4, 1, '', 1),
(401, '实名人员', '/realname/list', 'User', 40, 1, 1, 'realname:list:view', 1);

INSERT INTO sys_menu (id, menu_name, menu_path, menu_icon, parent_id, sort_order, menu_type, perm_code, status) VALUES
(10101, '用户查询', '', '', 101, 1, 2, 'system:user:view', 1),
(10102, '用户新增', '', '', 101, 2, 2, 'system:user:add', 1),
(10103, '用户编辑', '', '', 101, 3, 2, 'system:user:edit', 1),
(10104, '用户删除', '', '', 101, 4, 2, 'system:user:delete', 1),
(10201, '角色查询', '', '', 102, 1, 2, 'system:role:view', 1),
(10202, '角色新增', '', '', 102, 2, 2, 'system:role:add', 1),
(10203, '角色编辑', '', '', 102, 3, 2, 'system:role:edit', 1),
(10204, '角色删除', '', '', 102, 4, 2, 'system:role:delete', 1),
(10301, '菜单查询', '', '', 103, 1, 2, 'system:menu:view', 1),
(10302, '菜单新增', '', '', 103, 2, 2, 'system:menu:add', 1),
(10303, '菜单编辑', '', '', 103, 3, 2, 'system:menu:edit', 1),
(10304, '菜单删除', '', '', 103, 4, 2, 'system:menu:delete', 1),
(10401, '日志查询', '', '', 104, 1, 2, 'system:log:view', 1),
(10501, '字典查询', '', '', 105, 1, 2, 'system:dict:view', 1),
(10502, '字典新增', '', '', 105, 2, 2, 'system:dict:add', 1),
(10503, '字典编辑', '', '', 105, 3, 2, 'system:dict:edit', 1),
(10504, '字典删除', '', '', 105, 4, 2, 'system:dict:delete', 1),
(20101, '在用卡查询', '', '', 201, 1, 2, 'phone:active:view', 1),
(20102, '在用卡新增', '', '', 201, 2, 2, 'phone:active:add', 1),
(20103, '在用卡编辑', '', '', 201, 3, 2, 'phone:active:edit', 1),
(20104, '在用卡删除', '', '', 201, 4, 2, 'phone:active:delete', 1),
(20201, '备用卡查询', '', '', 202, 1, 2, 'phone:backup:view', 1),
(20202, '备用卡新增', '', '', 202, 2, 2, 'phone:backup:add', 1),
(20203, '备用卡编辑', '', '', 202, 3, 2, 'phone:backup:edit', 1),
(20204, '备用卡删除', '', '', 202, 4, 2, 'phone:backup:delete', 1),
(20401, '代理商查询', '', '', 204, 1, 2, 'phone:agent:view', 1),
(20402, '代理商新增', '', '', 204, 2, 2, 'phone:agent:add', 1),
(20403, '代理商编辑', '', '', 204, 3, 2, 'phone:agent:edit', 1),
(20404, '代理商删除', '', '', 204, 4, 2, 'phone:agent:delete', 1),
(20301, '数据总览查询', '', '', 203, 1, 2, 'phone:overview:view', 1),
(20302, '数据总览导出', '', '', 203, 2, 2, 'phone:overview:export', 1),
(30101, '服务器查询', '', '', 301, 1, 2, 'server:active:view', 1),
(30102, '服务器新增', '', '', 301, 2, 2, 'server:active:add', 1),
(30103, '服务器编辑', '', '', 301, 3, 2, 'server:active:edit', 1),
(30104, '服务器删除', '', '', 301, 4, 2, 'server:active:delete', 1),
(30301, '服务器总览查询', '', '', 303, 1, 2, 'server:overview:view', 1),
(30302, '服务器总览导出', '', '', 303, 2, 2, 'server:overview:export', 1),
(40101, '实名人员查询', '', '', 401, 1, 2, 'realname:list:view', 1),
(40102, '实名人员新增', '', '', 401, 2, 2, 'realname:list:add', 1),
(40103, '实名人员编辑', '', '', 401, 3, 2, 'realname:list:edit', 1),
(40104, '实名人员删除', '', '', 401, 4, 2, 'realname:list:delete', 1);

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 100), (2, 10),
(2, 101), (2, 10101), (2, 10102), (2, 10103), (2, 10104),
(2, 102), (2, 103), (2, 104),
(2, 105), (2, 10501), (2, 10502), (2, 10503), (2, 10504),
(2, 20),
(2, 201), (2, 20101), (2, 20102), (2, 20103), (2, 20104),
(2, 202), (2, 20201), (2, 20202), (2, 20203), (2, 20204),
(2, 203), (2, 20301), (2, 20302),
(2, 204), (2, 20401), (2, 20402), (2, 20403), (2, 20404),
(2, 30),
(2, 301), (2, 30101), (2, 30102), (2, 30103), (2, 30104),
(2, 303), (2, 30301), (2, 30302),
(2, 40),
(2, 401), (2, 40101), (2, 40102), (2, 40103), (2, 40104);

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 100), (3, 10),
(3, 101), (3, 102), (3, 103), (3, 104), (3, 105),
(3, 20),
(3, 201), (3, 202), (3, 203), (3, 204),
(3, 30),
(3, 301), (3, 303),
(3, 40), (3, 401);
