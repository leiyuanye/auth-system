package com.example.auth.mapper;

import com.example.auth.entity.SysOperateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper 接口
 * 对应表: sys_operate_log
 * 功能: 记录用户操作日志，用于审计追溯
 */
@Mapper
public interface SysOperateLogMapper {

    /**
     * 查询所有操作日志（不分页）
     * @return 日志列表
     */
    List<SysOperateLog> selectAll();

    /**
     * 根据ID查询日志详情
     * @param id 日志ID
     * @return 日志实体
     */
    SysOperateLog selectById(@Param("id") Long id);

    /**
     * 新增操作日志
     * @param log 日志实体
     * @return 影响行数
     */
    int insert(SysOperateLog log);

    /**
     * 条件分页查询操作日志
     * @param moduleNames 模块名称列表（多选筛选）
     * @param useModuleFlag 是否使用模块筛选（1=启用, 0=不启用）
     * @param operateTypes 操作类型列表（多选筛选：新增/修改/删除）
     * @param useTypeFlag 是否使用类型筛选（1=启用, 0=不启用）
     * @param operator 操作人
     * @param offset 分页偏移量
     * @param limit 每页条数
     * @return 日志列表
     */
    List<SysOperateLog> selectByCondition(@Param("moduleNames") List<String> moduleNames,
                                          @Param("useModuleFlag") Integer useModuleFlag,
                                          @Param("operateTypes") List<String> operateTypes,
                                          @Param("useTypeFlag") Integer useTypeFlag,
                                          @Param("operator") String operator,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

    /**
     * 条件查询日志总数（用于分页计算）
     * @param moduleNames 模块名称列表
     * @param useModuleFlag 是否使用模块筛选
     * @param operateTypes 操作类型列表
     * @param useTypeFlag 是否使用类型筛选
     * @param operator 操作人
     * @return 符合条件的记录总数
     */
    int countByCondition(@Param("moduleNames") List<String> moduleNames,
                         @Param("useModuleFlag") Integer useModuleFlag,
                         @Param("operateTypes") List<String> operateTypes,
                         @Param("useTypeFlag") Integer useTypeFlag,
                         @Param("operator") String operator);

    /**
     * 查询所有不同的模块名称（用于日志筛选下拉）
     * @return 模块名称列表
     */
    List<String> selectDistinctModules();
}