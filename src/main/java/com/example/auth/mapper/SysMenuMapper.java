package com.example.auth.mapper;

import com.example.auth.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单 Mapper 接口
 * 对应表: sys_menu
 * 功能: 菜单权限管理，用于构建用户可访问的菜单树
 */
@Mapper
public interface SysMenuMapper {

    /**
     * 根据用户ID查询其可访问的菜单列表（用于构建菜单树）
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询其权限编码列表（用于接口权限校验）
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询所有菜单（不分页，含按钮权限）
     * @return 菜单列表
     */
    List<SysMenu> selectAllMenus();

    /**
     * 根据ID查询菜单详情
     * @param id 菜单ID
     * @return 菜单实体
     */
    SysMenu selectById(@Param("id") Long id);

    /**
     * 根据路径查询菜单（用于唯一性校验）
     * @param path 菜单路径
     * @return 菜单实体
     */
    SysMenu selectByPath(@Param("path") String path);

    /**
     * 新增菜单
     * @param menu 菜单实体
     * @return 影响行数
     */
    int insert(SysMenu menu);

    /**
     * 更新菜单信息
     * @param menu 菜单实体（含ID）
     * @return 影响行数
     */
    int update(SysMenu menu);

    /**
     * 根据ID删除菜单
     * @param id 菜单ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据父菜单ID查询子菜单数量（删除前校验）
     * @param parentId 父菜单ID
     * @return 子菜单数量
     */
    int countByParentId(@Param("parentId") Long parentId);

    /**
     * 条件查询菜单列表（不含按钮权限，只查菜单）
     * @param keyword 关键词（菜单名称/路径模糊匹配）
     * @return 菜单列表
     */
    List<SysMenu> selectByCondition(@Param("keyword") String keyword);
}