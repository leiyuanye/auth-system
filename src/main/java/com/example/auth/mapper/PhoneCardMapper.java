package com.example.auth.mapper;

import com.example.auth.entity.PhoneCard;
import org.apache.ibatis.annotations.Mapper;
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
     * 返回: [{agentName: 'xxx', count: 10}, ...]
     */
    List<java.util.Map<String, Object>> countByAgent();

    /**
     * 按状态分组统计各状态的手机卡数量
     * 返回: [{cardStatus: 1, count: 10}, ...]
     */
    List<java.util.Map<String, Object>> countByStatusGroup();

    /**
     * 按月统计异常手机卡处理数量（规则：当月 update_time 内，且 card_status 非 1 的手机卡）
     * 用于"月度异常处理"柱状图
     * 返回: [{monthLabel: '2024-06', count: 5}, ...]
     */
    List<java.util.Map<String, Object>> monthlyExceptionProcess();

    /**
     * 按运营商分组统计实名人数量（realname_name 非空且不为 '未实名'）
     * 返回: [{operatorType: 1, operatorLabel: '移动', count: 10}, ...]
     */
    List<java.util.Map<String, Object>> countRealnameByOperator();

    /**
     * 按实名人分组统计：每个实名人共实名多少张 + 各运营商分别多少张
     * 返回: [{ realnameName: '张三', totalCount: 3, mobileCount: 2, unicomCount: 1, telecomCount: 0, otherCount: 0 }, ...]
     */
    List<java.util.Map<String, Object>> countByRealnameWithOperator();

    /**
     * 统计已实名手机卡总数（realname_id 非空 或 realname_name 有意义）
     */
    int countTotalRealname();
}
