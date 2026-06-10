package com.example.auth.mapper;

import com.example.auth.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    List<SysRole> selectAll();
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
