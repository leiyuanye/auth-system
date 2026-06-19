package com.example.auth.mapper;

import com.example.auth.entity.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 服务器 Mapper 接口
 * 对应表: server
 * 功能: 服务器资产的增删改查及统计分析
 */
@Mapper
public interface ServerMapper {

    /**
     * 查询所有服务器（不分页）
     * @return 服务器列表
     */
    List<Server> selectAll();

    /**
     * 根据ID查询服务器详情
     * @param id 服务器ID
     * @return 服务器实体
     */
    Server selectById(@Param("id") Long id);

    /**
     * 新增服务器
     * @param server 服务器实体
     * @return 影响行数
     */
    int insert(Server server);

    /**
     * 批量插入服务器（导入用）
     * @param servers 服务器列表
     * @return 影响行数
     */
    int batchInsert(List<Server> servers);

    /**
     * 更新服务器信息
     * @param server 服务器实体（含ID）
     * @return 影响行数
     */
    int update(Server server);

    /**
     * 根据ID删除服务器
     * @param id 服务器ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 条件分页查询服务器列表
     * @param keyword 关键词（服务器名称/IP地址/类型模糊匹配）
     * @param serverStatus 服务器状态（1=运行中, 2=维护中, 3=已下线, 4=到期）
     * @param expireSort 到期时间排序（asc/desc）
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 服务器列表
     */
    List<Server> selectByCondition(
            @Param("keyword") String keyword,
            @Param("serverStatus") Integer serverStatus,
            @Param("expireSort") String expireSort,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 条件查询服务器总数（用于分页计算）
     * @param keyword 关键词
     * @param serverStatus 服务器状态
     * @return 符合条件的记录总数
     */
    int countByCondition(
            @Param("keyword") String keyword,
            @Param("serverStatus") Integer serverStatus);

    /**
     * 导出全部服务器数据（不分页）
     * @return 服务器列表
     */
    List<Server> selectAllForExport();

    // ==================== 统计用 ====================

    /**
     * 统计服务器总数
     * @return 总数
     */
    int countTotal();

    /**
     * 按服务器状态统计数量
     * @param serverStatus 服务器状态值
     * @return 该状态下的服务器数量
     */
    int countByServerStatus(@Param("serverStatus") Integer serverStatus);
}