package com.example.auth.service.impl;

import com.example.auth.entity.PhoneCard;
import com.example.auth.mapper.PhoneCardMapper;
import com.example.auth.service.PhoneCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhoneCardServiceImpl implements PhoneCardService {

    @Autowired
    private PhoneCardMapper cardMapper;

    @Override
    public List<PhoneCard> selectByCondition(String keyword, Integer usageStatus, Integer cardStatus, Integer operatorType, String groupBy, int offset, int size) {
        return cardMapper.selectByCondition(keyword, usageStatus, cardStatus, operatorType, groupBy, offset, size);
    }

    @Override
    public int countByCondition(String keyword, Integer usageStatus, Integer cardStatus, Integer operatorType) {
        return cardMapper.countByCondition(keyword, usageStatus, cardStatus, operatorType);
    }

    @Override
    public PhoneCard selectById(Long id) {
        return cardMapper.selectById(id);
    }

    @Override
    public int insert(PhoneCard card) {
        return cardMapper.insert(card);
    }

    @Override
    public int update(PhoneCard card) {
        return cardMapper.update(card);
    }

    @Override
    public int deleteById(Long id) {
        return cardMapper.deleteById(id);
    }
}