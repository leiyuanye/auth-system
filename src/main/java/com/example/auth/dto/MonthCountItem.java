package com.example.auth.dto;

import lombok.Data;

/**
 * 月度异常统计结果
 */
@Data
public class MonthCountItem {
    private String monthLabel;
    private Integer count;
}
