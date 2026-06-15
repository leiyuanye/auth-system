package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.DeviceGroup;
import com.example.auth.entity.Dict;
import com.example.auth.entity.PhoneDevice;
import com.example.auth.entity.PhoneSubAccount;
import com.example.auth.entity.PhoneDeviceArchive;
import com.example.auth.mapper.DictMapper;
import com.example.auth.mapper.PhoneDeviceArchiveMapper;
import com.example.auth.mapper.PhoneDeviceMapper;
import com.example.auth.mapper.PhoneSubAccountMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 手机设备管理（两表结构 + 归档表）
 *   phone_device          —— 主设备表（每个物理设备一条记录）
 *   phone_sub_account     —— 子账号表（每个主设备最多 5 个）
 *   phone_device_archive  —— 作废账号归档表（仅用于统计）
 *
 * 业务规则：
 *   - device_code 唯一；编辑时可变更（需同步子账号关联）
 *   - 手机类型 = 摩托罗拉(3) 才能挂子号；摩托罗拉 device_code 必须以 "MT" 开头
 *   - 手机位置 (phone_location) 由字典管理；子号的手机位置与主号同步
 *   - 当企微状态 & 微信状态都为"作废"时，该记录自动归档，从原表移除
 */
@RestController
@CrossOrigin
public class PhoneDeviceController {

    @Autowired
    private PhoneDeviceMapper deviceMapper;

    @Autowired
    private PhoneSubAccountMapper subAccountMapper;

    @Autowired
    private PhoneDeviceArchiveMapper archiveMapper;

    @Autowired
    private DictMapper dictMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "手机设备管理";
    private static final int MAX_SUB_ACCOUNTS = 5;
    private static final int PHONE_TYPE_MOTOROLA = 3;
    private static final String MOTOROLA_PREFIX = "MT";
    private static final String DICT_WECHAT_STATUS = "phone_device_wechat_status";
    private static final String DICT_WX_STATUS = "phone_device_wx_status";
    private static final String VOID_LABEL = "作废";
    private static final String[] DEVICE_IMPORT_HEADERS = {
            "账号类型", "设备编码", "槽位", "企微昵称", "主体简称", "企微实名人", "企微手机号", "手机位置",
            "企微状态", "使用状态", "使用部门", "企微用途", "微信状态", "微信用途", "手机类型",
            "微信实名人", "微信手机号", "微信密码", "备注"
    };

    // 缓存"作废"对应的字典 key（避免重复查询）
    private Integer cachedVoidWechatStatusKey = null;
    private Integer cachedVoidWxStatusKey = null;

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    // 获取企微状态字典中"作废"对应的 key
    private Integer getVoidWechatStatusKey() {
        if (cachedVoidWechatStatusKey != null) return cachedVoidWechatStatusKey;
        List<Dict> list = dictMapper.selectByType(DICT_WECHAT_STATUS);
        if (list == null) return null;
        for (Dict d : list) {
            if (VOID_LABEL.equals(d.getDictValue())) {
                try {
                    cachedVoidWechatStatusKey = Integer.parseInt(d.getDictKey());
                    return cachedVoidWechatStatusKey;
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    // 获取微信状态字典中"作废"对应的 key
    private Integer getVoidWxStatusKey() {
        if (cachedVoidWxStatusKey != null) return cachedVoidWxStatusKey;
        List<Dict> list = dictMapper.selectByType(DICT_WX_STATUS);
        if (list == null) return null;
        for (Dict d : list) {
            if (VOID_LABEL.equals(d.getDictValue())) {
                try {
                    cachedVoidWxStatusKey = Integer.parseInt(d.getDictKey());
                    return cachedVoidWxStatusKey;
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    // 判断两个状态是否都为"作废"
    private boolean isBothVoid(Integer wechatStatus, Integer wxStatus) {
        if (wechatStatus == null || wxStatus == null) return false;
        Integer voidWechat = getVoidWechatStatusKey();
        Integer voidWx = getVoidWxStatusKey();
        // 如果字典里查不到"作废"，返回 false —— 不做归档（避免误操作）
        if (voidWechat == null || voidWx == null) return false;
        return voidWechat.equals(wechatStatus) && voidWx.equals(wxStatus);
    }

    private String getDictValue(String dictType, Integer dictKey) {
        if (dictKey == null) return null;
        List<Dict> list = dictMapper.selectByType(dictType);
        if (list == null) return null;
        for (Dict d : list) {
            if (d.getDictKey() != null && String.valueOf(dictKey).equals(d.getDictKey())) {
                return d.getDictValue();
            }
        }
        return null;
    }

    private boolean isAccountStatusEnabled(String dictType, Integer status) {
        if (status == null) return false;
        String value = getDictValue(dictType, status);
        return !"无".equals(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String validateAccountFields(Integer wechatStatus, String wechatPhone, Integer wechatUsage,
                                         Integer wxStatus, String wxPhone, Integer wxUsage, String wxPassword) {
        if (isAccountStatusEnabled(DICT_WECHAT_STATUS, wechatStatus)) {
            if (isBlank(wechatPhone)) {
                return "企微状态不是“无”时，请选择企微手机号";
            }
            if (wechatUsage == null) {
                return "企微状态不是“无”时，请选择企微用途";
            }
        }

        if (isAccountStatusEnabled(DICT_WX_STATUS, wxStatus)) {
            if (isBlank(wxPhone)) {
                return "微信状态不是“无”时，请选择微信手机号";
            }
            if (wxUsage == null) {
                return "微信状态不是“无”时，请选择微信用途";
            }
            if (isBlank(wxPassword)) {
                return "微信状态不是“无”时，请填写微信密码";
            }
        }
        return null;
    }

    private String validateDeviceAccountFields(PhoneDevice device) {
        return validateAccountFields(
                device.getWechatStatus(), device.getWechatPhone(), device.getWechatUsage(),
                device.getWxStatus(), device.getWxPhone(), device.getWxUsage(), device.getWxPassword()
        );
    }

    private String validateSubAccountFields(PhoneSubAccount account) {
        return validateAccountFields(
                account.getWechatStatus(), account.getWechatPhone(), account.getWechatUsage(),
                account.getWxStatus(), account.getWxPhone(), account.getWxUsage(), account.getWxPassword()
        );
    }

    private PhoneDevice mergeDeviceForValidation(PhoneDevice old, PhoneDevice incoming) {
        PhoneDevice merged = new PhoneDevice();
        merged.setWechatStatus(incoming.getWechatStatus() != null ? incoming.getWechatStatus() : old.getWechatStatus());
        merged.setWechatPhone(incoming.getWechatPhone() != null ? incoming.getWechatPhone() : old.getWechatPhone());
        merged.setWechatUsage(incoming.getWechatUsage() != null ? incoming.getWechatUsage() : old.getWechatUsage());
        merged.setWxStatus(incoming.getWxStatus() != null ? incoming.getWxStatus() : old.getWxStatus());
        merged.setWxPhone(incoming.getWxPhone() != null ? incoming.getWxPhone() : old.getWxPhone());
        merged.setWxUsage(incoming.getWxUsage() != null ? incoming.getWxUsage() : old.getWxUsage());
        merged.setWxPassword(incoming.getWxPassword() != null ? incoming.getWxPassword() : old.getWxPassword());
        return merged;
    }

    private PhoneSubAccount mergeSubAccountForValidation(PhoneSubAccount old, PhoneSubAccount incoming) {
        PhoneSubAccount merged = new PhoneSubAccount();
        merged.setWechatStatus(incoming.getWechatStatus() != null ? incoming.getWechatStatus() : old.getWechatStatus());
        merged.setWechatPhone(incoming.getWechatPhone() != null ? incoming.getWechatPhone() : old.getWechatPhone());
        merged.setWechatUsage(incoming.getWechatUsage() != null ? incoming.getWechatUsage() : old.getWechatUsage());
        merged.setWxStatus(incoming.getWxStatus() != null ? incoming.getWxStatus() : old.getWxStatus());
        merged.setWxPhone(incoming.getWxPhone() != null ? incoming.getWxPhone() : old.getWxPhone());
        merged.setWxUsage(incoming.getWxUsage() != null ? incoming.getWxUsage() : old.getWxUsage());
        merged.setWxPassword(incoming.getWxPassword() != null ? incoming.getWxPassword() : old.getWxPassword());
        return merged;
    }

    private void writeHeader(Sheet sheet, Workbook wb) {
        CellStyle headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        Row header = sheet.createRow(0);
        for (int i = 0; i < DEVICE_IMPORT_HEADERS.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(DEVICE_IMPORT_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private String labelOrValue(String dictType, Integer value) {
        if (value == null) return "";
        String label = getDictValue(dictType, value);
        return label == null ? String.valueOf(value) : label;
    }

    private void writeDeviceRow(Row row, String accountType, String accountIndex, PhoneDevice device) {
        row.createCell(0).setCellValue(accountType);
        row.createCell(1).setCellValue(defaultString(device.getDeviceCode(), ""));
        row.createCell(2).setCellValue(accountIndex);
        row.createCell(3).setCellValue(defaultString(device.getWechatNickname(), ""));
        row.createCell(4).setCellValue(defaultString(device.getEntityName(), ""));
        row.createCell(5).setCellValue(defaultString(device.getWechatPerson(), ""));
        row.createCell(6).setCellValue(defaultString(device.getWechatPhone(), ""));
        row.createCell(7).setCellValue(defaultString(device.getPhoneLocation(), ""));
        row.createCell(8).setCellValue(labelOrValue(DICT_WECHAT_STATUS, device.getWechatStatus()));
        row.createCell(9).setCellValue(labelOrValue("phone_device_use_status", device.getUseStatus()));
        row.createCell(10).setCellValue(labelOrValue("phone_device_dept", device.getDept()));
        row.createCell(11).setCellValue(labelOrValue("phone_device_wechat_usage", device.getWechatUsage()));
        row.createCell(12).setCellValue(labelOrValue(DICT_WX_STATUS, device.getWxStatus()));
        row.createCell(13).setCellValue(labelOrValue("phone_device_wx_usage", device.getWxUsage()));
        row.createCell(14).setCellValue(labelOrValue("phone_device_phone_type", device.getPhoneType()));
        row.createCell(15).setCellValue(defaultString(device.getWxRealname(), ""));
        row.createCell(16).setCellValue(defaultString(device.getWxPhone(), ""));
        row.createCell(17).setCellValue(defaultString(device.getWxPassword(), ""));
        row.createCell(18).setCellValue(defaultString(device.getRemark(), ""));
    }

    private void writeSubAccountRow(Row row, PhoneSubAccount account) {
        row.createCell(0).setCellValue("子");
        row.createCell(1).setCellValue(defaultString(account.getDeviceCode(), ""));
        row.createCell(2).setCellValue(defaultString(account.getAccountIndex(), ""));
        row.createCell(3).setCellValue(defaultString(account.getWechatNickname(), ""));
        row.createCell(4).setCellValue(defaultString(account.getEntityName(), ""));
        row.createCell(5).setCellValue(defaultString(account.getWechatPerson(), ""));
        row.createCell(6).setCellValue(defaultString(account.getWechatPhone(), ""));
        row.createCell(7).setCellValue(defaultString(account.getPhoneLocation(), ""));
        row.createCell(8).setCellValue(labelOrValue(DICT_WECHAT_STATUS, account.getWechatStatus()));
        row.createCell(9).setCellValue(labelOrValue("phone_device_use_status", account.getUseStatus()));
        row.createCell(10).setCellValue(labelOrValue("phone_device_dept", account.getDept()));
        row.createCell(11).setCellValue(labelOrValue("phone_device_wechat_usage", account.getWechatUsage()));
        row.createCell(12).setCellValue(labelOrValue(DICT_WX_STATUS, account.getWxStatus()));
        row.createCell(13).setCellValue(labelOrValue("phone_device_wx_usage", account.getWxUsage()));
        row.createCell(14).setCellValue(labelOrValue("phone_device_phone_type", account.getPhoneType()));
        row.createCell(15).setCellValue(defaultString(account.getWxRealname(), ""));
        row.createCell(16).setCellValue(defaultString(account.getWxPhone(), ""));
        row.createCell(17).setCellValue(defaultString(account.getWxPassword(), ""));
        row.createCell(18).setCellValue(defaultString(account.getRemark(), ""));
    }

    private String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row, int maxCells) {
        for (int i = 0; i < maxCells; i++) {
            if (!isBlank(getCellString(row.getCell(i)))) return false;
        }
        return true;
    }

    private Integer parseDictKey(String dictType, String value, Integer defaultValue) {
        if (isBlank(value)) return defaultValue;
        String text = value.trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
        }
        List<Dict> list = dictMapper.selectByType(dictType);
        if (list != null) {
            for (Dict d : list) {
                if (text.equals(d.getDictValue())) {
                    return Integer.parseInt(d.getDictKey());
                }
            }
        }
        throw new IllegalArgumentException("字典值不存在：" + text);
    }

    private PhoneDevice buildDeviceFromRow(Row row, String deviceCode) {
        PhoneDevice device = new PhoneDevice();
        device.setDeviceCode(deviceCode);
        device.setPhoneNo(deviceCode);
        device.setWechatNickname(getCellString(row.getCell(3)));
        device.setEntityName(getCellString(row.getCell(4)));
        device.setWechatPerson(getCellString(row.getCell(5)));
        device.setWechatPhone(getCellString(row.getCell(6)));
        device.setPhoneLocation(getCellString(row.getCell(7)));
        device.setWechatStatus(parseDictKey(DICT_WECHAT_STATUS, getCellString(row.getCell(8)), 0));
        device.setUseStatus(parseDictKey("phone_device_use_status", getCellString(row.getCell(9)), 1));
        device.setDept(parseDictKey("phone_device_dept", getCellString(row.getCell(10)), 1));
        device.setWechatUsage(parseDictKey("phone_device_wechat_usage", getCellString(row.getCell(11)), 1));
        device.setWxStatus(parseDictKey(DICT_WX_STATUS, getCellString(row.getCell(12)), 0));
        device.setWxUsage(parseDictKey("phone_device_wx_usage", getCellString(row.getCell(13)), 1));
        device.setPhoneType(parseDictKey("phone_device_phone_type", getCellString(row.getCell(14)), 1));
        device.setWxRealname(getCellString(row.getCell(15)));
        device.setWxPhone(getCellString(row.getCell(16)));
        device.setWxPassword(getCellString(row.getCell(17)));
        device.setRemark(getCellString(row.getCell(18)));
        return device;
    }

    private PhoneSubAccount buildSubAccountFromRow(Row row, String deviceCode, String accountIndex) {
        PhoneSubAccount account = new PhoneSubAccount();
        account.setDeviceCode(deviceCode);
        account.setAccountIndex(accountIndex);
        account.setWechatNickname(getCellString(row.getCell(3)));
        account.setEntityName(getCellString(row.getCell(4)));
        account.setWechatPerson(getCellString(row.getCell(5)));
        account.setWechatPhone(getCellString(row.getCell(6)));
        account.setPhoneLocation(getCellString(row.getCell(7)));
        account.setWechatStatus(parseDictKey(DICT_WECHAT_STATUS, getCellString(row.getCell(8)), 0));
        account.setUseStatus(parseDictKey("phone_device_use_status", getCellString(row.getCell(9)), 1));
        account.setDept(parseDictKey("phone_device_dept", getCellString(row.getCell(10)), 1));
        account.setWechatUsage(parseDictKey("phone_device_wechat_usage", getCellString(row.getCell(11)), 1));
        account.setWxStatus(parseDictKey(DICT_WX_STATUS, getCellString(row.getCell(12)), 0));
        account.setWxUsage(parseDictKey("phone_device_wx_usage", getCellString(row.getCell(13)), 1));
        account.setPhoneType(parseDictKey("phone_device_phone_type", getCellString(row.getCell(14)), PHONE_TYPE_MOTOROLA));
        account.setWxRealname(getCellString(row.getCell(15)));
        account.setWxPhone(getCellString(row.getCell(16)));
        account.setWxPassword(getCellString(row.getCell(17)));
        account.setRemark(getCellString(row.getCell(18)));
        return account;
    }

    // 将主号写入归档表（从原表删除）
    private void archiveMainDevice(PhoneDevice device) {
        PhoneDeviceArchive a = new PhoneDeviceArchive();
        a.setAccountType("main");
        a.setDeviceCode(device.getDeviceCode());
        a.setAccountIndex("主");
        a.setPhoneNo(device.getPhoneNo());
        a.setWechatNickname(device.getWechatNickname());
        a.setEntityName(device.getEntityName());
        a.setWechatPerson(device.getWechatPerson());
        a.setWechatPhone(device.getWechatPhone());
        a.setPhoneLocation(device.getPhoneLocation());
        a.setWechatStatus(device.getWechatStatus());
        a.setUseStatus(device.getUseStatus());
        a.setDept(device.getDept());
        a.setWechatUsage(device.getWechatUsage());
        a.setWxStatus(device.getWxStatus());
        a.setWxUsage(device.getWxUsage());
        a.setPhoneType(device.getPhoneType());
        a.setWxRealname(device.getWxRealname());
        a.setWxPhone(device.getWxPhone());
        a.setWxPassword(device.getWxPassword());
        a.setRemark(device.getRemark());
        a.setCreateTime(device.getCreateTime());
        a.setUpdateTime(device.getUpdateTime());
        archiveMapper.insert(a);
        deviceMapper.deleteById(device.getId());
    }

    // 将子号写入归档表（从原表删除）
    private void archiveSubAccount(PhoneSubAccount account) {
        PhoneDeviceArchive a = new PhoneDeviceArchive();
        a.setAccountType("sub");
        a.setDeviceCode(account.getDeviceCode());
        a.setAccountIndex(account.getAccountIndex());
        a.setPhoneNo(account.getPhoneNo());
        a.setWechatNickname(account.getWechatNickname());
        a.setEntityName(account.getEntityName());
        a.setWechatPerson(account.getWechatPerson());
        a.setWechatPhone(account.getWechatPhone());
        a.setPhoneLocation(account.getPhoneLocation());
        a.setWechatStatus(account.getWechatStatus());
        a.setUseStatus(account.getUseStatus());
        a.setDept(account.getDept());
        a.setWechatUsage(account.getWechatUsage());
        a.setWxStatus(account.getWxStatus());
        a.setWxUsage(account.getWxUsage());
        a.setPhoneType(account.getPhoneType());
        a.setWxRealname(account.getWxRealname());
        a.setWxPhone(account.getWxPhone());
        a.setWxPassword(account.getWxPassword());
        a.setRemark(account.getRemark());
        a.setCreateTime(account.getCreateTime());
        a.setUpdateTime(account.getUpdateTime());
        archiveMapper.insert(a);
        subAccountMapper.deleteById(account.getId());
    }

    // ============================================================
    //  分组列表（核心接口）—— 返回设备 + 子账号的分组数据
    // ============================================================
    @GetMapping("/api/phone/devices/groups")
    public Result<List<DeviceGroup>> groups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer wechatStatus,
            @RequestParam(required = false) Integer useStatus,
            @RequestParam(required = false) Integer dept,
            @RequestParam(required = false) Integer wxStatus,
            @RequestParam(required = false) Integer phoneType,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String wechatPerson,
            @RequestParam(required = false) String phoneLocation,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "500") int size) {

        int offset = Math.max(0, (page - 1) * size);

        // 1. 查询主设备（带筛选）
        List<PhoneDevice> devices = deviceMapper.selectByCondition(
                keyword, wechatStatus, useStatus, dept, wxStatus, phoneType,
                entityName, wechatPerson, phoneLocation, null, offset, size);

        if (devices == null || devices.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        // 2. 组装 DeviceGroup（主设备 + 子账号）
        List<DeviceGroup> result = new ArrayList<>();
        for (PhoneDevice d : devices) {
            DeviceGroup group = new DeviceGroup();
            group.setId(d.getId());
            group.setDeviceCode(d.getDeviceCode());
            group.setPhoneNo(d.getPhoneNo());
            group.setWechatNickname(d.getWechatNickname());
            group.setEntityName(d.getEntityName());
            group.setWechatPerson(d.getWechatPerson());
            group.setWechatPhone(d.getWechatPhone());
            group.setPhoneLocation(d.getPhoneLocation());
            group.setWechatStatus(d.getWechatStatus());
            group.setUseStatus(d.getUseStatus());
            group.setDept(d.getDept());
            group.setWechatUsage(d.getWechatUsage());
            group.setWxStatus(d.getWxStatus());
            group.setWxUsage(d.getWxUsage());
            group.setPhoneType(d.getPhoneType());
            group.setWxRealname(d.getWxRealname());
            group.setWxPhone(d.getWxPhone());
            group.setWxPassword(d.getWxPassword());
            group.setRemark(d.getRemark());
            group.setCreateTime(d.getCreateTime());
            group.setUpdateTime(d.getUpdateTime());
            group.setAccountIndex("主");
            group.setIsMain(1);

            // 查询子账号
            List<PhoneSubAccount> subs = subAccountMapper.selectByDeviceCode(d.getDeviceCode());
            group.setSubAccounts(subs != null ? subs : new ArrayList<>());

            result.add(group);
        }
        return Result.ok(result);
    }

    // ============================================================
    //  主设备：详情
    // ============================================================
    @GetMapping("/api/phone/devices/{id}")
    public Result<PhoneDevice> getDeviceById(@PathVariable Long id) {
        return Result.ok(deviceMapper.selectById(id));
    }

    @GetMapping("/api/phone/sub-accounts/{id}")
    public Result<PhoneSubAccount> getSubById(@PathVariable Long id) {
        return Result.ok(subAccountMapper.selectById(id));
    }

    // ============================================================
    //  下拉选项
    // ============================================================
    @GetMapping("/api/phone/devices/options/realnames")
    public Result<List<String>> getRealnameOptions() {
        return Result.ok(deviceMapper.selectRealnameOptions());
    }

    @GetMapping("/api/phone/devices/template")
    public StreamingResponseBody downloadDeviceTemplate(HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        String filename = "手机设备导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        return outputStream -> {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("手机设备导入模板");
                writeHeader(sheet, wb);
                String[] exampleValues = {
                        "主", "TEST002", "", "测试企微02", "科技A", "测试实名人", "13900000001", "测试机架-002",
                        "正常", "使用中", "淘客组", "接粉号", "正常", "视频号", "Android",
                        "", "13900000001", "test_wx_002", "导入模板示例"
                };
                Row row = sheet.createRow(1);
                for (int i = 0; i < exampleValues.length; i++) {
                    row.createCell(i).setCellValue(exampleValues[i]);
                }
                for (int i = 0; i < DEVICE_IMPORT_HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 14 * 256));
                }
                wb.write(outputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @GetMapping("/api/phone/devices/export")
    public StreamingResponseBody exportDevices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer wechatStatus,
            @RequestParam(required = false) Integer useStatus,
            @RequestParam(required = false) Integer dept,
            @RequestParam(required = false) Integer wxStatus,
            @RequestParam(required = false) Integer phoneType,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String wechatPerson,
            @RequestParam(required = false) String phoneLocation,
            HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        List<PhoneDevice> devices = deviceMapper.selectByCondition(
                keyword, wechatStatus, useStatus, dept, wxStatus, phoneType,
                entityName, wechatPerson, phoneLocation, null, 0, Integer.MAX_VALUE);
        String filename = "手机设备数据_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        return outputStream -> {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("手机设备数据");
                writeHeader(sheet, wb);
                int rowIndex = 1;
                if (devices != null) {
                    for (PhoneDevice device : devices) {
                        writeDeviceRow(sheet.createRow(rowIndex++), "主", "", device);
                        List<PhoneSubAccount> subs = subAccountMapper.selectByDeviceCode(device.getDeviceCode());
                        if (subs != null) {
                            for (PhoneSubAccount sub : subs) {
                                writeSubAccountRow(sheet.createRow(rowIndex++), sub);
                            }
                        }
                    }
                }
                for (int i = 0; i < DEVICE_IMPORT_HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 12 * 256));
                }
                wb.write(outputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @PostMapping("/api/phone/devices/import")
    public Result<Map<String, Object>> importDevices(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.fail("仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        Set<String> importedMain = new HashSet<>();
        Set<String> importedSub = new HashSet<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, DEVICE_IMPORT_HEADERS.length)) continue;
                int line = i + 1;
                try {
                    String accountType = defaultString(getCellString(row.getCell(0)), "主").trim();
                    String deviceCode = getCellString(row.getCell(1));
                    if (isBlank(deviceCode)) {
                        throw new IllegalArgumentException("设备编码不能为空");
                    }
                    deviceCode = deviceCode.trim();
                    if (accountType.contains("子")) {
                        String accountIndex = getCellString(row.getCell(2));
                        if (isBlank(accountIndex)) {
                            throw new IllegalArgumentException("子号槽位不能为空");
                        }
                        String subKey = deviceCode + "-" + accountIndex.trim();
                        if (!importedSub.add(subKey)) {
                            throw new IllegalArgumentException("导入文件内子号重复：" + subKey);
                        }
                        PhoneSubAccount account = buildSubAccountFromRow(row, deviceCode, accountIndex.trim());
                        Result<Map<String, Object>> result = addSubAccount(account, request);
                        if (result.getCode() == 200) {
                            successCount++;
                        } else {
                            throw new IllegalArgumentException(result.getMessage());
                        }
                    } else {
                        if (!importedMain.add(deviceCode)) {
                            throw new IllegalArgumentException("导入文件内主号重复：" + deviceCode);
                        }
                        PhoneDevice device = buildDeviceFromRow(row, deviceCode);
                        Result<Map<String, Object>> result = addDevice(device, request);
                        if (result.getCode() == 200) {
                            successCount++;
                        } else {
                            throw new IllegalArgumentException(result.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    failCount++;
                    errors.add("第" + line + "行：" + ex.getMessage());
                }
            }
        } catch (Exception e) {
            return Result.fail("导入失败: " + e.getMessage());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        data.put("failCount", failCount);
        data.put("total", successCount + failCount);
        data.put("message", String.join("；", errors));
        return Result.ok(data);
    }

    @GetMapping("/api/phone/devices/options/phone-numbers")
    public Result<List<String>> getPhoneNumberOptions() {
        return Result.ok(deviceMapper.selectPhoneNumberOptions());
    }

    /**
     * 返回所有主设备编码（用于新增子号时选择主设备）
     */
    @GetMapping("/api/phone/devices/options/device-codes")
    public Result<List<String>> getDeviceCodeOptions() {
        return Result.ok(deviceMapper.selectDeviceCodeOptions());
    }

    /**
     * 仅返回摩托罗拉类型的主设备编码（只有摩托罗拉可以挂子号）
     */
    @GetMapping("/api/phone/devices/options/device-codes/motorola")
    public Result<List<String>> getMotorolaDeviceCodeOptions() {
        return Result.ok(deviceMapper.selectMotorolaDeviceCodeOptions());
    }

    /**
     * 返回归档统计 —— 作废账号总数
     */
    @GetMapping("/api/phone/archive/statistics")
    public Result<Map<String, Object>> getArchiveStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("total", archiveMapper.countTotal());
        data.put("main", archiveMapper.countMain());
        data.put("sub", archiveMapper.countSub());
        return Result.ok(data);
    }

    /**
     * 返回某个主设备的子账号数量 + 已占用槽位
     */
    @GetMapping("/api/phone/sub-accounts/device-status/{deviceCode}")
    public Result<Map<String, Object>> getSubAccountStatus(@PathVariable String deviceCode) {
        Map<String, Object> result = new HashMap<>();
        int count = subAccountMapper.countByDeviceCode(deviceCode);
        List<PhoneSubAccount> list = subAccountMapper.selectByDeviceCode(deviceCode);
        List<String> usedSlots = new ArrayList<>();
        if (list != null) {
            for (PhoneSubAccount s : list) {
                usedSlots.add(s.getAccountIndex());
            }
        }
        result.put("count", count);
        result.put("max", MAX_SUB_ACCOUNTS);
        result.put("usedSlots", usedSlots);
        result.put("canAdd", count < MAX_SUB_ACCOUNTS);
        return Result.ok(result);
    }

    // ============================================================
    //  主设备：新增
    // ============================================================
    @PostMapping("/api/phone/devices")
    public Result<Map<String, Object>> addDevice(@RequestBody PhoneDevice device, HttpServletRequest request) {
        if (device.getDeviceCode() == null || device.getDeviceCode().trim().isEmpty()) {
            return Result.fail("设备编码不能为空");
        }
        // device_code 唯一校验
        PhoneDevice existing = deviceMapper.selectByDeviceCode(device.getDeviceCode().trim());
        if (existing != null) {
            return Result.fail("设备编码「" + device.getDeviceCode() + "」已存在，不能重复");
        }

        if (device.getWechatStatus() == null) device.setWechatStatus(1);
        if (device.getUseStatus() == null) device.setUseStatus(1);
        if (device.getDept() == null) device.setDept(1);
        if (device.getWechatUsage() == null) device.setWechatUsage(1);
        if (device.getWxStatus() == null) device.setWxStatus(1);
        if (device.getWxUsage() == null) device.setWxUsage(1);
        if (device.getPhoneType() == null) device.setPhoneType(1);

        String deviceCode = device.getDeviceCode().trim();
        device.setDeviceCode(deviceCode);
        // 完整编号由后端自动生成 = 设备编码
        device.setPhoneNo(deviceCode);

        // 摩托罗拉命名规范：必须以 "MT" 开头（不区分大小写）
        if (PHONE_TYPE_MOTOROLA == device.getPhoneType() && !deviceCode.toUpperCase().startsWith(MOTOROLA_PREFIX)) {
            return Result.fail("摩托罗拉设备的设备编码必须以 \"MT\" 开头，例如 MT601、MT602");
        }

        String validationMessage = validateDeviceAccountFields(device);
        if (validationMessage != null) {
            return Result.fail(validationMessage);
        }

        // 插入前判断：如果两个状态都为"作废"，直接写入归档表
        if (isBothVoid(device.getWechatStatus(), device.getWxStatus())) {
            deviceMapper.insert(device);
            PhoneDevice fresh = deviceMapper.selectById(device.getId());
            archiveMainDevice(fresh);
            logUtil.logAdd(MODULE_NAME, device.getId(), device.getDeviceCode() + "[已作废归档]", device, currentUser(request));
            Map<String, Object> data = new HashMap<>();
            data.put("id", device.getId());
            data.put("deviceCode", device.getDeviceCode());
            data.put("archived", true);
            return Result.ok(data);
        }

        deviceMapper.insert(device);
        logUtil.logAdd(MODULE_NAME, device.getId(), device.getDeviceCode(), device, currentUser(request));

        Map<String, Object> data = new HashMap<>();
        data.put("id", device.getId());
        data.put("deviceCode", device.getDeviceCode());
        return Result.ok(data);
    }

    // ============================================================
    //  主设备：更新
    //  - device_code 可变更，但必须唯一（排除自身）
    //  - 变更 device_code 时，级联更新所有子账号的 device_code
    //  - 更新后若两状态都为"作废" → 归档（主号 + 子号都删除并写入 archive）
    // ============================================================
    @PutMapping("/api/phone/devices/{id}")
    public Result<Void> updateDevice(@PathVariable Long id, @RequestBody PhoneDevice device, HttpServletRequest request) {
        PhoneDevice old = deviceMapper.selectById(id);
        if (old == null) {
            return Result.fail("记录不存在");
        }
        device.setId(id);

        // 处理 device_code 变更（新值必须唯一且与旧值不同）
        boolean deviceCodeChanged = false;
        String oldDeviceCode = old.getDeviceCode();
        if (device.getDeviceCode() != null) {
            String newDeviceCode = device.getDeviceCode().trim();
            device.setDeviceCode(newDeviceCode);
            device.setPhoneNo(newDeviceCode);
            if (!newDeviceCode.equals(oldDeviceCode)) {
                // 新值是否与其他记录冲突
                PhoneDevice conflict = deviceMapper.selectByDeviceCode(newDeviceCode);
                if (conflict != null && !conflict.getId().equals(id)) {
                    return Result.fail("设备编码「" + newDeviceCode + "」已存在，不能使用");
                }
                deviceCodeChanged = true;
            }
        }

        String validationMessage = validateDeviceAccountFields(mergeDeviceForValidation(old, device));
        if (validationMessage != null) {
            return Result.fail(validationMessage);
        }

        // 执行更新
        deviceMapper.update(device);

        // device_code 变更，级联更新所有子账号
        if (deviceCodeChanged) {
            List<PhoneSubAccount> subs = subAccountMapper.selectByDeviceCode(oldDeviceCode);
            if (subs != null) {
                for (PhoneSubAccount s : subs) {
                    PhoneSubAccount update = new PhoneSubAccount();
                    update.setId(s.getId());
                    update.setDeviceCode(device.getDeviceCode());
                    // phoneNo 也跟着变
                    update.setPhoneNo(device.getDeviceCode() + "-" + s.getAccountIndex());
                    subAccountMapper.update(update);
                }
            }
        }

        logUtil.logUpdate(MODULE_NAME, id,
                (device.getDeviceCode() != null ? device.getDeviceCode() : old.getDeviceCode()),
                old, device, currentUser(request));

        // 更新后判断：两状态都为"作废" → 归档
        // 重新拉取最新记录（合并字段）
        PhoneDevice after = deviceMapper.selectById(id);
        // 以提交值优先判断，若提交值为空，用原记录值
        Integer wechatStatusAfter = (device.getWechatStatus() != null) ? device.getWechatStatus() : after.getWechatStatus();
        Integer wxStatusAfter = (device.getWxStatus() != null) ? device.getWxStatus() : after.getWxStatus();

        if (isBothVoid(wechatStatusAfter, wxStatusAfter)) {
            // 先归档子号，再归档主号
            if (after.getDeviceCode() != null && !after.getDeviceCode().isEmpty()) {
                List<PhoneSubAccount> subs = subAccountMapper.selectByDeviceCode(after.getDeviceCode());
                if (subs != null) {
                    for (PhoneSubAccount s : subs) {
                        archiveSubAccount(s);
                    }
                }
            }
            archiveMainDevice(after);
        }

        return Result.ok(null);
    }

    // ============================================================
    //  主设备：删除（级联删除子账号）
    // ============================================================
    @DeleteMapping("/api/phone/devices/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id, HttpServletRequest request) {
        PhoneDevice old = deviceMapper.selectById(id);
        if (old == null) {
            return Result.fail("记录不存在");
        }
        // 先删子账号，再删主设备
        if (old.getDeviceCode() != null && !old.getDeviceCode().isEmpty()) {
            subAccountMapper.deleteByDeviceCode(old.getDeviceCode());
        }
        deviceMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, old.getDeviceCode(), old, currentUser(request));
        return Result.ok(null);
    }

    // ============================================================
    //  子账号：新增
    // ============================================================
    @PostMapping("/api/phone/sub-accounts")
    public Result<Map<String, Object>> addSubAccount(@RequestBody PhoneSubAccount account, HttpServletRequest request) {
        if (account.getDeviceCode() == null || account.getDeviceCode().trim().isEmpty()) {
            return Result.fail("请选择主设备（device_code 不能为空）");
        }
        if (account.getAccountIndex() == null || account.getAccountIndex().trim().isEmpty()) {
            return Result.fail("账号槽位不能为空");
        }

        String deviceCode = account.getDeviceCode().trim();
        String accountIndex = account.getAccountIndex().trim();
        account.setDeviceCode(deviceCode);
        account.setAccountIndex(accountIndex);

        // 校验主设备存在
        PhoneDevice device = deviceMapper.selectByDeviceCode(deviceCode);
        if (device == null) {
            return Result.fail("主设备「" + deviceCode + "」不存在，请先添加主设备");
        }

        // 只有摩托罗拉类型的设备可以添加子号
        if (device.getPhoneType() == null || PHONE_TYPE_MOTOROLA != device.getPhoneType()) {
            return Result.fail("只有摩托罗拉类型的设备才能添加子号，当前设备类型为：" + (device.getPhoneType() == null ? "未设置" : device.getPhoneType()));
        }

        // 槽位必须是 1~5 的数字
        int slot;
        try {
            slot = Integer.parseInt(accountIndex);
        } catch (NumberFormatException e) {
            return Result.fail("账号槽位必须是 1~5 的数字");
        }
        if (slot < 1 || slot > MAX_SUB_ACCOUNTS) {
            return Result.fail("账号槽位必须是 1~5 的数字");
        }

        // 校验最多 5 个子账号
        int count = subAccountMapper.countByDeviceCode(deviceCode);
        if (count >= MAX_SUB_ACCOUNTS) {
            return Result.fail("主设备「" + deviceCode + "」最多只能添加 " + MAX_SUB_ACCOUNTS + " 个子账号");
        }

        // 槽位不能重复
        java.util.List<PhoneSubAccount> existing = subAccountMapper.selectByDeviceCode(deviceCode);
        if (existing != null) {
            for (PhoneSubAccount s : existing) {
                if (accountIndex.equals(s.getAccountIndex())) {
                    return Result.fail("该设备下槽位 " + accountIndex + " 已被占用，请选择其他槽位");
                }
            }
        }

        if (account.getWechatStatus() == null) account.setWechatStatus(1);
        if (account.getUseStatus() == null) account.setUseStatus(1);
        if (account.getDept() == null) account.setDept(1);
        if (account.getWechatUsage() == null) account.setWechatUsage(1);
        if (account.getWxStatus() == null) account.setWxStatus(1);
        if (account.getWxUsage() == null) account.setWxUsage(1);
        // 子号的手机类型必须与主号保持一致（同一台手机登录）
        account.setPhoneType(device.getPhoneType());
        // 子号的手机位置与主号同步（手机属于主号）
        account.setPhoneLocation(device.getPhoneLocation());
        // 子号完整编号由后端自动生成 = deviceCode + "-" + accountIndex
        account.setPhoneNo(deviceCode + "-" + accountIndex);

        String validationMessage = validateSubAccountFields(account);
        if (validationMessage != null) {
            return Result.fail(validationMessage);
        }

        // 插入前判断：两状态都为"作废" → 写入后直接归档
        if (isBothVoid(account.getWechatStatus(), account.getWxStatus())) {
            subAccountMapper.insert(account);
            PhoneSubAccount fresh = subAccountMapper.selectById(account.getId());
            archiveSubAccount(fresh);
            logUtil.logAdd(MODULE_NAME, account.getId(), deviceCode + "-" + accountIndex + "[已作废归档]", account, currentUser(request));
            Map<String, Object> data = new HashMap<>();
            data.put("id", account.getId());
            data.put("deviceCode", deviceCode);
            data.put("accountIndex", accountIndex);
            data.put("archived", true);
            return Result.ok(data);
        }

        subAccountMapper.insert(account);
        logUtil.logAdd(MODULE_NAME, account.getId(),
                deviceCode + "-" + account.getAccountIndex(), account, currentUser(request));

        Map<String, Object> data = new HashMap<>();
        data.put("id", account.getId());
        data.put("deviceCode", account.getDeviceCode());
        data.put("accountIndex", account.getAccountIndex());
        return Result.ok(data);
    }

    // ============================================================
    //  子账号：更新
    //  - 若两状态都为"作废" → 自动归档（从子账号表删除，写入归档表）
    // ============================================================
    @PutMapping("/api/phone/sub-accounts/{id}")
    public Result<Void> updateSubAccount(@PathVariable Long id, @RequestBody PhoneSubAccount account, HttpServletRequest request) {
        PhoneSubAccount old = subAccountMapper.selectById(id);
        if (old == null) {
            return Result.fail("记录不存在");
        }
        account.setId(id);
        if (account.getDeviceCode() != null) account.setDeviceCode(account.getDeviceCode().trim());
        if (account.getAccountIndex() != null) account.setAccountIndex(account.getAccountIndex().trim());
        // 子号完整编号 = deviceCode + "-" + accountIndex
        String dc = account.getDeviceCode() != null ? account.getDeviceCode() : old.getDeviceCode();
        String ai = account.getAccountIndex() != null ? account.getAccountIndex() : old.getAccountIndex();
        account.setPhoneNo(dc + "-" + ai);
        // 子号的手机类型必须与主号保持一致（同一台手机登录），忽略前端值
        PhoneDevice mainDevice = deviceMapper.selectByDeviceCode(dc);
        if (mainDevice != null) {
            account.setPhoneType(mainDevice.getPhoneType());
            // 子号的手机位置与主号同步
            account.setPhoneLocation(mainDevice.getPhoneLocation());
        }

        String validationMessage = validateSubAccountFields(mergeSubAccountForValidation(old, account));
        if (validationMessage != null) {
            return Result.fail(validationMessage);
        }

        subAccountMapper.update(account);
        logUtil.logUpdate(MODULE_NAME, id,
                (account.getDeviceCode() != null ? account.getDeviceCode() : old.getDeviceCode()) + "-" +
                (account.getAccountIndex() != null ? account.getAccountIndex() : old.getAccountIndex()),
                old, account, currentUser(request));

        // 更新后判断：两状态都为"作废" → 归档（从子账号表删除，写入归档表）
        Integer wechatStatusAfter = (account.getWechatStatus() != null) ? account.getWechatStatus() : old.getWechatStatus();
        Integer wxStatusAfter = (account.getWxStatus() != null) ? account.getWxStatus() : old.getWxStatus();

        if (isBothVoid(wechatStatusAfter, wxStatusAfter)) {
            PhoneSubAccount after = subAccountMapper.selectById(id);
            if (after != null) {
                archiveSubAccount(after);
            }
        }

        return Result.ok(null);
    }

    // ============================================================
    //  子账号：删除
    // ============================================================
    @DeleteMapping("/api/phone/sub-accounts/{id}")
    public Result<Void> deleteSubAccount(@PathVariable Long id, HttpServletRequest request) {
        PhoneSubAccount old = subAccountMapper.selectById(id);
        if (old == null) {
            return Result.fail("记录不存在");
        }
        subAccountMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, old.getDeviceCode() + "-" + old.getAccountIndex(), old, currentUser(request));
        return Result.ok(null);
    }
}
