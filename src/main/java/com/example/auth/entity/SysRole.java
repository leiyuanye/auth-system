package com.example.auth.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SysRole {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer status;
    private Date createTime;
}
