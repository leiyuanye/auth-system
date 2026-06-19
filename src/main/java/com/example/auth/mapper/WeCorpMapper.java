package com.example.auth.mapper;

import com.example.auth.entity.WeCorp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 企微主体 Mapper 接口
 * 对应表: we_corp
 * 功能: 企微主体的增删改查
 */
@Mapper
public interface WeCorpMapper {

    /**
     * 查询所有企微主体（不分页）
     * @return 企微主体列表
     */
    List<WeCorp> selectAll();

    /**
     * 根据ID查询企微主体详情
     * @param id 企微主体ID
     * @return 企微主体实体
     */
    WeCorp selectById(@Param("id") Long id);

    /**
     * 新增企微主体
     * @param corp 企微主体实体
     * @return 影响行数
     */
    int insert(WeCorp corp);

    /**
     * 更新企微主体信息
     * @param corp 企微主体实体（含ID）
     * @return 影响行数
     */
    int update(WeCorp corp);

    /**
     * 根据ID删除企微主体
     * @param id 企微主体ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询企微主体列表
     * @param subjectShorts 主体简称列表（多选筛选）
     * @param customerTypes 客户类型列表（多选筛选）
     * @param keyword 关键词（企业全称/主体简称/手机号/创建人模糊匹配）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 企微主体列表
     */
    List<WeCorp> selectByCondition(
            @Param("subjectShorts") List<String> subjectShorts,
            @Param("customerTypes") List<String> customerTypes,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询企微主体总数（用于分页计算）
     * @param subjectShorts 主体简称列表
     * @param customerTypes 客户类型列表
     * @param keyword 关键词
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("subjectShorts") List<String> subjectShorts,
            @Param("customerTypes") List<String> customerTypes,
            @Param("keyword") String keyword);
}