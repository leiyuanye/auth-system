package com.example.auth.entity;

import lombok.Data;

/**
 * 数据字典实体
 * 用于服务器模块的动态下拉选项配置
 */
@Data
public class Dict {
    private Long id;
    private String dictType;    // 字典类型: server_type / server_group / server_status / stock_status
    private String dictKey;     // 字典键(存DB的值)
    private String dictValue;   // 字典值(显示文本)
    private Integer sortOrder;   // 排序
}
