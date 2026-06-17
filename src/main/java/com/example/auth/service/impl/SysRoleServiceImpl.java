package com.example.auth.service.impl;

import com.example.auth.entity.SysRole;
import com.example.auth.mapper.SysRoleMapper;
import com.example.auth.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public List<SysRole> selectAll() {
        return roleMapper.selectAll();
    }

    @Override
    public SysRole selectById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public SysRole selectByRoleCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }

    @Override
    public List<SysRole> selectByCondition(String keyword, Integer status, int offset, int size) {
        return roleMapper.selectByCondition(keyword, status, offset, size);
    }

    @Override
    public int countByCondition(String keyword, Integer status) {
        return roleMapper.countByCondition(keyword, status);
    }

    @Override
    public int insert(SysRole role) {
        return roleMapper.insert(role);
    }

    @Override
    public int update(SysRole role) {
        return roleMapper.update(role);
    }

    @Override
    public int deleteById(Long id) {
        return roleMapper.deleteById(id);
    }

    @Override
    public void deleteRoleMenusByRoleId(Long roleId) {
        roleMapper.deleteRoleMenusByRoleId(roleId);
    }

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return roleMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public void insertRoleMenu(Long roleId, Long menuId) {
        roleMapper.insertRoleMenu(roleId, menuId);
    }

    @Override
    public List<String> selectRoleCodesByUserId(Long userId) {
        return roleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        return roleMapper.selectRoleIdsByUserId(userId);
    }
}