package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneDevice;
import com.example.auth.mapper.PhoneDeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/phone/devices")
@CrossOrigin
public class PhoneDeviceController {

    @Autowired
    private PhoneDeviceMapper deviceMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "手机设备管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<PageResult<PhoneDevice>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer wechatStatus,
            @RequestParam(required = false) Integer useStatus,
            @RequestParam(required = false) Integer dept,
            @RequestParam(required = false) Integer wxStatus,
            @RequestParam(required = false) Integer phoneType,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) String wechatPerson,
            @RequestParam(required = false) String phoneLocation,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "200") int size) {
        int offset = (page - 1) * size;
        return Result.ok(new PageResult<>(
                deviceMapper.countByCondition(keyword, wechatStatus, useStatus, dept, wxStatus, phoneType,
                        entityName, wechatPerson, phoneLocation, deviceCode),
                deviceMapper.selectByCondition(keyword, wechatStatus, useStatus, dept, wxStatus, phoneType,
                        entityName, wechatPerson, phoneLocation, deviceCode, offset, size),
                page, size));
    }

    @GetMapping("/{id}")
    public Result<PhoneDevice> getById(@PathVariable Long id) {
        return Result.ok(deviceMapper.selectById(id));
    }

    @GetMapping("/options/realnames")
    public Result<java.util.List<String>> getRealnameOptions() {
        return Result.ok(deviceMapper.selectRealnameOptions());
    }

    @GetMapping("/options/phones")
    public Result<java.util.List<String>> getPhoneNumberOptions() {
        return Result.ok(deviceMapper.selectPhoneNumberOptions());
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody PhoneDevice device, HttpServletRequest request) {
        if (device.getPhoneNo() == null || device.getPhoneNo().trim().isEmpty()) {
            return Result.fail("手机编号不能为空");
        }
        if (device.getWechatStatus() == null) device.setWechatStatus(1);
        if (device.getUseStatus() == null) device.setUseStatus(1);
        if (device.getDept() == null) device.setDept(1);
        if (device.getWechatUsage() == null) device.setWechatUsage(1);
        if (device.getWxStatus() == null) device.setWxStatus(1);
        if (device.getWxUsage() == null) device.setWxUsage(1);
        if (device.getPhoneType() == null) device.setPhoneType(1);
        device.parseFromPhoneNo();
        deviceMapper.insert(device);
        logUtil.logAdd(MODULE_NAME, device.getId(), device.getPhoneNo(), device, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", device.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PhoneDevice device, HttpServletRequest request) {
        PhoneDevice old = deviceMapper.selectById(id);
        device.setId(id);
        device.parseFromPhoneNo();
        deviceMapper.update(device);
        logUtil.logUpdate(MODULE_NAME, id, device.getPhoneNo(), old, device, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        PhoneDevice old = deviceMapper.selectById(id);
        deviceMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, old != null ? old.getPhoneNo() : String.valueOf(id), old, currentUser(request));
        return Result.ok(null);
    }
}
