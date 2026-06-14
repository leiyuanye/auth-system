-- ============================================================
-- 创建归档表 phone_device_archive
-- 当企微状态 & 微信状态都为"作废"时，数据自动从原表归档到这里
-- 仅用于统计，不可被其他模块引用
-- ============================================================
DROP TABLE IF EXISTS phone_device_archive;
CREATE TABLE phone_device_archive (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_type VARCHAR(16) NOT NULL COMMENT '账号类型：main-主号；sub-子号',
    device_code VARCHAR(64) NOT NULL COMMENT '设备编码',
    account_index VARCHAR(16) DEFAULT NULL COMMENT '槽位（子号时必填）',
    phone_no VARCHAR(128) DEFAULT NULL COMMENT '完整编号',
    wechat_nickname VARCHAR(128) DEFAULT NULL COMMENT '企微对外昵称',
    entity_name VARCHAR(256) DEFAULT NULL COMMENT '主体简称',
    wechat_person VARCHAR(64) DEFAULT NULL COMMENT '企微实名人',
    wechat_phone VARCHAR(32) DEFAULT NULL COMMENT '企微手机号',
    phone_location VARCHAR(128) DEFAULT NULL COMMENT '手机位置',
    wechat_status INT DEFAULT NULL COMMENT '企微状态',
    use_status INT DEFAULT NULL COMMENT '使用状态',
    dept INT DEFAULT NULL COMMENT '使用部门',
    wechat_usage INT DEFAULT NULL COMMENT '企微用途',
    wx_status INT DEFAULT NULL COMMENT '微信状态',
    wx_usage INT DEFAULT NULL COMMENT '微信用途',
    phone_type INT DEFAULT NULL COMMENT '手机类型',
    wx_realname VARCHAR(64) DEFAULT NULL COMMENT '微信实名人',
    wx_phone VARCHAR(32) DEFAULT NULL COMMENT '微信手机号',
    wx_password VARCHAR(128) DEFAULT NULL COMMENT '微信密码',
    remark VARCHAR(512) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT NULL COMMENT '原创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '原最后更新时间',
    archive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    PRIMARY KEY (id),
    KEY idx_device_code (device_code),
    KEY idx_account_type (account_type),
    KEY idx_archive_time (archive_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手机设备-作废账号归档表（仅用于统计，不可在别处引用）';
