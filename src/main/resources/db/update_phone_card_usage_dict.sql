USE auth_system;

-- 手机卡字段调整：
-- 1. 原 department 字段不再用于手机卡，新增 usage_status 表示使用状态。
-- 2. usage_status: 1=使用中，2=库存，用于区分原在用/备用手机卡。
-- 3. card_type / usage_status / card_status 均改为由 sys_dict 维护。

ALTER TABLE phone_card
  ADD COLUMN usage_status TINYINT DEFAULT 1 COMMENT '使用状态：1=使用中，2=库存' AFTER realname_name;

-- 旧数据中 card_type=1 表示在用，card_type=2 表示备用；迁移到 usage_status。
UPDATE phone_card
SET usage_status = CASE WHEN card_type = 2 THEN 2 ELSE 1 END;

-- card_type 不再承担在用/备用含义，旧数据统一设为普通卡。
UPDATE phone_card
SET card_type = 1
WHERE card_type IS NULL OR card_type IN (1, 2);

-- 如确认不再需要旧部门字段，可执行下面语句删除。
-- ALTER TABLE phone_card DROP COLUMN department;

DELETE FROM sys_dict
WHERE dict_type IN ('phone_card_type', 'phone_usage_status', 'phone_card_status');

INSERT INTO sys_dict (dict_type, dict_key, dict_value, sort_order) VALUES
('phone_card_type', '1', '普通卡', 1),
('phone_card_type', '2', '物联卡', 2),
('phone_usage_status', '1', '使用中', 1),
('phone_usage_status', '2', '库存', 2),
('phone_card_status', '1', '正常', 1),
('phone_card_status', '2', '二次实名', 2),
('phone_card_status', '3', '欠费', 3);
