package com.example.auth.service;

import com.example.auth.entity.Trademark;

import java.util.List;

public interface TrademarkService {
    List<Trademark> selectByCondition(String keyword, String category, String companyName, int offset, int size);
    int countByCondition(String keyword, String category, String companyName);
    Trademark selectById(Long id);
    int insert(Trademark trademark);
    int update(Trademark trademark);
    int deleteById(Long id);
}