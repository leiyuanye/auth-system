package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.Result;
import com.example.auth.entity.SysMenu;
import com.example.auth.mapper.SysMenuMapper;
import com.example.auth.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/sys/menus")
@CrossOrigin
public class SysMenuController {

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "菜单管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<List<SysMenu>> list(@RequestParam(required = false) String keyword) {
        List<SysMenu> list = menuMapper.selectByCondition(keyword);
        return Result.ok(list);
    }

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
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        SysMenu menu = new SysMenu();
        menu.setName((String) body.get("name"));
        menu.setPath((String) body.get("path"));
        menu.setIcon((String) body.get("icon"));
        Object parentId = body.get("parentId");
        menu.setParentId(parentId == null ? 0L : ((Number) parentId).longValue());
        Object sortOrder = body.get("sortOrder");
        menu.setSortOrder(sortOrder == null ? 0 : ((Number) sortOrder).intValue());
        Object menuType = body.get("menuType");
        menu.setMenuType(menuType == null ? 1 : ((Number) menuType).intValue());
        menu.setPermCode((String) body.get("permCode"));
        Object status = body.get("status");
        menu.setStatus(status == null ? 1 : ((Number) status).intValue());

        menuMapper.insert(menu);

        Long userId = (Long) request.getAttribute("userId");
        if (userId != null) {
            List<Long> roleIds = roleMapper.selectRoleIdsByUserId(userId);
            for (Long roleId : roleIds) {
                roleMapper.insertRoleMenu(roleId, menu.getId());
            }
        }

        logUtil.logAdd(MODULE_NAME, menu.getId(), menu.getName(), menu, currentUser(request));

        Map<String, Object> data = new HashMap<>();
        data.put("id", menu.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMenu menu, HttpServletRequest request) {
        SysMenu oldMenu = menuMapper.selectById(id);
        menu.setId(id);
        menuMapper.update(menu);
        SysMenu newMenu = menuMapper.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id, menu.getName() != null ? menu.getName() : (oldMenu != null ? oldMenu.getName() : null), oldMenu, newMenu, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        int childCount = menuMapper.countByParentId(id);
        if (childCount > 0) {
            return Result.fail("请先删除子菜单");
        }
        SysMenu oldMenu = menuMapper.selectById(id);
        menuMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldMenu != null ? oldMenu.getName() : String.valueOf(id), oldMenu, currentUser(request));
        return Result.ok(null);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            Long pid = menu.getParentId() == null ? 0L : menu.getParentId();
            if (pid.equals(parentId)) {
                menu.setChildren(buildTree(menus, menu.getId()));
                result.add(menu);
            }
        }
        result.sort((a, b) -> {
            int sa = a.getSortOrder() == null ? 0 : a.getSortOrder();
            int sb = b.getSortOrder() == null ? 0 : b.getSortOrder();
            return Integer.compare(sa, sb);
        });
        return result;
    }
}
