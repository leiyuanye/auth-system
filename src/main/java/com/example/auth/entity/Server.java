package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

/**
 * 服务器实体
 * 承载 在用服务器 / 备用服务器 统一管理
 */
@Data
public class Server {
    private Long id;
    private String serverName;    // 服务器名称
    private String ipAddress;     // IP地址
    private String serverType;    // 类型: 物理服务器/云服务器/虚拟服务器
    private String location;      // 所在机房
    private String specs;         // 配置 (如 16核32G/500G SSD)
    private Integer serverStatus; // 状态（在用服务器用）: 1=运行中 2=维护中 3=已下线
    private String stockStatus;   // 库存状态（备用服务器用）: 1=库存 2=已借出 3=报废
    private Integer cardType;     // 冗余: 1=在用 2=备用（用于区分两类列表查询）
    private String remark;
    private Date createTime;
    private Date updateTime;
}
