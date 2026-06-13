package com.example.auth.mapper;

import com.example.auth.entity.WeCorp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeCorpMapper {
    List<WeCorp> selectAll();

    WeCorp selectById(@Param("id") Long id);

    int insert(WeCorp corp);

    int update(WeCorp corp);

    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询
     * @param subjectShorts 主体简称列表（多选，逗号分隔的传入字符串，或由前端拼成）
     * @param customerTypes 客户类型列表（多选）
     * @param keyword 关键字（搜索企业全称/主体简称/手机号/创建人）
     * @param offset
     * @param limit
     */
    List<WeCorp> selectByCondition(
            @Param("subjectShorts") List<String> subjectShorts,
            @Param("customerTypes") List<String> customerTypes,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("subjectShorts") List<String> subjectShorts,
            @Param("customerTypes") List<String> customerTypes,
            @Param("keyword") String keyword);
}
