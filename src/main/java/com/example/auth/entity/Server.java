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
    private String serverType;    // 类型: 腾讯云/阿里云/华为云/物理服务器/其他
    private String location;      // 所在地区: 广州/杭州/北京/上海/成都 等
    private String specs;         // 所在分组: 数据库组/应用组/缓存组/备份组 等
    private String mfaKey;        // MFA密钥
    private Integer serverStatus; // 运行状态（在用服务器用）: 1=运行中 2=维护中 3=已下线
    private String stockStatus;   // 库存状态（备用服务器用）: 库存/已借出/报废
    private Integer cardType;     // 类型标识: 1=在用 2=备用
    private String remark;
    private Date createTime;
    private Date updateTime;      // 最近修改时间
}
