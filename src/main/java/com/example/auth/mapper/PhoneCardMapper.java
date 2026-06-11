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
            @Param("cardType") Integer cardType,
            @Param("cardStatus") Integer cardStatus,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    int countByCondition(
            @Param("keyword") String keyword,
            @Param("cardType") Integer cardType,
            @Param("cardStatus") Integer cardStatus);
}
