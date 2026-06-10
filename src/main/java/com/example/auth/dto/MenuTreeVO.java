package com.example.auth.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuTreeVO {
    private Long id;
    private String menuName;
    private String menuPath;
    private String menuIcon;
    private Long parentId;
    private Integer sortOrder;
    private Integer menuType;
    private String permCode;
    private Integer status;
    private List<MenuTreeVO> children;
}
