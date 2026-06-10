package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.SysMenu;
import com.example.auth.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/menus")
    public Result<List<SysMenu>> getMenus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SysMenu> menus = menuService.getUserMenuTree(userId);
        return Result.ok(menus);
    }

    @GetMapping("/permissions")
    public Result<List<String>> getPermissions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<String> permissions = menuService.getUserPermissions(userId);
        return Result.ok(permissions);
    }
}
