package com.example.auth.service;

import com.example.auth.entity.DeviceGroup;
import com.example.auth.entity.PhoneDevice;
import com.example.auth.entity.PhoneDeviceArchive;
import com.example.auth.entity.PhoneSubAccount;

import java.util.List;
import java.util.Map;

public interface PhoneDeviceService {
    List<DeviceGroup> selectDeviceGroups(String keyword, Integer wechatStatus, Integer useStatus, Integer dept, Integer wxStatus, Integer phoneType, String entityName, String wechatPerson, String phoneLocation, int offset, int size);
    PhoneDevice selectByDeviceCode(String deviceCode);
    PhoneDevice selectById(Long id);
    PhoneSubAccount selectSubById(Long id);
    int insertDevice(PhoneDevice device);
    int updateDevice(PhoneDevice device);
    int deleteDevice(Long id);
    void deleteSubAccountsByDeviceCode(String deviceCode);
    List<PhoneSubAccount> selectSubAccountsByDeviceCode(String deviceCode);
    int insertSubAccount(PhoneSubAccount account);
    int updateSubAccount(PhoneSubAccount account);
    int deleteSubAccount(Long id);
    int countSubAccountsByDeviceCode(String deviceCode);
    List<String> selectRealnameOptions();
    List<String> selectPhoneNumberOptions();
    List<String> selectDeviceCodeOptions();
    List<String> selectMotorolaDeviceCodeOptions();
    void archiveMainDevice(PhoneDevice device);
    void archiveSubAccount(PhoneSubAccount account);
    Map<String, Object> getArchiveStats();
    Map<String, Object> getSubAccountStatus(String deviceCode);
}