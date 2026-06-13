package com.example.auth.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 服务器实体
 * 在用/备用统一管理，按 server_status 区分状态
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
    private Integer serverStatus; // 状态: 1=运行中 2=维护中 3=已下线 4=到期
    private String remoteAccount; // 远程账号
    private String remotePwd;     // 远程密码
    private String backendAccount;// 后台账号
    private String backendPwd;    // 后台密码
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;      // 到期时间
}
