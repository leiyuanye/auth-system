package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.Dict;
import com.example.auth.mapper.DictMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 数据字典接口
 * path: /api/dict
 */
@RestController
@RequestMapping("/api/dict")
@CrossOrigin
public class DictController {

    private static final Logger log = LoggerFactory.getLogger(DictController.class);

    @Autowired
    private DictMapper dictMapper;

    /**
     * 字典类型 → 业务表字段的级联映射配置
     * key: dictType
     * value: List of {table, column, isCsv}
     *   - table: 业务表名
     *   - column: 业务表中存储字典值的列名
     *   - isCsv: 是否为逗号分隔的多值字段（如 entity_name 存 "公司A,公司B"）
     */
    private static final Map<String, List<Map<String, Object>>> CASCADE_CONFIG = new HashMap<>();
    static {
        // 企微主体简称 → we_corp.subject_short（直接文本）+ phone_device.entity_name（逗号分隔）
        addCascade("we_corp_subject_short", "we_corp", "subject_short", false);
        addCascade("we_corp_subject_short", "phone_device", "entity_name", true);
        addCascade("we_corp_subject_short", "phone_sub_account", "entity_name", true);

        // 企微客户类型 → we_corp.customer_type
        addCascade("we_corp_customer_type", "we_corp", "customer_type", false);

        // 企微主体状态 → we_corp.corp_status
        addCascade("we_corp_status", "we_corp", "corp_status", false);

        // 服务器类型 → sys_server.server_type
        addCascade("server_type", "sys_server", "server_type", false);

        // 服务器分组 → sys_server.specs
        addCascade("server_group", "sys_server", "specs", false);

        // 手机卡代理商 → phone_card.agent_name
        addCascade("phone_agent", "phone_card", "agent_name", false);

        // 手机位置 → phone_device.phone_location + phone_sub_account.phone_location
        addCascade("phone_device_phone_location", "phone_device", "phone_location", false);
        addCascade("phone_device_phone_location", "phone_sub_account", "phone_location", false);
    }

    private static void addCascade(String dictType, String table, String column, boolean isCsv) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("table", table);
        entry.put("column", column);
        entry.put("isCsv", isCsv);
        CASCADE_CONFIG.computeIfAbsent(dictType, k -> new ArrayList<>()).add(entry);
    }

    /**
     * 按类型查询字典项列表
     * @param type server_type / server_group / server_status / stock_status
     */
    @GetMapping("/type/{type}")
    public Result<List<Dict>> getByType(@PathVariable String type) {
        List<Dict> list = dictMapper.selectByType(type);
        return Result.ok(list);
    }

    /**
     * 新增字典项
     */
    @PostMapping
    public Result<Void> add(@RequestBody Dict dict) {
        dictMapper.insert(dict);
        return Result.ok(null);
    }

    /**
     * 更新字典项（含级联更新业务表）
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Dict dict) {
        // 1. 查询旧的字典值
        Dict oldDict = dictMapper.selectById(id);
        if (oldDict == null) {
            return Result.fail("字典项不存在");
        }

        dict.setId(id);
        dictMapper.update(dict);

        // 2. 如果 dictValue 发生了变化，级联更新所有引用该值的业务表
        String oldValue = oldDict.getDictValue();
        String newValue = dict.getDictValue();
        if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
            cascadeUpdate(oldDict.getDictType(), oldValue, newValue);
        }

        return Result.ok(null);
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictMapper.deleteById(id);
        return Result.ok(null);
    }

    /**
     * 执行级联更新
     */
    private void cascadeUpdate(String dictType, String oldValue, String newValue) {
        List<Map<String, Object>> cascades = CASCADE_CONFIG.get(dictType);
        if (cascades == null || cascades.isEmpty()) {
            return;
        }

        for (Map<String, Object> cascade : cascades) {
            String table = (String) cascade.get("table");
            String column = (String) cascade.get("column");
            boolean isCsv = (boolean) cascade.get("isCsv");
            try {
                int affected;
                if (isCsv) {
                    affected = dictMapper.cascadeUpdateCsvValue(table, column, oldValue, newValue);
                } else {
                    affected = dictMapper.cascadeUpdateValue(table, column, oldValue, newValue);
                }
                if (affected > 0) {
                    log.info("字典级联更新: dictType={}, table={}, column={}, old='{}' -> new='{}', 影响{}行",
                            dictType, table, column, oldValue, newValue, affected);
                }
            } catch (Exception e) {
                log.warn("字典级联更新失败: dictType={}, table={}, column={}, error={}",
                        dictType, table, column, e.getMessage());
            }
        }
    }
}
