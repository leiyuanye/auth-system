package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.SysUser;
import com.example.auth.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/users")
@CrossOrigin
public class SysUserController {

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 分页查询用户列表
     * GET /api/sys/users?keyword=&status=&page=1&size=10
     */
    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<SysUser> list = userMapper.selectByCondition(keyword, status, offset, size);
        int total = userMapper.countByCondition(keyword, status);
        // 清掉密码字段
        for (SysUser u : list) {
            u.setPassword(null);
        }
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    /**
     * 查询单个用户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.ok(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody SysUser user) {
        // 校验用户名是否存在
        SysUser exist = userMapper.selectByUsername(user.getUsername());
        if (exist != null) {
            return Result.fail("用户名已存在");
        }
        // 默认密码：若密码为空则使用 admin123
        String rawPassword = (user.getPassword() == null || user.getPassword().isEmpty())
                ? "admin123" : user.getPassword();
        user.setPassword(DigestUtils.md5DigestAsHex(rawPassword.getBytes(StandardCharsets.UTF_8)));
        if (user.getStatus() == null) user.setStatus(1);

        int rows = userMapper.insert(user);
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        return Result.ok(data);
    }

    /**
     * 更新用户（不含密码）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        // 清掉密码（防止误用此接口改密码）
        user.setPassword(null);
        int rows = userMapper.update(user);
        return Result.ok(null);
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = "admin123";
        }
        String md5Password = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));
        userMapper.updatePassword(id, md5Password);
        return Result.ok(null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 先删除用户-角色关联，再删除用户
        userMapper.deleteUserRolesByUserId(id);
        int rows = userMapper.deleteById(id);
        return Result.ok(null);
    }

    // ============== 用户-角色分配 ==============

    /**
     * 获取用户已分配的角色ID列表
     */
    @GetMapping("/{userId}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        List<Long> roleIds = userMapper.selectRoleIdsByUserId(userId);
        return Result.ok(roleIds);
    }

    /**
     * 给用户分配角色
     * body: { roleIds: [1,2,3] }
     */
    @PostMapping("/{userId}/roles")
    public Result<Void> assignRoles(@PathVariable Long userId, @RequestBody Map<String, Object> body) {
        List<Integer> roleIdsInt = (List<Integer>) body.get("roleIds");
        // 先删后插
        userMapper.deleteUserRolesByUserId(userId);
        if (roleIdsInt != null) {
            for (Integer rid : roleIdsInt) {
                userMapper.insertUserRole(userId, rid.longValue());
            }
        }
        return Result.ok(null);
    }
}
