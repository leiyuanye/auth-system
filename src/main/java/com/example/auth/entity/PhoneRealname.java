package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

@Data
public class PhoneRealname {
    private Long id;
    private String realName;
    private String colleagueStatus;
    private String colleagueName;
    private Integer scanStatus;
    private String remark;
    private Date createTime;
}
