package com.example.auth.mapper;

import com.example.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {
    SysUser selectByUsername(@Param("username") String username);
    List<SysUser> selectAll();
    SysUser selectById(@Param("id") Long id);
}
