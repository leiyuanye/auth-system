package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.SysMenu;
import com.example.auth.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sys/menus")
@CrossOrigin
public class SysMenuController {

    @Autowired
    private SysMenuMapper menuMapper;

    /**
     * 查询所有菜单（带条件筛选，返回平铺列表）
     */
    @GetMapping
    public Result<List<SysMenu>> list(@RequestParam(required = false) String keyword) {
        List<SysMenu> list = menuMapper.selectByCondition(keyword);
        return Result.ok(list);
    }

    /**
     * 获取菜单树（用于左侧导航）
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        List<SysMenu> all = menuMapper.selectAllMenus();
        List<SysMenu> tree = buildTree(all, 0L);
        return Result.ok(tree);
    }

    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.ok(menuMapper.selectById(id));
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody SysMenu menu) {
        if (menu.getParentId() == null) menu.setParentId(0L);
        if (menu.getSortOrder() == null) menu.setSortOrder(0);
        if (menu.getStatus() == null) menu.setStatus(1);
        menuMapper.insert(menu);
        Map<String, Object> data = new HashMap<>();
        data.put("id", menu.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        menuMapper.update(menu);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 检查是否有子菜单
        int childCount = menuMapper.countByParentId(id);
        if (childCount > 0) {
            return Result.fail("请先删除子菜单");
        }
        menuMapper.deleteById(id);
        return Result.ok(null);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            Long pid = menu.getParentId() == null ? 0L : menu.getParentId();
            if (pid.equals(parentId)) {
                menu.setChildren(buildTree(menus, menu.getId()));
                result.add(menu);
            }
        }
        // 按 sortOrder 排序
        result.sort((a, b) -> {
            int sa = a.getSortOrder() == null ? 0 : a.getSortOrder();
            int sb = b.getSortOrder() == null ? 0 : b.getSortOrder();
            return Integer.compare(sa, sb);
        });
        return result;
    }
}
