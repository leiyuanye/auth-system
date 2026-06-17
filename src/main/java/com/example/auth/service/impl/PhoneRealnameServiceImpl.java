package com.example.auth.service.impl;

import com.example.auth.entity.PhoneRealname;
import com.example.auth.mapper.PhoneRealnameMapper;
import com.example.auth.service.PhoneRealnameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhoneRealnameServiceImpl implements PhoneRealnameService {

    @Autowired
    private PhoneRealnameMapper realnameMapper;

    @Override
    public List<PhoneRealname> selectByCondition(String keyword, Integer scanStatus, String colleagueStatus, int offset, int size) {
        return realnameMapper.selectByCondition(keyword, scanStatus, colleagueStatus, offset, size);
    }

    @Override
    public int countByCondition(String keyword, Integer scanStatus, String colleagueStatus) {
        return realnameMapper.countByCondition(keyword, scanStatus, colleagueStatus);
    }

    @Override
    public PhoneRealname selectById(Long id) {
        return realnameMapper.selectById(id);
    }

    @Override
    public int insert(PhoneRealname realname) {
        return realnameMapper.insert(realname);
    }

    @Override
    public int update(PhoneRealname realname) {
        return realnameMapper.update(realname);
    }

    @Override
    public int deleteById(Long id) {
        return realnameMapper.deleteById(id);
    }
}