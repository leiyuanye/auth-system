package com.example.auth.service.impl;

import com.example.auth.entity.SysMenu;
import com.example.auth.mapper.SysMenuMapper;
import com.example.auth.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> getUserMenuTree(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return buildTree(menus, 0L);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return menuMapper.selectPermCodesByUserId(userId);
    }

    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        Map<Long, List<SysMenu>> childrenMap = new HashMap<>();

        for (SysMenu menu : menus) {
            Long pid = menu.getParentId() == null ? 0L : menu.getParentId();
            childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(menu);
        }

        List<SysMenu> roots = childrenMap.get(parentId);
        if (roots != null) {
            for (SysMenu root : roots) {
                root.setChildren(buildTreeChildren(root.getId(), childrenMap));
                tree.add(root);
            }
        }
        return tree;
    }

    private List<SysMenu> buildTreeChildren(Long parentId, Map<Long, List<SysMenu>> childrenMap) {
        List<SysMenu> children = childrenMap.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        for (SysMenu child : children) {
            child.setChildren(buildTreeChildren(child.getId(), childrenMap));
        }
        return children;
    }
}
