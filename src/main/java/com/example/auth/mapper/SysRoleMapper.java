package com.example.auth.mapper;

import com.example.auth.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    // 角色 CRUD
    List<SysRole> selectAll();
    SysRole selectById(@Param("id") Long id);
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);
    int insert(SysRole role);
    int update(SysRole role);
    int deleteById(@Param("id") Long id);

    // 条件查询
    List<SysRole> selectByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    // ============== 角色-菜单关联 ==============
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    int deleteRoleMenusByRoleId(@Param("roleId") Long roleId);
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
