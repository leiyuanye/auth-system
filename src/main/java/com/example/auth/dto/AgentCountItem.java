package com.example.auth.dto;

import lombok.Data;

/**
 * 按代理商分组统计结果
 */
@Data
public class AgentCountItem {
    private String agentName;
    private Integer count;
}
