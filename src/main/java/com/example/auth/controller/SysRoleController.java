package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.SysRole;
import com.example.auth.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/roles")
@CrossOrigin
public class SysRoleController {

    @Autowired
    private SysRoleMapper roleMapper;

    @GetMapping
    public Result<List<SysRole>> list() {
        List<SysRole> list = roleMapper.selectAll();
        return Result.ok(list);
    }
}
