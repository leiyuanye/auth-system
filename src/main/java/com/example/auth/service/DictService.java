package com.example.auth.service;

import com.example.auth.entity.Dict;

import java.util.List;

public interface DictService {
    List<Dict> selectByType(String dictType);
    int insert(Dict dict);
    int update(Dict dict);
    int deleteById(Long id);
}