package com.example.auth.mapper;

import com.example.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户 Mapper 接口
 * 对应表: sys_user
 * 功能: 用户管理及用户-角色关联
 */
@Mapper
public interface SysUserMapper {

    /**
     * 根据用户名查询用户（用于登录认证）
     * @param username 用户名
     * @return 用户实体
     */
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 查询所有用户（不分页）
     * @return 用户列表
     */
    List<SysUser> selectAll();

    /**
     * 根据ID查询用户详情
     * @param id 用户ID
     * @return 用户实体
     */
    SysUser selectById(@Param("id") Long id);

    /**
     * 新增用户（返回主键 id）
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(SysUser user);

    /**
     * 更新用户信息
     * @param user 用户实体（含ID）
     * @return 影响行数
     */
    int update(SysUser user);

    /**
     * 更新用户密码
     * @param id 用户ID
     * @param password 新密码（加密后）
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询用户列表
     * @param keyword 关键词（用户名/姓名模糊匹配）
     * @param status 用户状态（1=正常, 0=禁用）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 用户列表
     */
    List<SysUser> selectByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询用户总数（用于分页计算）
     * @param keyword 关键词
     * @param status 用户状态
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    // ============== 用户-角色关联 ==============

    /**
     * 删除用户的所有角色关联（分配角色前先清空）
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteUserRolesByUserId(@Param("userId") Long userId);

    /**
     * 给用户分配单个角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 查询用户已分配的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}