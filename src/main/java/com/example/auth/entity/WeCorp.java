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
    private String subjectShort;  // 主体简称
    private String subjectFull;   // 企业全称
    private String customerType;  // 客户类型（多选用逗号分隔）
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date certExpire;      // 企业认证到期时间
    private Integer quotaTotal;   // 外部联系人规模额度
    private Integer quotaUsed;    // 已用外部联系人额度
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date contactValidDate;// 外部联系人有效期
    private String creator;       // 主体创建人
    private String phone;         // 手机号码
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    // 计算字段：剩余外部联系人额度
    public Integer getQuotaRemaining() {
        int total = (quotaTotal == null ? 0 : quotaTotal);
        int used = (quotaUsed == null ? 0 : quotaUsed);
        return total - used;
    }
}
