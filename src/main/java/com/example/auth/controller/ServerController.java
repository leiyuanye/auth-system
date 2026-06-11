package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.Server;
import com.example.auth.mapper.ServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器管理接口
 * path: /api/server/servers
 */
@RestController
@RequestMapping("/api/server/servers")
@CrossOrigin
public class ServerController {

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "服务器管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<PageResult<Server>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer cardType,
            @RequestParam(required = false) Integer serverStatus,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Server> list = serverMapper.selectByCondition(keyword, cardType, serverStatus, stockStatus, offset, size);
        int total = serverMapper.countByCondition(keyword, cardType, serverStatus, stockStatus);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<Server> getById(@PathVariable Long id) {
        Server server = serverMapper.selectById(id);
        return Result.ok(server);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Server server, HttpServletRequest request) {
        if (server.getServerName() == null || server.getServerName().trim().isEmpty()) {
            return Result.fail("服务器名称不能为空");
        }
        if (server.getCardType() == null) server.setCardType(1);
        int rows = serverMapper.insert(server);
        logUtil.logAdd(MODULE_NAME, server.getId(), server.getServerName(), server, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", server.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Server server, HttpServletRequest request) {
        Server oldServer = serverMapper.selectById(id);
        server.setId(id);
        int rows = serverMapper.update(server);
        Server newServer = serverMapper.selectById(id);
        logUtil.logUpdate(
                MODULE_NAME, id,
                server.getServerName() != null ? server.getServerName() : (oldServer != null ? oldServer.getServerName() : null),
                oldServer, newServer, currentUser(request)
        );
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Server oldServer = serverMapper.selectById(id);
        int rows = serverMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldServer != null ? oldServer.getServerName() : String.valueOf(id), oldServer, currentUser(request));
        return Result.ok(null);
    }
}
