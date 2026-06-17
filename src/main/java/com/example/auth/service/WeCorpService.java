package com.example.auth.service;

import com.example.auth.entity.WeCorp;

import java.util.List;

public interface WeCorpService {
    List<WeCorp> selectByCondition(List<String> subjectShorts, List<String> customerTypes, String keyword, int offset, int size);
    int countByCondition(List<String> subjectShorts, List<String> customerTypes, String keyword);
    WeCorp selectById(Long id);
    int insert(WeCorp weCorp);
    int update(WeCorp weCorp);
    int deleteById(Long id);
}