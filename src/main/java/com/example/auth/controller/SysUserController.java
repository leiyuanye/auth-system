package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.SysUser;
import com.example.auth.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/users")
@CrossOrigin
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private OperateLogUtil logUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String MODULE_NAME = "用户管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<SysUser> list = userService.selectByCondition(keyword, status, offset, size);
        int total = userService.countByCondition(keyword, status);
        for (SysUser u : list) {
            u.setPassword(null);
        }
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.ok(user);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody SysUser user, HttpServletRequest request) {
        SysUser exist = userService.selectByUsername(user.getUsername());
        if (exist != null) {
            return Result.fail("用户名已存在");
        }
        String rawPassword = (user.getPassword() == null || user.getPassword().isEmpty())
                ? "admin123" : user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        if (user.getStatus() == null) user.setStatus(1);

        int rows = userService.insert(user);
        SysUser logged = userService.selectById(user.getId());
        if (logged != null) logged.setPassword(null);
        logUtil.logAdd(MODULE_NAME, user.getId(), user.getUsername(), logged, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user, HttpServletRequest request) {
        SysUser oldUser = userService.selectById(id);
        if (oldUser != null) oldUser.setPassword(null);
        user.setId(id);
        user.setPassword(null);
        int rows = userService.update(user);
        SysUser newUser = userService.selectById(id);
        if (newUser != null) newUser.setPassword(null);
        logUtil.logUpdate(MODULE_NAME, id, user.getUsername() != null ? user.getUsername() : (oldUser != null ? oldUser.getUsername() : null), oldUser, newUser, currentUser(request));
        return Result.ok(null);
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = "admin123";
        }
        String bcryptPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(id, bcryptPassword);
        SysUser updated = userService.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id, updated != null ? updated.getUsername() : String.valueOf(id),
                "原密码(已加密)", "新密码(已重置)", currentUser(request),
                "密码重置为 " + newPassword);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        SysUser oldUser = userService.selectById(id);
        if (oldUser != null) oldUser.setPassword(null);
        userService.deleteUserRolesByUserId(id);
        int rows = userService.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldUser != null ? oldUser.getUsername() : String.valueOf(id), oldUser, currentUser(request));
        return Result.ok(null);
    }

    @GetMapping("/{userId}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        List<Long> roleIds = userService.selectRoleIdsByUserId(userId);
        return Result.ok(roleIds);
    }

    @PostMapping("/{userId}/roles")
    public Result<Void> assignRoles(@PathVariable Long userId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        List<Integer> roleIdsInt = (List<Integer>) body.get("roleIds");
        SysUser oldUser = userService.selectById(userId);
        userService.deleteUserRolesByUserId(userId);
        if (roleIdsInt != null) {
            for (Integer rid : roleIdsInt) {
                userService.insertUserRole(userId, rid.longValue());
            }
        }
        logUtil.logUpdate(MODULE_NAME, userId, oldUser != null ? oldUser.getUsername() : String.valueOf(userId),
                "角色变更", roleIdsInt == null ? "空" : roleIdsInt.toString(), currentUser(request),
                "角色分配为: " + roleIdsInt);
        return Result.ok(null);
    }
}
