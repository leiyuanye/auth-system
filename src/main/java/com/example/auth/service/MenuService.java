package com.example.auth.service;

import com.example.auth.entity.SysMenu;

import java.util.List;

public interface MenuService {
    List<SysMenu> getUserMenuTree(Long userId);
    List<String> getUserPermissions(Long userId);
}
