package com.example.auth.mapper;

import com.example.auth.entity.PhoneRealname;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PhoneRealnameMapper {
    List<PhoneRealname> selectAll();

    PhoneRealname selectById(@Param("id") Long id);

    int insert(PhoneRealname realname);

    int update(PhoneRealname realname);

    int deleteById(@Param("id") Long id);

    List<PhoneRealname> selectByCondition(
            @Param("keyword") String keyword,
            @Param("scanStatus") Integer scanStatus,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("scanStatus") Integer scanStatus);
}
