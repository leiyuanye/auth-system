package com.example.auth.mapper;

import com.example.auth.entity.PhoneSubAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSubAccountMapper {

    // 根据 device_code 查询该设备的所有子账号
    List<PhoneSubAccount> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    // 查询所有子账号（用于全量统计/筛选）
    List<PhoneSubAccount> selectAll();

    PhoneSubAccount selectById(@Param("id") Long id);

    int insert(PhoneSubAccount account);

    int update(PhoneSubAccount account);

    int deleteById(@Param("id") Long id);

    // 删除主设备时级联删除子账号
    int deleteByDeviceCode(@Param("deviceCode") String deviceCode);

    // 统计某个主设备的子账号数量（用于限制最多 5 个）
    int countByDeviceCode(@Param("deviceCode") String deviceCode);
}
