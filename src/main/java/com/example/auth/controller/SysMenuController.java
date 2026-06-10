package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.SysMenu;
import com.example.auth.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/menus")
@CrossOrigin
public class SysMenuController {

    @Autowired
    private SysMenuMapper menuMapper;

    @GetMapping
    public Result<List<SysMenu>> list() {
        List<SysMenu> list = menuMapper.selectAllMenus();
        return Result.ok(list);
    }
}
