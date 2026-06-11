package com.example.auth.mapper;

import com.example.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper {
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    // 菜单 CRUD
    List<SysMenu> selectAllMenus();
    SysMenu selectById(@Param("id") Long id);
    SysMenu selectByPath(@Param("path") String path);
    int insert(SysMenu menu);
    int update(SysMenu menu);
    int deleteById(@Param("id") Long id);

    // 根据 parentId 查询子菜单数量
    int countByParentId(@Param("parentId") Long parentId);

    // 条件查询（不含按钮权限，只查菜单）
    List<SysMenu> selectByCondition(@Param("keyword") String keyword);
}
