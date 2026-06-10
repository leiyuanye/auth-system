package com.example.auth.mapper;

import com.example.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper {
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);
    List<SysMenu> selectAllMenus();
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
