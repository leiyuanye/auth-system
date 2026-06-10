package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.SysUser;
import com.example.auth.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/users")
@CrossOrigin
public class SysUserController {

    @Autowired
    private SysUserMapper userMapper;

    @GetMapping
    public Result<List<SysUser>> list() {
        List<SysUser> list = userMapper.selectAll();
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        return Result.ok(user);
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        return Result.fail("暂未实现");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        return Result.fail("暂未实现");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return Result.fail("暂未实现");
    }
}
