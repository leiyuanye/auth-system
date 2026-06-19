package com.example.auth.mapper;

import com.example.auth.dto.AgentCountItem;
import com.example.auth.dto.MonthCountItem;
import com.example.auth.dto.OperatorCountItem;
import com.example.auth.dto.RealnameDetailItem;
import com.example.auth.dto.StatusCountItem;
import com.example.auth.entity.PhoneCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 手机卡 Mapper 接口
 * 对应表: phone_card
 * 功能: 手机卡的增删改查及统计分析
 */
@Mapper
public interface PhoneCardMapper {

    /**
     * 查询所有手机卡（不分页）
     * @return 手机卡列表
     */
    List<PhoneCard> selectAll();

    /**
     * 根据ID查询手机卡详情
     * @param id 手机卡ID
     * @return 手机卡实体
     */
    PhoneCard selectById(@Param("id") Long id);

    /**
     * 新增手机卡
     * @param card 手机卡实体
     * @return 影响行数
     */
    int insert(PhoneCard card);

    /**
     * 更新手机卡信息
     * @param card 手机卡实体（含ID）
     * @return 影响行数
     */
    int update(PhoneCard card);

    /**
     * 根据ID删除手机卡
     * @param id 手机卡ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询手机卡列表
     * @param keyword 关键词（ICCID/手机号/实名人模糊匹配）
     * @param usageStatus 使用状态（1=在用, 2=备用）
     * @param cardStatus 卡状态（1=正常, 2=二次实名, 3=欠费）
     * @param operatorType 运营商类型（1=移动, 2=联通, 3=电信, 4=其他）
     * @param groupBy 分组字段（用于聚合查询）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 手机卡列表
     */
    List<PhoneCard> selectByCondition(
            @Param("keyword") String keyword,
            @Param("usageStatus") Integer usageStatus,
            @Param("cardStatus") Integer cardStatus,
            @Param("operatorType") Integer operatorType,
            @Param("groupBy") String groupBy,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询手机卡总数（用于分页计算）
     * @param keyword 关键词
     * @param usageStatus 使用状态
     * @param cardStatus 卡状态
     * @param operatorType 运营商类型
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("usageStatus") Integer usageStatus,
            @Param("cardStatus") Integer cardStatus,
            @Param("operatorType") Integer operatorType);

    // ==================== 统计用 ====================

    /**
     * 统计手机卡总数
     * @return 总数
     */
    int countTotal();

    /**
     * 按使用状态统计手机卡数量
     * @param usageStatus 使用状态值
     * @return 该状态下的手机卡数量
     */
    int countByUsageStatus(@Param("usageStatus") Integer usageStatus);

    /**
     * 按卡状态统计手机卡数量
     * @param cardStatus 卡状态值
     * @return 该状态下的手机卡数量
     */
    int countByCardStatus(@Param("cardStatus") Integer cardStatus);

    /**
     * 按代理商分组统计各代理商下的手机卡数量
     * @return 代理商统计列表（AgentCountItem）
     */
    List<AgentCountItem> countByAgent();

    /**
     * 按状态分组统计各状态的手机卡数量
     * @return 状态统计列表（StatusCountItem）
     */
    List<StatusCountItem> countByStatusGroup();

    /**
     * 按月统计异常手机卡处理数量（用于趋势图）
     * @return 月度统计列表（MonthCountItem）
     */
    List<MonthCountItem> monthlyExceptionProcess();

    /**
     * 按运营商分组统计实名人数量
     * @return 运营商统计列表（OperatorCountItem）
     */
    List<OperatorCountItem> countRealnameByOperator();

    /**
     * 按实名人分组统计：每个实名人在各运营商下的实名卡数量（分页）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 实名人明细列表（RealnameDetailItem）
     */
    List<RealnameDetailItem> countByRealnameWithOperator(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 按实名人分组统计的总条数（用于分页计算）
     * @return 总条数
     */
    int countByRealnameWithOperatorTotal();

    /**
     * 统计已实名手机卡总数
     * @return 已实名手机卡总数
     */
    int countTotalRealname();
}