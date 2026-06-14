package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.DeviceGroup;
import com.example.auth.entity.PhoneDevice;
import com.example.auth.entity.PhoneSubAccount;
import com.example.auth.mapper.PhoneDeviceMapper;
import com.example.auth.mapper.PhoneSubAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 手机设备管理（两表结构）
 *   phone_device       —— 主设备表（每个物理设备一条记录）
 *   phone_sub_account  —— 子账号表（每个主设备最多 5 个）
 */
@RestController
@CrossOrigin
public class PhoneDeviceController {

    @Autowired
    private PhoneDeviceMapper deviceMapper;

    @Autowired
    private PhoneSubAccountMapper subAccountMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "手机设备管理";
    private static final int MAX_SUB_ACCOUNTS = 5;

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
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

        device.setDeviceCode(device.getDeviceCode().trim());
        if (device.getPhoneNo() != null) device.setPhoneNo(device.getPhoneNo().trim());

        deviceMapper.insert(device);
        logUtil.logAdd(MODULE_NAME, device.getId(), device.getDeviceCode(), device, currentUser(request));

        Map<String, Object> data = new HashMap<>();
        data.put("id", device.getId());
        data.put("deviceCode", device.getDeviceCode());
        return Result.ok(data);
    }

    // ============================================================
    //  主设备：更新
    // ============================================================
    @PutMapping("/api/phone/devices/{id}")
    public Result<Void> updateDevice(@PathVariable Long id, @RequestBody PhoneDevice device, HttpServletRequest request) {
        PhoneDevice old = deviceMapper.selectById(id);
        if (old == null) {
            return Result.fail("记录不存在");
        }
        device.setId(id);
        if (device.getDeviceCode() != null) device.setDeviceCode(device.getDeviceCode().trim());
        if (device.getPhoneNo() != null) device.setPhoneNo(device.getPhoneNo().trim());

        deviceMapper.update(device);
        logUtil.logUpdate(MODULE_NAME, id, device.getDeviceCode(), old, device, currentUser(request));
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
        account.setDeviceCode(deviceCode);
        account.setAccountIndex(account.getAccountIndex().trim());

        // 校验主设备存在
        PhoneDevice device = deviceMapper.selectByDeviceCode(deviceCode);
        if (device == null) {
            return Result.fail("主设备「" + deviceCode + "」不存在，请先添加主设备");
        }

        // 校验最多 5 个子账号
        int count = subAccountMapper.countByDeviceCode(deviceCode);
        if (count >= MAX_SUB_ACCOUNTS) {
            return Result.fail("主设备「" + deviceCode + "」最多只能添加 " + MAX_SUB_ACCOUNTS + " 个子账号");
        }

        if (account.getWechatStatus() == null) account.setWechatStatus(1);
        if (account.getUseStatus() == null) account.setUseStatus(1);
        if (account.getDept() == null) account.setDept(1);
        if (account.getWechatUsage() == null) account.setWechatUsage(1);
        if (account.getWxStatus() == null) account.setWxStatus(1);
        if (account.getWxUsage() == null) account.setWxUsage(1);
        if (account.getPhoneType() == null) account.setPhoneType(1);
        if (account.getPhoneNo() != null) account.setPhoneNo(account.getPhoneNo().trim());

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
        if (account.getPhoneNo() != null) account.setPhoneNo(account.getPhoneNo().trim());

        subAccountMapper.update(account);
        logUtil.logUpdate(MODULE_NAME, id,
                (account.getDeviceCode() != null ? account.getDeviceCode() : old.getDeviceCode()) + "-" +
                (account.getAccountIndex() != null ? account.getAccountIndex() : old.getAccountIndex()),
                old, account, currentUser(request));
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
