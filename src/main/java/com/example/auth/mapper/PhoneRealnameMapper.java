package com.example.auth.mapper;

import com.example.auth.entity.PhoneRealname;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实名人员 Mapper 接口
 * 对应表: phone_realname
 * 功能: 实名人员的增删改查
 */
@Mapper
public interface PhoneRealnameMapper {

    /**
     * 查询所有实名人员（不分页）
     * @return 实名人员列表
     */
    List<PhoneRealname> selectAll();

    /**
     * 根据ID查询实名人员详情
     * @param id 实名人员ID
     * @return 实名人员实体
     */
    PhoneRealname selectById(@Param("id") Long id);

    /**
     * 新增实名人员
     * @param realname 实名人员实体
     * @return 影响行数
     */
    int insert(PhoneRealname realname);

    /**
     * 更新实名人员信息
     * @param realname 实名人员实体（含ID）
     * @return 影响行数
     */
    int update(PhoneRealname realname);

    /**
     * 根据ID删除实名人员
     * @param id 实名人员ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询实名人员列表
     * @param keyword 关键词（姓名模糊匹配）
     * @param scanStatus 扫脸便捷性状态
     * @param colleagueStatus 同事状态（active/resigned/other）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 实名人员列表
     */
    List<PhoneRealname> selectByCondition(
            @Param("keyword") String keyword,
            @Param("scanStatus") Integer scanStatus,
            @Param("colleagueStatus") String colleagueStatus,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询实名人员总数（用于分页计算）
     * @param keyword 关键词
     * @param scanStatus 扫脸便捷性状态
     * @param colleagueStatus 同事状态
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("scanStatus") Integer scanStatus,
            @Param("colleagueStatus") String colleagueStatus);
}