package com.example.auth.mapper;

import com.example.auth.entity.PhoneDevice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneDeviceMapper {
    List<PhoneDevice> selectAll();
    PhoneDevice selectById(@Param("id") Long id);
    int insert(PhoneDevice device);
    int update(PhoneDevice device);
    int deleteById(@Param("id") Long id);

    List<PhoneDevice> selectByCondition(
            @Param("keyword") String keyword,
            @Param("wechatStatus") Integer wechatStatus,
            @Param("useStatus") Integer useStatus,
            @Param("dept") Integer dept,
            @Param("wxStatus") Integer wxStatus,
            @Param("phoneType") Integer phoneType,
            @Param("entityName") String entityName,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("wechatStatus") Integer wechatStatus,
            @Param("useStatus") Integer useStatus,
            @Param("dept") Integer dept,
            @Param("wxStatus") Integer wxStatus,
            @Param("phoneType") Integer phoneType,
            @Param("entityName") String entityName);

    // 下拉选项 - 实名人
    List<String> selectRealnameOptions();
    // 下拉选项 - 手机号
    List<String> selectPhoneNumberOptions();
}
