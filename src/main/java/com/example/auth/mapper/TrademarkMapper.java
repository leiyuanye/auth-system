package com.example.auth.mapper;

import com.example.auth.entity.Trademark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商标 Mapper 接口
 * 对应表: trademark
 * 功能: 商标的增删改查
 */
@Mapper
public interface TrademarkMapper {

    /**
     * 查询所有商标（不分页）
     * @return 商标列表
     */
    List<Trademark> selectAll();

    /**
     * 根据ID查询商标详情
     * @param id 商标ID
     * @return 商标实体
     */
    Trademark selectById(@Param("id") Long id);

    /**
     * 新增商标
     * @param trademark 商标实体
     * @return 影响行数
     */
    int insert(Trademark trademark);

    /**
     * 更新商标信息
     * @param trademark 商标实体（含ID）
     * @return 影响行数
     */
    int update(Trademark trademark);

    /**
     * 根据ID删除商标
     * @param id 商标ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询商标列表
     * @param keyword 关键词（商标名称模糊匹配）
     * @param category 商标类别
     * @param companyName 公司名称
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 商标列表
     */
    List<Trademark> selectByCondition(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("companyName") String companyName,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询商标总数（用于分页计算）
     * @param keyword 关键词
     * @param category 商标类别
     * @param companyName 公司名称
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("companyName") String companyName);
}