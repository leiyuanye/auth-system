package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PhoneCard {
    private Long id;
    private String cardNumber;
    private Long agentId;
    private String agentName;
    private String phoneNumber;
    private Long realnameId;
    private String realnameName;
    private String department;
    private String package_;
    private Integer cardStatus;
    private Integer cardType;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
