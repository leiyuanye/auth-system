package com.example.auth.mapper;

import com.example.auth.entity.SysOperateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysOperateLogMapper {
    List<SysOperateLog> selectAll();
    SysOperateLog selectById(@Param("id") Long id);
    int insert(SysOperateLog log);

    /**
     * 条件筛选 + 分页
     * useModuleFlag = 1 时使用 moduleNames 过滤; useTypeFlag = 1 时使用 operateTypes 过滤
     */
    List<SysOperateLog> selectByCondition(@Param("moduleNames") List<String> moduleNames,
                                          @Param("useModuleFlag") Integer useModuleFlag,
                                          @Param("operateTypes") List<String> operateTypes,
                                          @Param("useTypeFlag") Integer useTypeFlag,
                                          @Param("operator") String operator,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);

    int countByCondition(@Param("moduleNames") List<String> moduleNames,
                         @Param("useModuleFlag") Integer useModuleFlag,
                         @Param("operateTypes") List<String> operateTypes,
                         @Param("useTypeFlag") Integer useTypeFlag,
                         @Param("operator") String operator);

    List<String> selectDistinctModules();
}
