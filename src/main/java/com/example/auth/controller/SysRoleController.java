package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.SysRole;
import com.example.auth.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sys/roles")
@CrossOrigin
public class SysRoleController {

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 查询所有角色（不分页，用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.ok(roleMapper.selectAll());
    }

    /**
     * 分页查询角色列表
     */
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
    public Result<Map<String, Object>> add(@RequestBody SysRole role) {
        // 校验 roleCode 是否重复
        SysRole exist = roleMapper.selectByRoleCode(role.getRoleCode());
        if (exist != null) {
            return Result.fail("角色编码已存在");
        }
        if (role.getStatus() == null) role.setStatus(1);
        roleMapper.insert(role);
        Map<String, Object> data = new HashMap<>();
        data.put("id", role.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleMapper.update(role);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 先删除角色-菜单关联
        roleMapper.deleteRoleMenusByRoleId(id);
        roleMapper.deleteById(id);
        return Result.ok(null);
    }

    // ============== 角色-菜单分配 ==============

    /**
     * 获取角色已分配的菜单ID列表
     */
    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        List<Long> menuIds = roleMapper.selectMenuIdsByRoleId(roleId);
        return Result.ok(menuIds);
    }

    /**
     * 给角色分配菜单（权限）
     * body: { menuIds: [1,2,3] }
     */
    @PostMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody Map<String, Object> body) {
        List<Integer> menuIdsInt = (List<Integer>) body.get("menuIds");
        // 先删后插
        roleMapper.deleteRoleMenusByRoleId(roleId);
        if (menuIdsInt != null) {
            for (Integer mid : menuIdsInt) {
                roleMapper.insertRoleMenu(roleId, mid.longValue());
            }
        }
        return Result.ok(null);
    }
}
