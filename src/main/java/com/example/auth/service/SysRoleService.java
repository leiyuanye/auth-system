package com.example.auth.service;

import com.example.auth.entity.SysRole;

import java.util.List;

public interface SysRoleService {
    List<SysRole> selectAll();
    SysRole selectById(Long id);
    SysRole selectByRoleCode(String roleCode);
    List<SysRole> selectByCondition(String keyword, Integer status, int offset, int size);
    int countByCondition(String keyword, Integer status);
    int insert(SysRole role);
    int update(SysRole role);
    int deleteById(Long id);
    void deleteRoleMenusByRoleId(Long roleId);
    List<Long> selectMenuIdsByRoleId(Long roleId);
    void insertRoleMenu(Long roleId, Long menuId);
    List<String> selectRoleCodesByUserId(Long userId);
    List<Long> selectRoleIdsByUserId(Long userId);
}