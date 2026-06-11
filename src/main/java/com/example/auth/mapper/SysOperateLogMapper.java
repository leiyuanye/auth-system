package com.example.auth.mapper;

import com.example.auth.entity.SysOperateLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysOperateLogMapper {
    List<SysOperateLog> selectAll();
    SysOperateLog selectById(@Param("id") Long id);
    int insert(SysOperateLog log);
    List<SysOperateLog> selectByCondition(@Param("moduleName") String moduleName,
                                          @Param("operateType") String operateType,
                                          @Param("operator") String operator,
                                          @Param("offset") Integer offset,
                                          @Param("limit") Integer limit);
    int countByCondition(@Param("moduleName") String moduleName,
                         @Param("operateType") String operateType,
                         @Param("operator") String operator);
}
