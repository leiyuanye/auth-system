package com.example.auth.service.impl;

import com.example.auth.entity.DeviceGroup;
import com.example.auth.entity.PhoneDevice;
import com.example.auth.entity.PhoneDeviceArchive;
import com.example.auth.entity.PhoneSubAccount;
import com.example.auth.mapper.PhoneDeviceArchiveMapper;
import com.example.auth.mapper.PhoneDeviceMapper;
import com.example.auth.mapper.PhoneSubAccountMapper;
import com.example.auth.service.PhoneDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PhoneDeviceServiceImpl implements PhoneDeviceService {

    @Autowired
    private PhoneDeviceMapper deviceMapper;

    @Autowired
    private PhoneSubAccountMapper subAccountMapper;

    @Autowired
    private PhoneDeviceArchiveMapper archiveMapper;

    private static final int MAX_SUB_ACCOUNTS = 5;

    @Override
    public List<DeviceGroup> selectDeviceGroups(String keyword, Integer wechatStatus, Integer useStatus, Integer dept, Integer wxStatus, Integer phoneType, String entityName, String wechatPerson, String phoneLocation, int offset, int size) {
        List<PhoneDevice> devices = deviceMapper.selectByCondition(keyword, wechatStatus, useStatus, dept, wxStatus, phoneType, entityName, wechatPerson, phoneLocation, null, offset, size);
        if (devices == null || devices.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> deviceCodes = new ArrayList<>();
        for (PhoneDevice d : devices) {
            deviceCodes.add(d.getDeviceCode());
        }

        Map<String, List<PhoneSubAccount>> subAccountMap = new HashMap<>();
        if (!deviceCodes.isEmpty()) {
            List<PhoneSubAccount> allSubs = subAccountMapper.selectByDeviceCodes(deviceCodes);
            for (PhoneSubAccount sub : allSubs) {
                subAccountMap.computeIfAbsent(sub.getDeviceCode(), k -> new ArrayList<>()).add(sub);
            }
        }

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
            group.setSubAccounts(subAccountMap.getOrDefault(d.getDeviceCode(), new ArrayList<>()));
            result.add(group);
        }
        return result;
    }

    @Override
    public PhoneDevice selectByDeviceCode(String deviceCode) {
        return deviceMapper.selectByDeviceCode(deviceCode);
    }

    @Override
    public PhoneDevice selectById(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    public PhoneSubAccount selectSubById(Long id) {
        return subAccountMapper.selectById(id);
    }

    @Override
    public int insertDevice(PhoneDevice device) {
        return deviceMapper.insert(device);
    }

    @Override
    public int updateDevice(PhoneDevice device) {
        return deviceMapper.update(device);
    }

    @Override
    public int deleteDevice(Long id) {
        return deviceMapper.deleteById(id);
    }

    @Override
    public void deleteSubAccountsByDeviceCode(String deviceCode) {
        subAccountMapper.deleteByDeviceCode(deviceCode);
    }

    @Override
    public List<PhoneSubAccount> selectSubAccountsByDeviceCode(String deviceCode) {
        return subAccountMapper.selectByDeviceCode(deviceCode);
    }

    @Override
    public int insertSubAccount(PhoneSubAccount account) {
        return subAccountMapper.insert(account);
    }

    @Override
    public int updateSubAccount(PhoneSubAccount account) {
        return subAccountMapper.update(account);
    }

    @Override
    public int deleteSubAccount(Long id) {
        return subAccountMapper.deleteById(id);
    }

    @Override
    public int countSubAccountsByDeviceCode(String deviceCode) {
        return subAccountMapper.countByDeviceCode(deviceCode);
    }

    @Override
    public List<String> selectRealnameOptions() {
        return deviceMapper.selectRealnameOptions();
    }

    @Override
    public List<String> selectPhoneNumberOptions() {
        return deviceMapper.selectPhoneNumberOptions();
    }

    @Override
    public List<String> selectDeviceCodeOptions() {
        return deviceMapper.selectDeviceCodeOptions();
    }

    @Override
    public List<String> selectMotorolaDeviceCodeOptions() {
        return deviceMapper.selectMotorolaDeviceCodeOptions();
    }

    @Override
    public void archiveMainDevice(PhoneDevice device) {
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

    @Override
    public void archiveSubAccount(PhoneSubAccount account) {
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

    @Override
    public Map<String, Object> getArchiveStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("total", archiveMapper.countTotal());
        data.put("main", archiveMapper.countMain());
        data.put("sub", archiveMapper.countSub());
        return data;
    }

    @Override
    public Map<String, Object> getSubAccountStatus(String deviceCode) {
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
        return result;
    }
}