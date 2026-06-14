package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PhoneDevice {
    private Long id;
    private String phoneNo;          // 手机编号（完整，如 MT101主 / MT101-1 / P2024-001）
    private String deviceCode;       // 物理设备编码（如 MT101 / P2024-001）
    private String accountIndex;     // 账号槽位（主 / 1 / 2 / 3 / 4 / 5 / 6）
    private String wechatNickname;   // 企微对外昵称
    private String entityName;       // 主体简称（多选逗号分隔）
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

    /**
     * 从 phoneNo 解析出 deviceCode 和 accountIndex。
     *   MT101主    → deviceCode="MT101", accountIndex="主"
     *   MT101-1    → deviceCode="MT101", accountIndex="1"
     *   MT101-10   → deviceCode="MT101", accountIndex="10"
     *   P2024-001  → deviceCode="P2024-001", accountIndex="主"
     *   XYZ        → deviceCode="XYZ", accountIndex="主"
     */
    public void parseFromPhoneNo() {
        if (phoneNo == null || phoneNo.isEmpty()) {
            if (deviceCode == null) deviceCode = "";
            if (accountIndex == null) accountIndex = "主";
            return;
        }
        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile("^([A-Za-z]{2,}\\d+)(主)$");
        java.util.regex.Matcher m1 = p1.matcher(phoneNo);
        if (m1.find()) {
            this.deviceCode = m1.group(1);
            this.accountIndex = m1.group(2);
            return;
        }
        java.util.regex.Pattern p2 = java.util.regex.Pattern.compile("^([A-Za-z]{2,}\\d+)-(\\d{1,2})$");
        java.util.regex.Matcher m2 = p2.matcher(phoneNo);
        if (m2.find()) {
            this.deviceCode = m2.group(1);
            this.accountIndex = m2.group(2);
            return;
        }
        this.deviceCode = phoneNo;
        this.accountIndex = "主";
    }
}
