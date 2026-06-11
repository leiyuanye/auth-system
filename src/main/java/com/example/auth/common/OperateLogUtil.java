package com.example.auth.common;

import cn.hutool.json.JSONUtil;
import com.example.auth.entity.SysOperateLog;
import com.example.auth.mapper.SysOperateLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 通用操作日志工具类
 * 统一记录各模块的新增/编辑/删除操作，字段变更使用"中文列名"描述
 */
@Component
public class OperateLogUtil {

    @Autowired
    private SysOperateLogMapper logMapper;

    // ==================== 字段名 → 中文列名的映射表 ====================
    // 统一小驼峰 → 中文；若表间字段重名（如 status/phone），按语义给出通用中文名
    private static final Map<String, String> FIELD_LABEL = new HashMap<>();

    // ==================== 字段值 → 中文描述的映射表 ====================
    // key: 字段名, value: Map<原值字符串, 中文描述>
    private static final Map<String, Map<String, String>> FIELD_VALUE_LABEL = new HashMap<>();

    // ==================== 应被忽略的字段（不展示在变更列表中） ====================
    private static final Set<String> IGNORE_FIELDS = new HashSet<>();

    static {
        // ----- 通用字段 -----
        FIELD_LABEL.put("id", "ID");
        FIELD_LABEL.put("remark", "备注");
        FIELD_LABEL.put("createTime", "创建时间");
        FIELD_LABEL.put("updateTime", "更新时间");

        // ----- 手机卡 (phone_card) -----
        FIELD_LABEL.put("cardNumber", "卡号");
        FIELD_LABEL.put("agentId", "代理商ID");
        FIELD_LABEL.put("agentName", "代理商");
        FIELD_LABEL.put("phoneNumber", "手机号");
        FIELD_LABEL.put("realnameId", "实名人ID");
        FIELD_LABEL.put("realnameName", "实名人");
        FIELD_LABEL.put("department", "部门");
        FIELD_LABEL.put("package_", "套餐");
        FIELD_LABEL.put("cardStatus", "状态");
        FIELD_LABEL.put("cardType", "类型");

        // ----- 代理商 (phone_agent) -----
        FIELD_LABEL.put("agentName", "代理商名称");
        FIELD_LABEL.put("contact", "联系人");
        FIELD_LABEL.put("phone", "联系电话");
        FIELD_LABEL.put("address", "地址");
        FIELD_LABEL.put("status", "状态");

        // ----- 实名人员 (phone_realname) -----
        FIELD_LABEL.put("realName", "姓名");
        FIELD_LABEL.put("scanStatus", "扫脸便捷性");

        // ----- 系统用户 (sys_user) -----
        FIELD_LABEL.put("username", "用户名");
        FIELD_LABEL.put("password", "密码");
        FIELD_LABEL.put("nickname", "昵称");
        FIELD_LABEL.put("email", "邮箱");
        FIELD_LABEL.put("avatar", "头像");
        FIELD_LABEL.put("deptId", "部门ID");

        // ----- 角色 (sys_role) -----
        FIELD_LABEL.put("roleName", "角色名称");
        FIELD_LABEL.put("roleKey", "角色编码");
        FIELD_LABEL.put("roleSort", "显示顺序");

        // ----- 菜单 (sys_menu) -----
        FIELD_LABEL.put("menuName", "菜单名称");
        FIELD_LABEL.put("parentId", "上级菜单");
        FIELD_LABEL.put("orderNum", "显示顺序");
        FIELD_LABEL.put("path", "路由地址");
        FIELD_LABEL.put("component", "组件路径");
        FIELD_LABEL.put("menuType", "菜单类型");
        FIELD_LABEL.put("visible", "显示状态");
        FIELD_LABEL.put("perms", "权限标识");
        FIELD_LABEL.put("icon", "图标");

        // ----- 服务器 (sys_server) -----
        FIELD_LABEL.put("serverName", "服务器名称");
        FIELD_LABEL.put("ipAddress", "IP地址");
        FIELD_LABEL.put("serverType", "服务器类型");
        FIELD_LABEL.put("location", "所在机房");
        FIELD_LABEL.put("specs", "配置");
        FIELD_LABEL.put("serverStatus", "运行状态");
        FIELD_LABEL.put("stockStatus", "库存状态");

        // ----- 字段值映射 -----
        // 手机卡状态: 1=正常, 2=二次实名, 3=欠费
        Map<String, String> cardStatusMap = new HashMap<>();
        cardStatusMap.put("1", "正常");
        cardStatusMap.put("2", "二次实名");
        cardStatusMap.put("3", "欠费");
        FIELD_VALUE_LABEL.put("cardStatus", cardStatusMap);

        // 手机卡类型: 1=在用, 2=备用
        Map<String, String> cardTypeMap = new HashMap<>();
        cardTypeMap.put("1", "在用");
        cardTypeMap.put("2", "备用");
        FIELD_VALUE_LABEL.put("cardType", cardTypeMap);

        // 扫脸便捷性: 1=不能扫脸, 2=方便扫脸, 3=较难扫脸
        Map<String, String> scanStatusMap = new HashMap<>();
        scanStatusMap.put("1", "不能扫脸");
        scanStatusMap.put("2", "方便扫脸");
        scanStatusMap.put("3", "较难扫脸");
        FIELD_VALUE_LABEL.put("scanStatus", scanStatusMap);

        // 代理商状态: 0=禁用, 1=启用
        Map<String, String> agentStatusMap = new HashMap<>();
        agentStatusMap.put("0", "禁用");
        agentStatusMap.put("1", "启用");
        FIELD_VALUE_LABEL.put("status", agentStatusMap);

        // 菜单类型: M=目录, C=菜单, F=按钮
        Map<String, String> menuTypeMap = new HashMap<>();
        menuTypeMap.put("M", "目录");
        menuTypeMap.put("C", "菜单");
        menuTypeMap.put("F", "按钮");
        FIELD_VALUE_LABEL.put("menuType", menuTypeMap);

        // 服务器运行状态: 1=运行中 2=维护中 3=已下线
        Map<String, String> serverStatusMap = new HashMap<>();
        serverStatusMap.put("1", "运行中");
        serverStatusMap.put("2", "维护中");
        serverStatusMap.put("3", "已下线");
        FIELD_VALUE_LABEL.put("serverStatus", serverStatusMap);

        // 菜单显示状态: 0=显示, 1=隐藏
        Map<String, String> visibleMap = new HashMap<>();
        visibleMap.put("0", "显示");
        visibleMap.put("1", "隐藏");
        FIELD_VALUE_LABEL.put("visible", visibleMap);

        // 忽略列表 —— 不对以下字段做"变更对比"展示
        IGNORE_FIELDS.add("serialVersionUID");
        IGNORE_FIELDS.add("createTime");
        IGNORE_FIELDS.add("updateTime");
        IGNORE_FIELDS.add("password");       // 密码不展示具体值
        IGNORE_FIELDS.add("avatar");         // 头像 blob/base64 不对比
    }

    // ==================== 对外接口 ====================

    public void logAdd(String moduleName, Long dataId, String dataName, Object newEntity, String operator) {
        SysOperateLog log = buildBase(moduleName, "新增", dataId, dataName, operator);
        log.setNewValue(safeJson(newEntity));
        log.setRemark("新增了一条「" + moduleName + "」数据：" + safeName(dataName));
        tryInsert(log);
    }

    public <T> void logUpdate(String moduleName, Long dataId, String dataName, T oldEntity, T newEntity, String operator) {
        SysOperateLog log = buildBase(moduleName, "编辑", dataId, dataName, operator);
        log.setOldValue(safeJson(oldEntity));
        log.setNewValue(safeJson(newEntity));
        log.setFieldChanged(buildFieldDiff(oldEntity, newEntity));
        log.setRemark("编辑了「" + moduleName + "」数据：" + safeName(dataName) + "，修改字段：" + log.getFieldChanged());
        tryInsert(log);
    }

    public void logUpdate(String moduleName, Long dataId, String dataName, String oldDesc, String newDesc, String operator, String remark) {
        SysOperateLog log = buildBase(moduleName, "编辑", dataId, dataName, operator);
        log.setOldValue(oldDesc);
        log.setNewValue(newDesc);
        log.setFieldChanged(remark != null ? remark : (oldDesc + " → " + newDesc));
        log.setRemark(remark != null ? remark : ("编辑了「" + moduleName + "」数据：" + safeName(dataName)));
        tryInsert(log);
    }

    public void logDelete(String moduleName, Long dataId, String dataName, Object oldEntity, String operator) {
        SysOperateLog log = buildBase(moduleName, "删除", dataId, dataName, operator);
        log.setOldValue(safeJson(oldEntity));
        log.setRemark("删除了「" + moduleName + "」数据：" + safeName(dataName));
        tryInsert(log);
    }

    // ==================== 内部工具 ====================

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
            System.err.println("[OperateLog] 写入日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 对比两个实体字段差异，输出格式：【中文列名】("原值" → "新值")、...
     * - 字段名通过 FIELD_LABEL 映射为中文；未配置的字段以字段名本身兜底
     * - 状态类字段值通过 FIELD_VALUE_LABEL 映射为可读中文
     */
    private <T> String buildFieldDiff(T oldObj, T newObj) {
        if (oldObj == null && newObj == null) return "-";
        if (oldObj == null) return "(原数据为空)";
        if (newObj == null) return "(新数据为空)";

        List<String> diff = new ArrayList<>();
        try {
            for (Field f : getAllFields(oldObj.getClass())) {
                f.setAccessible(true);
                String name = f.getName();
                if (IGNORE_FIELDS.contains(name)) continue;

                Object v1 = f.get(oldObj);
                Object v2 = f.get(newObj);
                if (!valueChanged(v1, v2)) continue;

                String label = FIELD_LABEL.getOrDefault(name, name);
                String strV1 = translateValue(name, v1);
                String strV2 = translateValue(name, v2);

                diff.add(label + "(" + strV1 + " → " + strV2 + ")");
            }
        } catch (Exception e) {
            System.err.println("[OperateLog] 字段对比异常: " + e.getMessage());
            return "(字段解析失败)";
        }
        return diff.isEmpty() ? "-" : String.join("、", diff);
    }

    /**
     * 将字段值转换为可读字符串；对枚举型字段映射为中文描述
     */
    private String translateValue(String fieldName, Object v) {
        if (v == null) return "空";
        String raw = String.valueOf(v);
        Map<String, String> valueMap = FIELD_VALUE_LABEL.get(fieldName);
        if (valueMap != null && valueMap.containsKey(raw)) {
            return valueMap.get(raw);
        }
        // 未命中映射表时：长字符串做截断，便于阅读
        return truncate(raw, 40);
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
