package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.SysOperateLog;
import com.example.auth.mapper.SysOperateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/logs")
@CrossOrigin
public class SysOperateLogController {

    @Autowired
    private SysOperateLogMapper logMapper;

    @GetMapping
    public Result<PageResult<SysOperateLog>> list(
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String operateType,
            @RequestParam(required = false) String operator,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<SysOperateLog> list = logMapper.selectByCondition(moduleName, operateType, operator, offset, size);
        int total = logMapper.countByCondition(moduleName, operateType, operator);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @PostMapping
    public Result<SysOperateLog> create(@RequestBody SysOperateLog log) {
        if (log.getModuleName() == null || log.getModuleName().trim().isEmpty()) {
            return Result.fail("模块名不能为空");
        }
        if (log.getOperateType() == null || log.getOperateType().trim().isEmpty()) {
            return Result.fail("操作类型不能为空");
        }
        logMapper.insert(log);
        return Result.ok(log);
    }
}
