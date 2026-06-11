package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.mapper.PhoneCardMapper;
import com.example.auth.mapper.ServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计/Overview 接口
 * path: /api/stats
 * 为首页、手机卡数据总览、服务器总览等页面提供聚合数据
 */
@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private PhoneCardMapper phoneCardMapper;

    @Autowired
    private ServerMapper serverMapper;

    /**
     * 手机卡数据总览
     * GET /api/stats/phone/overview
     */
    @GetMapping("/phone/overview")
    public Result<Map<String, Object>> phoneOverview() {
        Map<String, Object> data = new HashMap<>();

        int total = phoneCardMapper.countTotal();
        int activeCards = phoneCardMapper.countByCardType(1);
        int backupCards = phoneCardMapper.countByCardType(2);
        // 异常卡(欠费=3 或 二次实名=2)
        int warningCards = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);

        data.put("totalCards", total);
        data.put("activeCards", activeCards);
        data.put("backupCards", backupCards);
        data.put("warningCards", warningCards);

        // 按代理商分布
        List<Map<String, Object>> agentDist = phoneCardMapper.countByAgent();
        data.put("agentDistribution", agentDist);

        // 按状态分布
        List<Map<String, Object>> statusDist = phoneCardMapper.countByStatusGroup();
        data.put("statusDistribution", statusDist);

        // 月度异常处理 (按月统计异常卡数量)
        List<Map<String, Object>> monthly = phoneCardMapper.monthlyExceptionProcess();
        data.put("monthlyExceptionProcess", monthly);

        return Result.ok(data);
    }

    /**
     * 服务器数据总览
     * GET /api/stats/server/overview
     */
    @GetMapping("/server/overview")
    public Result<Map<String, Object>> serverOverview() {
        Map<String, Object> data = new HashMap<>();

        int total = serverMapper.countTotal();
        int activeServers = serverMapper.countByCardType(1);
        int backupServers = serverMapper.countByCardType(2);
        // 异常(维护中=2 或 已下线=3)
        int warningServers = serverMapper.countByServerStatus(2) + serverMapper.countByServerStatus(3);

        data.put("totalServers", total);
        data.put("activeServers", activeServers);
        data.put("backupServers", backupServers);
        data.put("warningServers", warningServers);

        // 服务器类型分布 (通过简单聚合查询所有在用记录)
        List<Map<String, Object>> typeDist = serverMapper.selectByCondition(null, null, null, null, 0, 10000)
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getServerType() != null ? s.getServerType() : "未知",
                        java.util.stream.Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("serverType", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
        data.put("typeDistribution", typeDist);

        return Result.ok(data);
    }

    /**
     * 首页聚合统计
     * GET /api/stats/home
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> homeStats() {
        Map<String, Object> data = new HashMap<>();

        int totalCards = phoneCardMapper.countTotal();
        int totalServers = serverMapper.countTotal();
        int activeCards = phoneCardMapper.countByCardType(1);
        int warningCards = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);
        int activeServers = serverMapper.countByCardType(1);
        int warningServers = serverMapper.countByServerStatus(2) + serverMapper.countByServerStatus(3);

        data.put("totalCards", totalCards);
        data.put("totalServers", totalServers);
        data.put("activeCards", activeCards);
        data.put("warningCards", warningCards);
        data.put("activeServers", activeServers);
        data.put("warningServers", warningServers);

        // 按月新增手机卡（按 create_time）在 Service 层做一个简单统计
        List<Map<String, Object>> monthlyCardTrend = phoneCardMapper.selectByCondition(null, null, null, 0, 10000)
                .stream()
                .filter(c -> c.getCreateTime() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> new java.text.SimpleDateFormat("yyyy-MM").format(c.getCreateTime()),
                        java.util.stream.Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("month", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .limit(6)
                .collect(java.util.stream.Collectors.toList());
        data.put("monthlyCardTrend", monthlyCardTrend);

        return Result.ok(data);
    }
}
