package com.example.auth.service.impl;

import com.example.auth.entity.Dict;
import com.example.auth.mapper.DictMapper;
import com.example.auth.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private DictMapper dictMapper;

    @Override
    public List<Dict> selectByType(String dictType) {
        return dictMapper.selectByType(dictType);
    }

    @Override
    public int insert(Dict dict) {
        return dictMapper.insert(dict);
    }

    @Override
    public int update(Dict dict) {
        return dictMapper.update(dict);
    }

    @Override
    public int deleteById(Long id) {
        return dictMapper.deleteById(id);
    }
}