package com.example.auth.service.impl;

import com.example.auth.entity.Trademark;
import com.example.auth.mapper.TrademarkMapper;
import com.example.auth.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrademarkServiceImpl implements TrademarkService {

    @Autowired
    private TrademarkMapper trademarkMapper;

    @Override
    public List<Trademark> selectByCondition(String keyword, String category, String companyName, int offset, int size) {
        return trademarkMapper.selectByCondition(keyword, category, companyName, offset, size);
    }

    @Override
    public int countByCondition(String keyword, String category, String companyName) {
        return trademarkMapper.countByCondition(keyword, category, companyName);
    }

    @Override
    public Trademark selectById(Long id) {
        return trademarkMapper.selectById(id);
    }

    @Override
    public int insert(Trademark trademark) {
        return trademarkMapper.insert(trademark);
    }

    @Override
    public int update(Trademark trademark) {
        return trademarkMapper.update(trademark);
    }

    @Override
    public int deleteById(Long id) {
        return trademarkMapper.deleteById(id);
    }
}