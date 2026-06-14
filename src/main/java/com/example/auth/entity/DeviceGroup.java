package com.example.auth.entity;

import lombok.Data;
import java.util.List;
import java.util.Date;

/**
 * 设备分组视图对象
 * 用于列表接口返回：一个主设备 + 其所有子账号
 * 前端根据此结构直接渲染分组表格
 */
@Data
public class DeviceGroup {
    // ===== 主设备信息（phone_device）=====
    private Long id;
    private String deviceCode;
    private String phoneNo;

    // ===== 账号信息（主账号）=====
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
    private Date createTime;
    private Date updateTime;

    // 标记：标识该账号类型为 "主"
    private String accountIndex = "主";
    // 是否为主号（前端区分用）
    private Integer isMain = 1;

    // ===== 子账号列表（phone_sub_account）=====
    private List<PhoneSubAccount> subAccounts;
}
