package com.example.auth.mapper;

import com.example.auth.entity.PhoneAgent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PhoneAgentMapper {
    List<PhoneAgent> selectAll();

    PhoneAgent selectById(@Param("id") Long id);

    int insert(PhoneAgent agent);

    int update(PhoneAgent agent);

    int deleteById(@Param("id") Long id);

    List<PhoneAgent> selectByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("status") Integer status);
}
