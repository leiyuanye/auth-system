package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

/**
 * 手机设备-作废账号归档表
 * 当一条记录（主号或子号）的企微状态和微信状态都为"作废"时，
 * 自动归档到本类对应的表，不再被其他模块引用，仅用于统计作废总数
 */
@Data
public class PhoneDeviceArchive {
    private Long id;
    private String accountType;     // 账号类型：main-主号；sub-子号
    private String deviceCode;      // 设备编码
    private String accountIndex;   // 槽位（子号时填）
    private String phoneNo;         // 完整编号
    private String wechatNickname;
    private String entityName;
    private String wechatPerson;
    private String wechatPhone;
    private String phoneLocation;
    private Integer wechatStatus;
    private Integer useStatus;
    private Integer dept;
    private Integer wechatUsage;
    private Integer wxStatus;
    private Integer wxUsage;
    private Integer phoneType;
    private String wxRealname;
    private String wxPhone;
    private String wxPassword;
    private String remark;
    private Date createTime;        // 原记录创建时间
    private Date updateTime;      // 原记录最后更新时间
    private Date archiveTime;    // 归档时间
}
