package com.example.auth.service;

import com.example.auth.entity.Server;

import java.util.List;

public interface ServerService {
    List<Server> selectByCondition(String keyword, Integer serverStatus, String expireSort, int offset, int size);
    int countByCondition(String keyword, Integer serverStatus);
    Server selectById(Long id);
    List<Server> selectAllForExport();
    int insert(Server server);
    int update(Server server);
    int deleteById(Long id);
    int batchInsert(List<Server> servers);
}