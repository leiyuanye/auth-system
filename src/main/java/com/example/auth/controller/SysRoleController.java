package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.SysRole;
import com.example.auth.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/roles")
@CrossOrigin
public class SysRoleController {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "角色管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.ok(roleMapper.selectAll());
    }

    @GetMapping
    public Result<PageResult<SysRole>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<SysRole> list = roleMapper.selectByCondition(keyword, status, offset, size);
        int total = roleMapper.countByCondition(keyword, status);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleMapper.selectById(id));
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody SysRole role, HttpServletRequest request) {
        SysRole exist = roleMapper.selectByRoleCode(role.getRoleCode());
        if (exist != null) {
            return Result.fail("角色编码已存在");
        }
        if (role.getStatus() == null) role.setStatus(1);
        roleMapper.insert(role);
        logUtil.logAdd(MODULE_NAME, role.getId(), role.getRoleName(), role, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", role.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role, HttpServletRequest request) {
        SysRole oldRole = roleMapper.selectById(id);
        role.setId(id);
        roleMapper.update(role);
        SysRole newRole = roleMapper.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id, role.getRoleName() != null ? role.getRoleName() : (oldRole != null ? oldRole.getRoleName() : null), oldRole, newRole, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        SysRole oldRole = roleMapper.selectById(id);
        roleMapper.deleteRoleMenusByRoleId(id);
        roleMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldRole != null ? oldRole.getRoleName() : String.valueOf(id), oldRole, currentUser(request));
        return Result.ok(null);
    }

    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        List<Long> menuIds = roleMapper.selectMenuIdsByRoleId(roleId);
        return Result.ok(menuIds);
    }

    @PostMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        List<Integer> menuIdsInt = (List<Integer>) body.get("menuIds");
        SysRole role = roleMapper.selectById(roleId);
        String roleName = role != null ? role.getRoleName() : String.valueOf(roleId);
        roleMapper.deleteRoleMenusByRoleId(roleId);
        if (menuIdsInt != null) {
            for (Integer mid : menuIdsInt) {
                roleMapper.insertRoleMenu(roleId, mid.longValue());
            }
        }
        logUtil.logUpdate(MODULE_NAME, roleId, roleName,
                "菜单权限变更", menuIdsInt == null ? "空" : menuIdsInt.toString(),
                currentUser(request), "角色菜单权限重新分配为: " + menuIdsInt);
        return Result.ok(null);
    }
}
