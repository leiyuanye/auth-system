package com.example.auth.mapper;

import com.example.auth.entity.PhoneDevice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 手机设备 Mapper 接口
 * 对应表: phone_device
 * 功能: 手机主设备的增删改查及下拉选项查询
 */
public interface PhoneDeviceMapper {

    /**
     * 查询所有手机设备（不分页）
     * @return 设备列表
     */
    List<PhoneDevice> selectAll();

    /**
     * 根据ID查询设备详情
     * @param id 设备ID
     * @return 设备实体
     */
    PhoneDevice selectById(@Param("id") Long id);

    /**
     * 根据设备编码查询设备（用于唯一性校验）
     * @param deviceCode 设备编码
     * @return 设备实体
     */
    PhoneDevice selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 新增手机设备
     * @param device 设备实体
     * @return 影响行数
     */
    int insert(PhoneDevice device);

    /**
     * 更新设备信息
     * @param device 设备实体（含ID）
     * @return 影响行数
     */
    int update(PhoneDevice device);

    /**
     * 根据ID删除设备
     * @param id 设备ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询设备列表（用于分组筛选）
     * @param keyword 关键词（设备编码/企微昵称/主体简称模糊匹配）
     * @param wechatStatus 企微状态
     * @param useStatus 使用状态
     * @param dept 使用部门
     * @param wxStatus 微信状态
     * @param phoneType 手机类型
     * @param entityName 主体简称
     * @param wechatPerson 企微实名人
     * @param phoneLocation 手机位置
     * @param deviceCode 设备编码
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 设备列表
     */
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

    /**
     * 条件查询设备总数（用于分页计算）
     * @return 符合条件的记录总数
     */
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

    /**
     * 下拉选项 - 查询所有实名人列表
     * @return 实名人姓名列表
     */
    List<String> selectRealnameOptions();

    /**
     * 下拉选项 - 查询所有手机号列表
     * @return 手机号列表
     */
    List<String> selectPhoneNumberOptions();

    /**
     * 下拉选项 - 查询所有主设备编码（新增子号时选择主设备）
     * @return 设备编码列表
     */
    List<String> selectDeviceCodeOptions();

    /**
     * 下拉选项 - 仅查询摩托罗拉类型的主设备编码（只有摩托罗拉可以挂子号）
     * @return 摩托罗拉设备编码列表
     */
    List<String> selectMotorolaDeviceCodeOptions();
}