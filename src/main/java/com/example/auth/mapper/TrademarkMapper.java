package com.example.auth.mapper;

import com.example.auth.entity.Trademark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrademarkMapper {

    List<Trademark> selectAll();

    Trademark selectById(@Param("id") Long id);

    int insert(Trademark trademark);

    int update(Trademark trademark);

    int deleteById(@Param("id") Long id);

    List<Trademark> selectByCondition(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("companyName") String companyName,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("companyName") String companyName);
}
