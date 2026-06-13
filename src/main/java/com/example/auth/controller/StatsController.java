package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.Server;
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
 * 服务器管理：在用/备用合并，按 server_status 状态聚合
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
     */
    @GetMapping("/phone/overview")
    public Result<Map<String, Object>> phoneOverview() {
        Map<String, Object> data = new HashMap<>();

        int total = phoneCardMapper.countTotal();
        int activeCards = phoneCardMapper.countByCardType(1);
        int backupCards = phoneCardMapper.countByCardType(2);
        int warningCards = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);

        data.put("totalCards", total);
        data.put("activeCards", activeCards);
        data.put("backupCards", backupCards);
        data.put("warningCards", warningCards);

        List<Map<String, Object>> agentDist = phoneCardMapper.countByAgent();
        data.put("agentDistribution", agentDist);

        List<Map<String, Object>> statusDist = phoneCardMapper.countByStatusGroup();
        data.put("statusDistribution", statusDist);

        List<Map<String, Object>> monthly = phoneCardMapper.monthlyExceptionProcess();
        data.put("monthlyExceptionProcess", monthly);

        return Result.ok(data);
    }

    /**
     * 服务器总览：不再区分在用/备用，按状态 + 类型分布
     */
    @GetMapping("/server/overview")
    public Result<Map<String, Object>> serverOverview() {
        Map<String, Object> data = new HashMap<>();

        int total = serverMapper.countTotal();
        int running = serverMapper.countByServerStatus(1);   // 运行中
        int maintenance = serverMapper.countByServerStatus(2); // 维护中
        int offline = serverMapper.countByServerStatus(3);     // 已下线
        int expired = serverMapper.countByServerStatus(4);     // 到期

        data.put("totalServers", total);
        data.put("runningServers", running);
        data.put("maintenanceServers", maintenance);
        data.put("offlineServers", offline);
        data.put("expiredServers", expired);

        // 服务器类型分布
        List<Map<String, Object>> typeDist = serverMapper.selectByCondition(null, null, null, 0, 10000)
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
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> homeStats() {
        Map<String, Object> data = new HashMap<>();

        int totalCards = phoneCardMapper.countTotal();
        int totalServers = serverMapper.countTotal();
        int activeCards = phoneCardMapper.countByCardType(1);
        int warningCards = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);
        int runningServers = serverMapper.countByServerStatus(1); // 运行中
        int expiredServers = serverMapper.countByServerStatus(4); // 到期(异常)

        data.put("totalCards", totalCards);
        data.put("totalServers", totalServers);
        data.put("activeCards", activeCards);
        data.put("warningCards", warningCards);
        data.put("runningServers", runningServers);
        data.put("warningServers", expiredServers);

        List<Map<String, Object>> monthlyCardTrend = phoneCardMapper.selectByCondition(null, null, null, null, 0, 10000)
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
