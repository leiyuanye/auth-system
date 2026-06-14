package com.example.auth.mapper;

import com.example.auth.entity.PhoneDeviceArchive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作废账号归档表 Mapper
 */
@Mapper
public interface PhoneDeviceArchiveMapper {
    // 插入一条归档记录
    int insert(PhoneDeviceArchive archive);

    // 按设备编码查询（主号下所有子账号也会通过 device_code 关联）
    List<PhoneDeviceArchive> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    // 统计总数（作废账号总数）
    int countTotal();

    // 统计作废主号数量
    int countMain();

    // 统计作废子号数量
    int countSub();
}
