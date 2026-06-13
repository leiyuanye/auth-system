package com.example.auth.mapper;

import com.example.auth.dto.AgentCountItem;
import com.example.auth.dto.MonthCountItem;
import com.example.auth.dto.OperatorCountItem;
import com.example.auth.dto.RealnameDetailItem;
import com.example.auth.dto.StatusCountItem;
import com.example.auth.entity.PhoneCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PhoneCardMapper {
    List<PhoneCard> selectAll();

    PhoneCard selectById(@Param("id") Long id);

    int insert(PhoneCard card);

    int update(PhoneCard card);

    int deleteById(@Param("id") Long id);

    List<PhoneCard> selectByCondition(
            @Param("keyword") String keyword,
            @Param("usageStatus") Integer usageStatus,
            @Param("cardStatus") Integer cardStatus,
            @Param("operatorType") Integer operatorType,
            @Param("groupBy") String groupBy,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("usageStatus") Integer usageStatus,
            @Param("cardStatus") Integer cardStatus,
            @Param("operatorType") Integer operatorType);

    // ==================== 统计用 ====================
    int countTotal();

    int countByUsageStatus(@Param("usageStatus") Integer usageStatus);

    int countByCardStatus(@Param("cardStatus") Integer cardStatus);

    /**
     * 按代理商分组统计各代理商下的手机卡数量
     */
    List<AgentCountItem> countByAgent();

    /**
     * 按状态分组统计各状态的手机卡数量
     */
    List<StatusCountItem> countByStatusGroup();

    /**
     * 按月统计异常手机卡处理数量
     */
    List<MonthCountItem> monthlyExceptionProcess();

    /**
     * 按运营商分组统计实名人数量
     */
    List<OperatorCountItem> countRealnameByOperator();

    /**
     * 按实名人分组统计：每个实名人在各运营商下的实名卡数量（分页）
     */
    List<RealnameDetailItem> countByRealnameWithOperator(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 按实名人分组统计的总条数（用于分页计算）
     */
    int countByRealnameWithOperatorTotal();

    /**
     * 统计已实名手机卡总数
     */
    int countTotalRealname();
}
