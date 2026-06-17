package com.example.auth.service;

import com.example.auth.entity.SysUser;

import java.util.List;

public interface SysUserService {
    SysUser selectByUsername(String username);
    SysUser selectById(Long id);
    List<SysUser> selectByCondition(String keyword, Integer status, int offset, int size);
    int countByCondition(String keyword, Integer status);
    int insert(SysUser user);
    int update(SysUser user);
    int deleteById(Long id);
    int updatePassword(Long id, String md5Password);
    void deleteUserRolesByUserId(Long userId);
    List<Long> selectRoleIdsByUserId(Long userId);
    void insertUserRole(Long userId, Long roleId);
}