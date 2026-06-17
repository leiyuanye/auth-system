package com.example.auth.service;

import com.example.auth.entity.Dict;

import java.util.List;

public interface DictService {
    List<Dict> selectByType(String dictType);
    List<Dict> selectAll();
    Dict selectById(Long id);
    int insert(Dict dict);
    int update(Dict dict);
    int deleteById(Long id);
}