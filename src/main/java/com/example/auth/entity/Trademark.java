package com.example.auth.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 商标信息
 */
@Data
public class Trademark {
    private Long id;
    private String trademarkName;      // 商标名称
    private String trademarkNo;        // 商标号
    private String category;           // 分类
    private String subCategory;        // 小类名称

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date validDate;            // 有效期

    private String companyName;        // 所属公司

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
