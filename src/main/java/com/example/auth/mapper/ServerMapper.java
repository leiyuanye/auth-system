package com.example.auth.mapper;

import com.example.auth.entity.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器 Mapper
 */
@Mapper
public interface ServerMapper {
    List<Server> selectAll();

    Server selectById(@Param("id") Long id);

    int insert(Server server);

    int update(Server server);

    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询（keyword 按 serverName/ipAddress/type 模糊；按cardType区分在用/备用；按status过滤）
     */
    List<Server> selectByCondition(
            @Param("keyword") String keyword,
            @Param("cardType") Integer cardType,
            @Param("serverStatus") Integer serverStatus,
            @Param("stockStatus") String stockStatus,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("cardType") Integer cardType,
            @Param("serverStatus") Integer serverStatus,
            @Param("stockStatus") String stockStatus);

    // ==================== 统计用 ====================
    int countTotal();

    int countByCardType(@Param("cardType") Integer cardType);

    int countByServerStatus(@Param("serverStatus") Integer serverStatus);
}
