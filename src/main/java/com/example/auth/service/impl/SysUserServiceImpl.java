package com.example.auth.service.impl;

import com.example.auth.entity.SysUser;
import com.example.auth.mapper.SysUserMapper;
import com.example.auth.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public SysUser selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public SysUser selectById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<SysUser> selectByCondition(String keyword, Integer status, int offset, int size) {
        return userMapper.selectByCondition(keyword, status, offset, size);
    }

    @Override
    public int countByCondition(String keyword, Integer status) {
        return userMapper.countByCondition(keyword, status);
    }

    @Override
    public int insert(SysUser user) {
        return userMapper.insert(user);
    }

    @Override
    public int update(SysUser user) {
        return userMapper.update(user);
    }

    @Override
    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

    @Override
    public int updatePassword(Long id, String md5Password) {
        return userMapper.updatePassword(id, md5Password);
    }

    @Override
    public void deleteUserRolesByUserId(Long userId) {
        userMapper.deleteUserRolesByUserId(userId);
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        return userMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public void insertUserRole(Long userId, Long roleId) {
        userMapper.insertUserRole(userId, roleId);
    }
}