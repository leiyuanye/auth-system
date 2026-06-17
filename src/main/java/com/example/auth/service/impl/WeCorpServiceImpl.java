package com.example.auth.service.impl;

import com.example.auth.entity.WeCorp;
import com.example.auth.mapper.WeCorpMapper;
import com.example.auth.service.WeCorpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeCorpServiceImpl implements WeCorpService {

    @Autowired
    private WeCorpMapper weCorpMapper;

    @Override
    public List<WeCorp> selectByCondition(List<String> subjectShorts, List<String> customerTypes, String keyword, int offset, int size) {
        return weCorpMapper.selectByCondition(subjectShorts, customerTypes, keyword, offset, size);
    }

    @Override
    public int countByCondition(List<String> subjectShorts, List<String> customerTypes, String keyword) {
        return weCorpMapper.countByCondition(subjectShorts, customerTypes, keyword);
    }

    @Override
    public WeCorp selectById(Long id) {
        return weCorpMapper.selectById(id);
    }

    @Override
    public int insert(WeCorp weCorp) {
        return weCorpMapper.insert(weCorp);
    }

    @Override
    public int update(WeCorp weCorp) {
        return weCorpMapper.update(weCorp);
    }

    @Override
    public int deleteById(Long id) {
        return weCorpMapper.deleteById(id);
    }
}