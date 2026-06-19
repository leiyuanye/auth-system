package com.example.auth.mapper;

import com.example.auth.entity.PhoneDeviceArchive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作废账号归档表 Mapper 接口
 * 对应表: phone_device_archive
 * 功能: 当企微状态和微信状态都为"作废"时，设备自动归档到此表
 */
@Mapper
public interface PhoneDeviceArchiveMapper {

    /**
     * 插入一条归档记录（主号或子号作废时写入）
     * @param archive 归档实体
     * @return 影响行数
     */
    int insert(PhoneDeviceArchive archive);

    /**
     * 按设备编码查询归档记录（主号下所有子账号也会通过 device_code 关联）
     * @param deviceCode 设备编码
     * @return 归档记录列表
     */
    List<PhoneDeviceArchive> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 统计归档总数（作废账号总数）
     * @return 总数
     */
    int countTotal();

    /**
     * 统计作废主号数量
     * @return 主号数量
     */
    int countMain();

    /**
     * 统计作废子号数量
     * @return 子号数量
     */
    int countSub();
}