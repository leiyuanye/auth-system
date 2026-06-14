-- ============================================================
-- phone_device 表新增字段 device_code / account_index
-- 用途：区分同一台物理手机（如摩托罗拉 MT101）上的多个账号
-- 运行方式：在数据库客户端执行以下 SQL 即可
-- ============================================================

-- 1. 新增字段（若列不存在时才添加，MySQL 版本不同写法不同，这里做通用保护）
ALTER TABLE phone_device ADD COLUMN device_code VARCHAR(64) DEFAULT NULL COMMENT '物理设备编码，如 MT101';
ALTER TABLE phone_device ADD COLUMN account_index VARCHAR(16) DEFAULT NULL COMMENT '账号槽位，如 主/1/2/3/4/5/6';

-- 2. 在索引上做优化（如果数据量大，可按需开启）
-- ALTER TABLE phone_device ADD INDEX idx_device_code (device_code);

-- ============================================================
-- 3. 为已有数据填充 device_code 和 account_index
--    规则：
--      MT101主    → device_code="MT101", account_index="主"
--      MT101-1    → device_code="MT101", account_index="1"
--      MT101-10   → device_code="MT101", account_index="10"
--      P2024-001  → device_code="P2024-001", account_index="主"
--      XYZ        → device_code="XYZ", account_index="主"
-- ============================================================

-- 3.1 处理 MT<数字>主 的情况（末尾是"主"字，前面至少2位字母+数字）
UPDATE phone_device
SET device_code = SUBSTRING(phone_no, 1, CHAR_LENGTH(phone_no) - 1),
    account_index = '主'
WHERE phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+主$';

-- 3.2 处理 MT<数字>-<1~2位数字> 的情况
UPDATE phone_device
SET device_code = SUBSTRING_INDEX(phone_no, '-', 1),
    account_index = SUBSTRING_INDEX(phone_no, '-', -1)
WHERE phone_no REGEXP BINARY '^[A-Za-z]{2,}[0-9]+-[0-9]{1,2}$';

-- 3.3 其他所有未处理的数据，把完整 phone_no 当作设备编码，槽位统一为"主"
UPDATE phone_device
SET device_code = phone_no,
    account_index = '主'
WHERE device_code IS NULL OR device_code = '';

-- ============================================================
-- 4. 验证：查看各类 phone_no 的处理结果
-- ============================================================
-- SELECT phone_no, device_code, account_index FROM phone_device ORDER BY device_code, (account_index='主') DESC, CAST(account_index AS UNSIGNED);
