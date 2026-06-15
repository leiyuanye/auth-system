-- =============================================================
-- 业务模块数据重置脚本（可重复执行）
-- 清空范围：手机卡 / 实名人 / 代理商 / 手机设备主号 / 子账号 / 归档 /
--           设备备份 / 服务器 / 企微主体 / 商标
-- 不清空：sys_dict / sys_menu / sys_role / sys_role_menu /
--         sys_user / sys_user_role / sys_operate_log
-- 每个模块保留 1 条模拟数据，便于页面展示
-- =============================================================

USE auth_system;

SET FOREIGN_KEY_CHECKS = 0;

-- ===== 手机卡相关 =====
TRUNCATE TABLE phone_card;
TRUNCATE TABLE phone_realname;
TRUNCATE TABLE phone_agent;

-- ===== 手机设备相关 =====
TRUNCATE TABLE phone_sub_account;
TRUNCATE TABLE phone_device_archive;
TRUNCATE TABLE phone_device;
TRUNCATE TABLE phone_device_backup;

-- ===== 服务器 =====
TRUNCATE TABLE sys_server;

-- ===== 企微主体 / 商标 =====
TRUNCATE TABLE we_corp;
TRUNCATE TABLE we_trademark;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- 模拟数据：每个模块保留 1 条
-- =============================================================

-- 实名人
INSERT INTO phone_realname (real_name, colleague_status, colleague_name, scan_status, remark)
VALUES ('演示实名人', 'active', '演示同事', 1, '初始化模拟数据');

-- 代理商（如果该表仍在使用）
INSERT INTO phone_agent (agent_name, contact, phone, address, status, remark)
VALUES ('演示代理商', '张三', '13900000000', '北京市朝阳区演示路1号', 1, '初始化模拟数据');

-- 手机卡
INSERT INTO phone_card (iccd, agent_name, phone_number, realname_name,
                        usage_status, card_status, operator_type, remark)
VALUES ('89860000000000000001', '演示代理商', '13900000001', '演示实名人',
        1, 1, 1, '初始化模拟手机卡');

-- 手机设备主号（摩托罗拉，便于配子号）
INSERT INTO phone_device (
    device_code, phone_no, wechat_nickname, entity_name, wechat_person,
    wechat_phone, phone_location, wechat_status, use_status, dept,
    wechat_usage, wx_status, wx_usage, phone_type,
    wx_realname, wx_phone, wx_password, remark, account_index
) VALUES (
    'MT001', 'MT001', '演示主号', '演示主体', '演示实名人',
    '13900000001', '机房一号架', 1, 1, 1,
    1, 1, 1, 3,
    '演示实名人', '13900000001', 'demo@123', '初始化模拟设备', '主'
);

-- 手机设备子号
INSERT INTO phone_sub_account (
    device_code, account_index, phone_no, wechat_nickname, entity_name,
    wechat_person, wechat_phone, phone_location, wechat_status, use_status,
    dept, wechat_usage, wx_status, wx_usage, phone_type,
    wx_realname, wx_phone, wx_password, remark, create_time, update_time
) VALUES (
    'MT001', '1', 'MT001', '演示子号01', '演示主体',
    '演示实名人', '13900000001', '机房一号架', 1, 1,
    1, 1, 1, 1, 3,
    '演示实名人', '13900000001', 'demo@123', '初始化模拟子号', NOW(), NOW()
);

-- 归档（保留 1 条作废示例，便于统计页面展示）
INSERT INTO phone_device_archive (
    account_type, device_code, account_index, phone_no, wechat_nickname,
    entity_name, wechat_person, wechat_phone, phone_location,
    wechat_status, use_status, dept, wechat_usage, wx_status, wx_usage,
    phone_type, wx_realname, wx_phone, wx_password, remark,
    create_time, update_time
) VALUES (
    'main', 'ARCHIVE001', '主', 'ARCHIVE001', '演示归档号',
    '演示主体', '演示实名人', '13900000002', '机房二号架',
    4, 4, 1, 1, 4, 1,
    3, '演示实名人', '13900000002', 'demo@123', '初始化模拟归档',
    NOW(), NOW()
);

-- 服务器
INSERT INTO sys_server (
    server_name, ip_address, server_type, location, specs, mfa_key,
    server_status, remote_account, remote_pwd, backend_account, backend_pwd,
    remark, expire_time
) VALUES (
    '演示服务器', '192.168.1.10', '应用服务器', '机房一号架', '4C8G',
    'demo-mfa', 1, 'admin', 'demo@123', 'root', 'demo@123',
    '初始化模拟服务器', '2027-01-01'
);

-- 企微主体
INSERT INTO we_corp (
    subject_short, subject_full, customer_type, cert_expire,
    quota_total, quota_used, contact_valid_date, creator, phone,
    corp_status, legal_name, legal_id_card, legal_phone,
    register_capital, register_date, business_scope, register_address, remark
) VALUES (
    '演示主体', '演示主体有限公司', '企业', '2027-01-01',
    100, 10, '2027-01-01', 'admin', '13900000001',
    'active', '演示法人', '110101199001011234', '13900000001',
    '100万', '2020-01-01', '技术服务', '北京市朝阳区演示路1号', '初始化模拟主体'
);

-- 商标
INSERT INTO we_trademark (
    trademark_name, trademark_no, category, sub_category, valid_date,
    company_name, remark
) VALUES (
    '演示商标', 'TM00000001', '第9类', '科学仪器', '2030-01-01',
    '演示主体有限公司', '初始化模拟商标'
);
