package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.SysOperateLog;
import com.example.auth.mapper.SysOperateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/logs")
@CrossOrigin
public class SysOperateLogController {

    @Autowired
    private SysOperateLogMapper logMapper;

    /**
     * 将前端逗号分隔的多值参数（如 "手机卡管理,用户管理"）转为列表
     */
    private List<String> splitMulti(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    @GetMapping
    public Result<PageResult<SysOperateLog>> list(
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) String operateType,
            @RequestParam(required = false) String operator,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<String> moduleNames = splitMulti(moduleName);
        List<String> operateTypes = splitMulti(operateType);

        int offset = Math.max(0, (page - 1) * size);
        List<SysOperateLog> list = logMapper.selectByCondition(
                moduleNames != null && !moduleNames.isEmpty() ? moduleNames : Collections.emptyList(),
                moduleNames != null && !moduleNames.isEmpty() ? 1 : 0,
                operateTypes != null && !operateTypes.isEmpty() ? operateTypes : Collections.emptyList(),
                operateTypes != null && !operateTypes.isEmpty() ? 1 : 0,
                operator,
                offset,
                size);
        int total = logMapper.countByCondition(
                moduleNames != null && !moduleNames.isEmpty() ? moduleNames : Collections.emptyList(),
                moduleNames != null && !moduleNames.isEmpty() ? 1 : 0,
                operateTypes != null && !operateTypes.isEmpty() ? operateTypes : Collections.emptyList(),
                operateTypes != null && !operateTypes.isEmpty() ? 1 : 0,
                operator);
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

    @GetMapping("/modules")
    public Result<List<String>> listModules() {
        return Result.ok(logMapper.selectDistinctModules());
    }
}
