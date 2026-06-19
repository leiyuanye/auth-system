package com.example.auth.mapper;

import com.example.auth.entity.PhoneSubAccount;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 手机子账号 Mapper 接口
 * 对应表: phone_sub_account
 * 功能: 手机子账号的增删改查，每个主设备最多5个子账号
 */
public interface PhoneSubAccountMapper {

    /**
     * 根据主设备编码查询该设备的所有子账号
     * @param deviceCode 主设备编码
     * @return 子账号列表
     */
    List<PhoneSubAccount> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 查询所有子账号（用于全量统计/筛选）
     * @return 子账号列表
     */
    List<PhoneSubAccount> selectAll();

    /**
     * 根据ID查询子账号详情
     * @param id 子账号ID
     * @return 子账号实体
     */
    PhoneSubAccount selectById(@Param("id") Long id);

    /**
     * 新增子账号
     * @param account 子账号实体
     * @return 影响行数
     */
    int insert(PhoneSubAccount account);

    /**
     * 更新子账号信息
     * @param account 子账号实体（含ID）
     * @return 影响行数
     */
    int update(PhoneSubAccount account);

    /**
     * 根据ID删除子账号
     * @param id 子账号ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除主设备时级联删除其所有子账号
     * @param deviceCode 主设备编码
     * @return 影响行数
     */
    int deleteByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 统计某个主设备的子账号数量（用于限制最多5个）
     * @param deviceCode 主设备编码
     * @return 子账号数量
     */
    int countByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据多个主设备编码批量查询子账号
     * @param deviceCodes 主设备编码列表
     * @return 子账号列表
     */
    List<PhoneSubAccount> selectByDeviceCodes(@Param("deviceCodes") List<String> deviceCodes);
}