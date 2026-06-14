package com.example.auth.mapper;

import com.example.auth.entity.PhoneDevice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneDeviceMapper {
    List<PhoneDevice> selectAll();
    PhoneDevice selectById(@Param("id") Long id);
    PhoneDevice selectByDeviceCode(@Param("deviceCode") String deviceCode);
    int insert(PhoneDevice device);
    int update(PhoneDevice device);
    int deleteById(@Param("id") Long id);

    // ===== 带条件的列表查询（用于分组筛选）=====
    List<PhoneDevice> selectByCondition(
            @Param("keyword") String keyword,
            @Param("wechatStatus") Integer wechatStatus,
            @Param("useStatus") Integer useStatus,
            @Param("dept") Integer dept,
            @Param("wxStatus") Integer wxStatus,
            @Param("phoneType") Integer phoneType,
            @Param("entityName") String entityName,
            @Param("wechatPerson") String wechatPerson,
            @Param("phoneLocation") String phoneLocation,
            @Param("deviceCode") String deviceCode,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("wechatStatus") Integer wechatStatus,
            @Param("useStatus") Integer useStatus,
            @Param("dept") Integer dept,
            @Param("wxStatus") Integer wxStatus,
            @Param("phoneType") Integer phoneType,
            @Param("entityName") String entityName,
            @Param("wechatPerson") String wechatPerson,
            @Param("phoneLocation") String phoneLocation,
            @Param("deviceCode") String deviceCode);

    // 下拉选项 - 实名人
    List<String> selectRealnameOptions();
    // 下拉选项 - 手机号
    List<String> selectPhoneNumberOptions();
    // 下拉选项 - 主设备编码（新增子号时选择）
    List<String> selectDeviceCodeOptions();
    // 下拉选项 - 仅摩托罗拉类型的主设备编码（只有摩托罗拉可以挂子号）
    List<String> selectMotorolaDeviceCodeOptions();
}
