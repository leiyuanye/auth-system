package com.example.auth.mapper;

import com.example.auth.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色 Mapper 接口
 * 对应表: sys_role
 * 功能: 角色管理及角色-菜单关联
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 根据用户ID查询角色编码列表（用于权限判断）
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有角色（不分页）
     * @return 角色列表
     */
    List<SysRole> selectAll();

    /**
     * 根据ID查询角色详情
     * @param id 角色ID
     * @return 角色实体
     */
    SysRole selectById(@Param("id") Long id);

    /**
     * 根据角色编码查询角色（用于唯一性校验）
     * @param roleCode 角色编码
     * @return 角色实体
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 新增角色
     * @param role 角色实体
     * @return 影响行数
     */
    int insert(SysRole role);

    /**
     * 更新角色信息
     * @param role 角色实体（含ID）
     * @return 影响行数
     */
    int update(SysRole role);

    /**
     * 根据ID删除角色
     * @param id 角色ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询角色列表
     * @param keyword 关键词（角色名称/编码模糊匹配）
     * @param status 角色状态（1=正常, 0=禁用）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 角色列表
     */
    List<SysRole> selectByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询角色总数（用于分页计算）
     * @param keyword 关键词
     * @param status 角色状态
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    // ============== 角色-菜单关联 ==============

    /**
     * 查询角色已分配的菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有菜单关联（分配菜单前先清空）
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteRoleMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 给角色分配单个菜单权限
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 影响行数
     */
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}