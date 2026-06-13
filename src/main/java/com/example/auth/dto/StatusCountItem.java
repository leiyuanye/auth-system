package com.example.auth.dto;

import lombok.Data;

/**
 * 按卡片状态分组统计结果
 */
@Data
public class StatusCountItem {
    private Integer cardStatus;
    private Integer count;
}
