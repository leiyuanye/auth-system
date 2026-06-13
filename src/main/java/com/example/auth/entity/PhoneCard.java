package com.example.auth.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class PhoneCard {
    private Long id;
    private String cardNumber;
    private String agentName;
    private String phoneNumber;
    private Long realnameId;
    private String realnameName;
    private Integer usageStatus;
    private Integer cardStatus;
    private Integer cardType;
    private Integer operatorType;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
