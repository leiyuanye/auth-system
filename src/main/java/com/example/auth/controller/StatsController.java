package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.dto.AgentCountItem;
import com.example.auth.dto.MonthCountItem;
import com.example.auth.dto.OperatorCountItem;
import com.example.auth.dto.RealnameDetailItem;
import com.example.auth.dto.StatusCountItem;
import com.example.auth.entity.Dict;
import com.example.auth.entity.Server;
import com.example.auth.mapper.DictMapper;
import com.example.auth.mapper.PhoneCardMapper;
import com.example.auth.mapper.ServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计/Overview 接口
 * path: /api/stats
 *
 * 注意：统计方法中的状态值均从数据库字典动态获取，避免硬编码导致字典修改后统计错误
 */
@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private PhoneCardMapper phoneCardMapper;

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private DictMapper dictMapper;

    /**
     * 手机卡数据总览
     */
    @GetMapping("/phone/overview")
    public Result<Map<String, Object>> phoneOverview(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Map<String, Object> data = new HashMap<>();

        data.put("totalCards", phoneCardMapper.countTotal());

        // 从字典动态获取使用状态值
        Map<String, Integer> usageStatusMap = getDictKeyToIntMap("phone_usage_status");
        int activeUsageStatus = getDictKeyAsInt("phone_usage_status", "在用", 1);
        int backupUsageStatus = getDictKeyAsInt("phone_usage_status", "备用", 2);
        data.put("activeCards", phoneCardMapper.countByUsageStatus(activeUsageStatus));
        data.put("backupCards", phoneCardMapper.countByUsageStatus(backupUsageStatus));

        // 从字典动态获取卡状态值（预警 = 非"正常"的状态）
        int normalCardStatus = getDictKeyAsInt("phone_card_status", "正常", 1);
        List<Dict> cardStatusDict = dictMapper.selectByType("phone_card_status");
        int warningCount = 0;
        if (cardStatusDict != null) {
            for (Dict d : cardStatusDict) {
                try {
                    int statusVal = Integer.parseInt(d.getDictKey());
                    if (statusVal != normalCardStatus) {
                        warningCount += phoneCardMapper.countByCardStatus(statusVal);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } else {
            // 兜底：旧逻辑（假设2=异常,3=欠费）
            warningCount = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);
        }
        data.put("warningCards", warningCount);

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
     * 状态值从数据库字典动态获取
     */
    @GetMapping("/server/overview")
    public Result<Map<String, Object>> serverOverview() {
        Map<String, Object> data = new HashMap<>();

        int total = serverMapper.countTotal();
        data.put("totalServers", total);

        // 从字典动态获取服务器状态值
        List<Dict> statusDict = dictMapper.selectByType("server_status");
        Map<String, Integer> statusCountMap = new HashMap<>();

        if (statusDict != null && !statusDict.isEmpty()) {
            // 初始化各状态计数为0
            for (Dict d : statusDict) {
                try {
                    int statusVal = Integer.parseInt(d.getDictKey());
                    statusCountMap.put(d.getDictValue(), serverMapper.countByServerStatus(statusVal));
                } catch (NumberFormatException ignored) {}
            }
        } else {
            // 兜底：旧逻辑（假设1=运行中,2=维护中,3=已下线,4=到期）
            statusCountMap.put("运行中", serverMapper.countByServerStatus(1));
            statusCountMap.put("维护中", serverMapper.countByServerStatus(2));
            statusCountMap.put("已下线", serverMapper.countByServerStatus(3));
            statusCountMap.put("到期", serverMapper.countByServerStatus(4));
        }

        data.put("runningServers", statusCountMap.getOrDefault("运行中", 0));
        data.put("maintenanceServers", statusCountMap.getOrDefault("维护中", 0));
        data.put("offlineServers", statusCountMap.getOrDefault("已下线", 0));
        data.put("expiredServers", statusCountMap.getOrDefault("到期", 0));

        // 服务器类型分布
        List<Server> allServers = serverMapper.selectAllForExport();
        List<Map<String, Object>> typeDist = (allServers == null ? new ArrayList<Server>() : allServers)
                .stream()
                .collect(Collectors.groupingBy(
                        s -> s.getServerType() != null ? s.getServerType() : "未知",
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("serverType", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
        data.put("typeDistribution", typeDist);

        return Result.ok(data);
    }

    /**
     * 首页聚合统计
     * 状态值从数据库字典动态获取
     */
    @GetMapping("/home")
    public Result<Map<String, Object>> homeStats() {
        Map<String, Object> data = new HashMap<>();

        int totalCards = phoneCardMapper.countTotal();
        int totalServers = serverMapper.countTotal();
        data.put("totalCards", totalCards);
        data.put("totalServers", totalServers);

        // 从字典动态获取使用状态值
        int activeUsageStatus = getDictKeyAsInt("phone_usage_status", "在用", 1);
        data.put("activeCards", phoneCardMapper.countByUsageStatus(activeUsageStatus));

        // 从字典动态获取卡状态值（预警 = 非"正常"的状态）
        int normalCardStatus = getDictKeyAsInt("phone_card_status", "正常", 1);
        List<Dict> cardStatusDict = dictMapper.selectByType("phone_card_status");
        int warningCount = 0;
        if (cardStatusDict != null) {
            for (Dict d : cardStatusDict) {
                try {
                    int statusVal = Integer.parseInt(d.getDictKey());
                    if (statusVal != normalCardStatus) {
                        warningCount += phoneCardMapper.countByCardStatus(statusVal);
                    }
                } catch (NumberFormatException ignored) {}
            }
        } else {
            warningCount = phoneCardMapper.countByCardStatus(2) + phoneCardMapper.countByCardStatus(3);
        }
        data.put("warningCards", warningCount);

        // 从字典动态获取服务器状态值
        int runningStatus = getDictKeyAsInt("server_status", "运行中", 1);
        int expiredStatus = getDictKeyAsInt("server_status", "到期", 4);
        data.put("runningServers", serverMapper.countByServerStatus(runningStatus));
        data.put("warningServers", serverMapper.countByServerStatus(expiredStatus));

        List<Map<String, Object>> monthlyCardTrend = phoneCardMapper.selectByCondition(null, null, null, null, null, null, 0)
                .stream()
                .filter(c -> c.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        c -> new java.text.SimpleDateFormat("yyyy-MM").format(c.getCreateTime()),
                        Collectors.counting()
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
                .collect(Collectors.toList());
        data.put("monthlyCardTrend", monthlyCardTrend);

        return Result.ok(data);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据字典类型和字典值获取对应的整型键
     * @param dictType 字典类型
     * @param dictValue 字典显示值
     * @param defaultValue 默认值（字典查询失败时使用）
     * @return 整型键值
     */
    private int getDictKeyAsInt(String dictType, String dictValue, int defaultValue) {
        List<Dict> dictList = dictMapper.selectByType(dictType);
        if (dictList != null) {
            for (Dict d : dictList) {
                if (dictValue.equals(d.getDictValue())) {
                    try {
                        return Integer.parseInt(d.getDictKey());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return defaultValue;
    }

    /**
     * 获取字典类型到整型键的映射
     * @param dictType 字典类型
     * @return 字典值 -> 整型键 的映射
     */
    private Map<String, Integer> getDictKeyToIntMap(String dictType) {
        Map<String, Integer> map = new HashMap<>();
        List<Dict> dictList = dictMapper.selectByType(dictType);
        if (dictList != null) {
            for (Dict d : dictList) {
                try {
                    map.put(d.getDictValue(), Integer.parseInt(d.getDictKey()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }
}