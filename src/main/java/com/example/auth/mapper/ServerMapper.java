package com.example.auth.mapper;

import com.example.auth.entity.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器 Mapper（在用/备用统一管理）
 */
@Mapper
public interface ServerMapper {
    List<Server> selectAll();

    Server selectById(@Param("id") Long id);

    int insert(Server server);

    /**
     * 批量插入（导入用）
     */
    int batchInsert(List<Server> servers);

    int update(Server server);

    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询（keyword 按 serverName/ipAddress/type 模糊；按serverStatus过滤）
     */
    List<Server> selectByCondition(
            @Param("keyword") String keyword,
            @Param("serverStatus") Integer serverStatus,
            @Param("expireSort") String expireSort,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("serverStatus") Integer serverStatus);

    /**
     * 导出全部数据（不分页）
     */
    List<Server> selectAllForExport();

    // ==================== 统计用 ====================
    int countTotal();

    int countByServerStatus(@Param("serverStatus") Integer serverStatus);
}
