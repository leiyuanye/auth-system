package com.example.auth.dto;

import lombok.Data;

/**
 * 按运营商分组统计已实名卡片数量
 */
@Data
public class OperatorCountItem {
    private Integer operatorType;
    private String operatorLabel;
    private Integer count;
}
