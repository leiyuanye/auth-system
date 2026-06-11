package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PhoneAgent {
    private Long id;
    private String agentName;
    private String contact;
    private String phone;
    private String address;
    private Integer status;
    private String remark;
    private Date createTime;
}
