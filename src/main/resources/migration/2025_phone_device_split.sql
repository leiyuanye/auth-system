-- ============================================================
-- 数据库重构：将 phone_device 拆分为主设备表 + 子账号表
-- 目标：
--   1. phone_device 成为主设备表（每个物理设备一条记录），device_code 唯一
--   2. phone_sub_account 为子账号表，每个主设备最多 5 个子账号
--   3. 自动迁移现有数据到新结构
-- 运行方式：直接在数据库中执行以下 SQL
-- ============================================================

-- ============================================================
-- 第一步：备份原表（防止数据丢失，可随时回滚）
-- ============================================================
DROP TABLE IF EXISTS phone_device_backup;
CREATE TABLE phone_device_backup AS SELECT * FROM phone_device;

-- ============================================================
-- 第二步：创建子账号表
-- ============================================================
DROP TABLE IF EXISTS phone_sub_account;
CREATE TABLE phone_sub_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    device_code VARCHAR(64) NOT NULL COMMENT '关联主设备的设备编码',
    account_index VARCHAR(16) NOT NULL COMMENT '账号槽位：1/2/3/4/5',
    phone_no VARCHAR(128) DEFAULT NULL COMMENT '完整手机编号，如 MT101-1',
    wechat_nickname VARCHAR(128) DEFAULT NULL COMMENT '企微对外昵称',
    entity_name VARCHAR(256) DEFAULT NULL COMMENT '主体简称（逗号分隔）',
    wechat_person VARCHAR(64) DEFAULT NULL COMMENT '企微实名人',
    wechat_phone VARCHAR(32) DEFAULT NULL COMMENT '企微手机号',
    phone_location VARCHAR(128) DEFAULT NULL COMMENT '手机位置',
    wechat_status INT DEFAULT 1 COMMENT '企微状态',
    use_status INT DEFAULT 1 COMMENT '使用状态',
    dept INT DEFAULT 1 COMMENT '使用部门',
    wechat_usage INT DEFAULT 1 COMMENT '企微用途',
    wx_status INT DEFAULT 1 COMMENT '微信状态',
    wx_usage INT DEFAULT 1 COMMENT '微信用途',
    phone_type INT DEFAULT 1 COMMENT '手机类型',
    wx_realname VARCHAR(64) DEFAULT NULL COMMENT '微信实名人',
    wx_phone VARCHAR(32) DEFAULT NULL COMMENT '微信手机号',
    wx_password VARCHAR(128) DEFAULT NULL COMMENT '微信密码',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT NULL,
    update_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_device_code (device_code),
    KEY idx_account_index (account_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手机设备-子账号表';

-- ============================================================
-- 第三步：重建 phone_device 为主设备表
--   - 删除原 device_code / account_index 字段
--   - 将 device_code 升级为唯一约束
--   - 保留主账号信息（原表中 account_index = '主' 的记录）
--   - 其他记录（account_index != '主'）迁移到 phone_sub_account
-- ============================================================

-- 3.1 迁移主账号记录到 phone_sub_account
--     （account_index != '主' 或 phone_no 格式为 MTxxx-数字 的都视为子账号）
INSERT INTO phone_sub_account (
    device_code, account_index, phone_no, wechat_nickname, entity_name,
    wechat_person, wechat_phone, phone_location, wechat_status, use_status,
    dept, wechat_usage, wx_status, wx_usage, phone_type, wx_realname,
    wx_phone, wx_password, remark, create_time
)
SELECT
    CASE
        WHEN pd.device_code IS NOT NULL AND pd.device_code != '' THEN pd.device_code
        WHEN pd.phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+-[0-9]{1,2}$'
          THEN SUBSTRING_INDEX(pd.phone_no, '-', 1)
        WHEN pd.phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+主$'
          THEN SUBSTRING(pd.phone_no, 1, CHAR_LENGTH(pd.phone_no) - 1)
        ELSE pd.phone_no
    END AS device_code,
    CASE
        WHEN pd.account_index IS NOT NULL AND pd.account_index != '' THEN pd.account_index
        WHEN pd.phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+-[0-9]{1,2}$'
          THEN SUBSTRING_INDEX(pd.phone_no, '-', -1)
        ELSE '主'
    END AS account_index,
    pd.phone_no,
    pd.wechat_nickname,
    pd.entity_name,
    pd.wechat_person,
    pd.wechat_phone,
    pd.phone_location,
    pd.wechat_status,
    pd.use_status,
    pd.dept,
    pd.wechat_usage,
    pd.wx_status,
    pd.wx_usage,
    pd.phone_type,
    pd.wx_realname,
    pd.wx_phone,
    pd.wx_password,
    pd.remark,
    pd.create_time
FROM phone_device pd
WHERE
    (pd.account_index IS NOT NULL AND pd.account_index != '' AND pd.account_index != '主')
    OR (pd.account_index IS NULL AND pd.phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+-[0-9]{1,2}$')
ORDER BY pd.id;

-- 3.2 从 phone_device 中删除已迁移的子账号记录
DELETE FROM phone_device
WHERE
    (account_index IS NOT NULL AND account_index != '' AND account_index != '主')
    OR (account_index IS NULL AND phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+-[0-9]{1,2}$');

-- 3.3 更新剩余的主账号记录：确保 device_code 有值，account_index = '主'
UPDATE phone_device SET account_index = '主' WHERE account_index IS NULL OR account_index = '';
UPDATE phone_device
SET device_code = CASE
    WHEN phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+主$'
      THEN SUBSTRING(phone_no, 1, CHAR_LENGTH(phone_no) - 1)
    ELSE COALESCE(device_code, phone_no)
END
WHERE device_code IS NULL OR device_code = '';

-- 3.4 删除不需要的字段（account_index 现在冗余了，主号都是 '主'）
--     保留 account_index 列作为标识，避免影响现有代码
ALTER TABLE phone_device
MODIFY COLUMN device_code VARCHAR(64) NOT NULL COMMENT '物理设备编码（唯一）',
ADD UNIQUE KEY uk_device_code (device_code);

-- ============================================================
-- 第四步：校验与清理
-- ============================================================
-- 4.1 查看主设备表
SELECT '主设备表数量' AS info, COUNT(*) AS count FROM phone_device
UNION ALL
SELECT '子账号表数量', COUNT(*) FROM phone_sub_account
UNION ALL
SELECT '原备份表数量', COUNT(*) FROM phone_device_backup;

-- 4.2 确保每个主设备的子账号不超过 5 个
SELECT device_code, COUNT(*) AS sub_count
FROM phone_sub_account
GROUP BY device_code
HAVING COUNT(*) > 5;
