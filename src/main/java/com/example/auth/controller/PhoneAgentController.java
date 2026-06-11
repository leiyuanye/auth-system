package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneAgent;
import com.example.auth.mapper.PhoneAgentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phone/agents")
@CrossOrigin
public class PhoneAgentController {

    @Autowired
    private PhoneAgentMapper agentMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "代理商管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

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
    public Result<Map<String, Object>> add(@RequestBody PhoneAgent agent, HttpServletRequest request) {
        if (agent.getAgentName() == null || agent.getAgentName().trim().isEmpty()) {
            return Result.fail("代理商名称不能为空");
        }
        if (agent.getStatus() == null) agent.setStatus(1);
        int rows = agentMapper.insert(agent);
        logUtil.logAdd(MODULE_NAME, agent.getId(), agent.getAgentName(), agent, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", agent.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PhoneAgent agent, HttpServletRequest request) {
        PhoneAgent oldAgent = agentMapper.selectById(id);
        agent.setId(id);
        int rows = agentMapper.update(agent);
        PhoneAgent newAgent = agentMapper.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id, agent.getAgentName() != null ? agent.getAgentName() : (oldAgent != null ? oldAgent.getAgentName() : null), oldAgent, newAgent, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        PhoneAgent oldAgent = agentMapper.selectById(id);
        int rows = agentMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldAgent != null ? oldAgent.getAgentName() : String.valueOf(id), oldAgent, currentUser(request));
        return Result.ok(null);
    }
}
