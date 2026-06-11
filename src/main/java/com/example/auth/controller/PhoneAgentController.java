package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneAgent;
import com.example.auth.mapper.PhoneAgentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phone/agents")
@CrossOrigin
public class PhoneAgentController {

    @Autowired
    private PhoneAgentMapper agentMapper;

    @GetMapping
    public Result<PageResult<PhoneAgent>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<PhoneAgent> list = agentMapper.selectByCondition(keyword, status, offset, size);
        int total = agentMapper.countByCondition(keyword, status);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<PhoneAgent> getById(@PathVariable Long id) {
        PhoneAgent agent = agentMapper.selectById(id);
        return Result.ok(agent);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody PhoneAgent agent) {
        if (agent.getAgentName() == null || agent.getAgentName().trim().isEmpty()) {
            return Result.fail("代理商名称不能为空");
        }
        if (agent.getStatus() == null) agent.setStatus(1);
        int rows = agentMapper.insert(agent);
        Map<String, Object> data = new HashMap<>();
        data.put("id", agent.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PhoneAgent agent) {
        agent.setId(id);
        int rows = agentMapper.update(agent);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        int rows = agentMapper.deleteById(id);
        return Result.ok(null);
    }
}
