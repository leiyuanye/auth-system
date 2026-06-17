package com.example.auth.service;

import com.example.auth.entity.PhoneCard;

import java.util.List;

public interface PhoneCardService {
    List<PhoneCard> selectByCondition(String keyword, Integer usageStatus, Integer cardStatus, Integer operatorType, String groupBy, int offset, int size);
    int countByCondition(String keyword, Integer usageStatus, Integer cardStatus, Integer operatorType);
    PhoneCard selectById(Long id);
    int insert(PhoneCard card);
    int update(PhoneCard card);
    int deleteById(Long id);
}