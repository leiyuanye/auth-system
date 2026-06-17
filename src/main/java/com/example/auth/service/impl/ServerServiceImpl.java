package com.example.auth.service.impl;

import com.example.auth.entity.Server;
import com.example.auth.mapper.ServerMapper;
import com.example.auth.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    private ServerMapper serverMapper;

    @Override
    public List<Server> selectByCondition(String keyword, Integer serverStatus, String expireSort, int offset, int size) {
        return serverMapper.selectByCondition(keyword, serverStatus, expireSort, offset, size);
    }

    @Override
    public int countByCondition(String keyword, Integer serverStatus) {
        return serverMapper.countByCondition(keyword, serverStatus);
    }

    @Override
    public Server selectById(Long id) {
        return serverMapper.selectById(id);
    }

    @Override
    public List<Server> selectAllForExport() {
        return serverMapper.selectAllForExport();
    }

    @Override
    public int insert(Server server) {
        return serverMapper.insert(server);
    }

    @Override
    public int update(Server server) {
        return serverMapper.update(server);
    }

    @Override
    public int deleteById(Long id) {
        return serverMapper.deleteById(id);
    }

    @Override
    public int batchInsert(List<Server> servers) {
        return serverMapper.batchInsert(servers);
    }
}