package com.example.auth.service;

import com.example.auth.entity.PhoneRealname;

import java.util.List;

public interface PhoneRealnameService {
    List<PhoneRealname> selectByCondition(String keyword, Integer scanStatus, String colleagueStatus, int offset, int size);
    int countByCondition(String keyword, Integer scanStatus, String colleagueStatus);
    PhoneRealname selectById(Long id);
    int insert(PhoneRealname realname);
    int update(PhoneRealname realname);
    int deleteById(Long id);
}