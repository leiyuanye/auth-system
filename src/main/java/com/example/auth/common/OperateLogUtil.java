package com.example.auth.common;

import cn.hutool.json.JSONUtil;
import com.example.auth.entity.SysOperateLog;
import com.example.auth.mapper.SysOperateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 通用操作日志工具类
 * 统一记录各模块的新增/编辑/删除操作，便于后续定位负责人
 */
@Component
public class OperateLogUtil {

    @Autowired
    private SysOperateLogMapper logMapper;

    /**
     * 记录新增操作
     */
    public void logAdd(String moduleName, Long dataId, String dataName, Object newEntity, String operator) {
        SysOperateLog log = buildBase(moduleName, "新增", dataId, dataName, operator);
        log.setNewValue(safeJson(newEntity));
        log.setRemark("新增了一条「" + moduleName + "」数据：" + safeName(dataName));
        tryInsert(log);
    }

    /**
     * 记录编辑操作（自动对比old与new，产出可读的字段变更清单）
     */
    public <T> void logUpdate(String moduleName, Long dataId, String dataName, T oldEntity, T newEntity, String operator) {
        SysOperateLog log = buildBase(moduleName, "编辑", dataId, dataName, operator);
        log.setOldValue(safeJson(oldEntity));
        log.setNewValue(safeJson(newEntity));
        log.setFieldChanged(buildFieldDiff(oldEntity, newEntity));
        log.setRemark("编辑了「" + moduleName + "」数据：" + safeName(dataName) + "，修改字段：" + log.getFieldChanged());
        tryInsert(log);
    }

    /**
     * 记录简单字符串变更（如密码重置、角色变更等无实体对比场景）
     */
    public void logUpdate(String moduleName, Long dataId, String dataName, String oldDesc, String newDesc, String operator, String remark) {
        SysOperateLog log = buildBase(moduleName, "编辑", dataId, dataName, operator);
        log.setOldValue(oldDesc);
        log.setNewValue(newDesc);
        log.setFieldChanged(remark != null ? remark : (oldDesc + " → " + newDesc));
        log.setRemark(remark != null ? remark : ("编辑了「" + moduleName + "」数据：" + safeName(dataName)));
        tryInsert(log);
    }

    /**
     * 记录删除操作
     */
    public void logDelete(String moduleName, Long dataId, String dataName, Object oldEntity, String operator) {
        SysOperateLog log = buildBase(moduleName, "删除", dataId, dataName, operator);
        log.setOldValue(safeJson(oldEntity));
        log.setRemark("删除了「" + moduleName + "」数据：" + safeName(dataName));
        tryInsert(log);
    }

    private SysOperateLog buildBase(String moduleName, String operateType, Long dataId, String dataName, String operator) {
        SysOperateLog log = new SysOperateLog();
        log.setModuleName(moduleName);
        log.setOperateType(operateType);
        log.setDataId(dataId);
        log.setDataName(safeName(dataName));
        log.setOperator(safeName(operator));
        log.setOperateTime(new Date());
        return log;
    }

    private void tryInsert(SysOperateLog log) {
        try {
            logMapper.insert(log);
        } catch (Exception e) {
            // 日志记录失败不应影响主流程，但需要明确报错信息
            System.err.println("[OperateLog] 写入日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private <T> String buildFieldDiff(T oldObj, T newObj) {
        if (oldObj == null && newObj == null) return "-";
        if (oldObj == null) return "(原数据为空)";
        if (newObj == null) return "(新数据为空)";
        List<String> diff = new ArrayList<>();
        try {
            for (Field f : getAllFields(oldObj.getClass())) {
                f.setAccessible(true);
                String name = f.getName();
                if ("serialVersionUID".equals(name)) continue;
                Object v1 = f.get(oldObj);
                Object v2 = f.get(newObj);
                if (valueChanged(v1, v2)) {
                    diff.add(name + "(\"" + truncate(strValue(v1), 40) + "\"→\"" + truncate(strValue(v2), 40) + "\")");
                }
            }
        } catch (Exception e) {
            System.err.println("[OperateLog] 字段对比异常: " + e.getMessage());
            return "(字段解析失败)";
        }
        return diff.isEmpty() ? "-" : String.join("、", diff);
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) list.add(f);
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    private boolean valueChanged(Object v1, Object v2) {
        if (v1 == v2) return false;
        if (v1 == null || v2 == null) return true;
        return !String.valueOf(v1).equals(String.valueOf(v2));
    }

    private String strValue(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private String safeJson(Object o) {
        if (o == null) return null;
        try {
            return JSONUtil.toJsonStr(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private String safeName(String s) {
        return s == null ? "" : s;
    }
}
