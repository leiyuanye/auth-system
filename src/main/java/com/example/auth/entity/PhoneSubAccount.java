package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

/**
 * 手机设备子账号表
 * 每个主设备最多关联 5 个子账号
 * 通过 device_code 关联主设备表 phone_device
 */
@Data
public class PhoneSubAccount {
    private Long id;
    private String deviceCode;       // 关联主设备的 device_code（必填）
    private String accountIndex;     // 账号槽位（必填）：1/2/3/4/5
    private String phoneNo;          // 完整手机编号，如 "MT101-1"
    private String wechatNickname;   // 企微对外昵称
    private String entityName;       // 主体简称（逗号分隔）
    private String wechatPerson;     // 企微实名人
    private String wechatPhone;      // 企微手机号
    private String phoneLocation;    // 手机位置
    private Integer wechatStatus;    // 企微状态
    private Integer useStatus;       // 使用状态
    private Integer dept;            // 使用部门
    private Integer wechatUsage;     // 企微用途
    private Integer wxStatus;        // 微信状态
    private Integer wxUsage;         // 微信用途
    private Integer phoneType;       // 手机类型
    private String wxRealname;       // 微信实名人
    private String wxPhone;          // 微信手机号
    private String wxPassword;       // 微信密码
    private String remark;
    private Date createTime;
    private Date updateTime;
}
