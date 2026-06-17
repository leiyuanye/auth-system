package com.example.auth.service;

import com.example.auth.entity.WeCorp;

import java.util.List;

public interface WeCorpService {
    List<WeCorp> selectByCondition(String keyword, String corpStatus, String customerType, int offset, int size);
    int countByCondition(String keyword, String corpStatus, String customerType);
    WeCorp selectById(Long id);
    int insert(WeCorp weCorp);
    int update(WeCorp weCorp);
    int deleteById(Long id);
}