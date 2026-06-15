USE auth_system;

-- ============================================================
-- 手机设备页面初始化数据
-- 用途：
--   1. 补齐企微/微信状态中的"无"，配合新增/编辑完整性校验。
--   2. 初始化手机卡手机号来源，确保手机号下拉有可选数据。
--   3. 生成一条手机设备页面测试数据。
-- 说明：
--   本脚本可重复执行；不会覆盖用户在数据字典中已维护的同 key 数据。
-- ============================================================

INSERT INTO sys_dict (dict_type, dict_key, dict_value, sort_order)
SELECT 'phone_device_wechat_status', '0', '无', 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict WHERE dict_type = 'phone_device_wechat_status' AND dict_key = '0'
);

INSERT INTO sys_dict (dict_type, dict_key, dict_value, sort_order)
SELECT 'phone_device_wx_status', '0', '无', 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict WHERE dict_type = 'phone_device_wx_status' AND dict_key = '0'
);

INSERT INTO phone_realname (real_name, colleague_status, colleague_name, scan_status, remark)
SELECT '测试实名人', 'active', '系统初始化', 2, '手机设备页面测试数据'
WHERE NOT EXISTS (
    SELECT 1 FROM phone_realname WHERE real_name = '测试实名人'
);

INSERT INTO phone_card (
    iccd, agent_name, phone_number, realname_id, realname_name,
    usage_status, card_status, operator_type, remark
)
SELECT
    '89860000000000000001',
    'XX科技有限公司',
    '13900000001',
    (SELECT id FROM phone_realname WHERE real_name = '测试实名人' ORDER BY id LIMIT 1),
    '测试实名人',
    1,
    1,
    1,
    '手机设备页面测试手机号'
WHERE NOT EXISTS (
    SELECT 1 FROM phone_card WHERE phone_number = '13900000001'
);

INSERT INTO phone_device (
    device_code, phone_no, wechat_nickname, entity_name, wechat_person, wechat_phone,
    phone_location, wechat_status, use_status, dept, wechat_usage, wx_status, wx_usage,
    phone_type, wx_realname, wx_phone, wx_password, remark, create_time
)
SELECT
    'TEST001',
    'TEST001',
    '测试企微01',
    '科技A',
    '测试实名人',
    '13900000001',
    '测试机架-001',
    1,
    1,
    1,
    1,
    1,
    1,
    1,
    '',
    '13900000001',
    'test_wx_001',
    '手机设备页面初始化测试数据',
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM phone_device WHERE device_code = 'TEST001'
);
