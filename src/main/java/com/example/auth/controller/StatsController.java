package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.dto.AgentCountItem;
import com.example.auth.dto.MonthCountItem;
import com.example.auth.dto.OperatorCountItem;
import com.example.auth.dto.RealnameDetailItem;
import com.example.auth.dto.StatusCountItem;
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
    public Result<Map<String, Object>> phoneOverview(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> data = new HashMap<>();

        data.put("totalCards", phoneCardMapper.countTotal());
        data.put("activeCards", phoneCardMapper.countByUsageStatus(1));
        data.put("backupCards", phoneCardMapper.countByUsageStatus(2));
        data.put("warningCards", phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3));

        List<AgentCountItem> agentDist = phoneCardMapper.countByAgent();
        data.put("agentDistribution", agentDist);

        List<StatusCountItem> statusDist = phoneCardMapper.countByStatusGroup();
        data.put("statusDistribution", statusDist);

        List<MonthCountItem> monthly = phoneCardMapper.monthlyExceptionProcess();
        data.put("monthlyExceptionProcess", monthly);

        // 运营商维度实名人数量（总览柱状图）
        List<OperatorCountItem> realnameDist = phoneCardMapper.countRealnameByOperator();
        data.put("realnameByOperator", realnameDist);
        data.put("totalRealnameCards", phoneCardMapper.countTotalRealname());

        // 每个实名人 × 运营商明细（分页）
        int offset = (page != null && page > 0 ? page - 1 : 0) * (size != null && size > 0 ? size : 10);
        int limit = size != null && size > 0 ? size : 10;
        List<RealnameDetailItem> realnameTable = phoneCardMapper.countByRealnameWithOperator(offset, limit);
        int tableTotal = phoneCardMapper.countByRealnameWithOperatorTotal();
        data.put("realnameWithOperatorTable", realnameTable);
        data.put("tableTotal", tableTotal);

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
        List<Server> allServers = serverMapper.selectAllForExport();
        List<Map<String, Object>> typeDist = (allServers == null ? new ArrayList<Server>() : allServers)
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
        int activeCards = phoneCardMapper.countByUsageStatus(1);
        int warningCards = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);
        int runningServers = serverMapper.countByServerStatus(1); // 运行中
        int expiredServers = serverMapper.countByServerStatus(4); // 到期(异常)

        data.put("totalCards", totalCards);
        data.put("totalServers", totalServers);
        data.put("activeCards", activeCards);
        data.put("warningCards", warningCards);
        data.put("runningServers", runningServers);
        data.put("warningServers", expiredServers);

        List<Map<String, Object>> monthlyCardTrend = phoneCardMapper.selectByCondition(null, null, null, null, null, null, 0)
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
