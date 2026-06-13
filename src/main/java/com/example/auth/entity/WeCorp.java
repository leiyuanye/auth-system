package com.example.auth.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 企微主体
 */
@Data
public class WeCorp {
    private Long id;
    private String subjectShort;
    private String subjectFull;
    private String customerType;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date certExpire;

    private Integer quotaTotal;
    private Integer quotaUsed;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contactValidDate;

    private String creator;
    private String phone;
    private String corpStatus;       // 主体状态（字典：正常/注销/冻结）

    // 法人信息
    private String legalName;
    private String legalIdCard;
    private String legalPhone;
    private String registerCapital;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date registerDate;

    private String businessScope;
    private String registerAddress;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public Integer getQuotaRemaining() {
        int total = (quotaTotal == null ? 0 : quotaTotal);
        int used = (quotaUsed == null ? 0 : quotaUsed);
        return total - used;
    }
}
