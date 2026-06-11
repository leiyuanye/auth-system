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

    // 新增用户（返回主键 id）
    int insert(SysUser user);

    // 更新用户
    int update(SysUser user);

    // 更新密码
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // 删除用户
    int deleteById(@Param("id") Long id);

    // 条件查询（带分页）
    List<SysUser> selectByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    // 条件查询统计总数
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    // ============== 用户-角色关联 ==============
    // 给用户分配角色（先删除后插入）
    int deleteUserRolesByUserId(@Param("userId") Long userId);
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    // 查询用户已分配的角色ID
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}
