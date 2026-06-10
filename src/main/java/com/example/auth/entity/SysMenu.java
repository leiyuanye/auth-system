package com.example.auth.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class SysMenu {
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private Integer menuType;
    private String permCode;
    private Integer status;
    private Date createTime;
    private List<SysMenu> children;
}
