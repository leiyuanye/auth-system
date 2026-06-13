package com.example.auth.dto;

import lombok.Data;

/**
 * 实名人 × 运营商交叉统计明细
 */
@Data
public class RealnameDetailItem {
    private String realnameName;
    private Integer totalCount;
    private Integer mobileCount;
    private Integer unicomCount;
    private Integer telecomCount;
    private Integer otherCount;
}
