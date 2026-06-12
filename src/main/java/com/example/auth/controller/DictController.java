package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.entity.Dict;
import com.example.auth.mapper.DictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典接口
 * path: /api/dict
 */
@RestController
@RequestMapping("/api/dict")
@CrossOrigin
public class DictController {

    @Autowired
    private DictMapper dictMapper;

    /**
     * 按类型查询字典项列表
     * @param type server_type / server_group / server_status / stock_status
     */
    @GetMapping("/type/{type}")
    public Result<List<Dict>> getByType(@PathVariable String type) {
        List<Dict> list = dictMapper.selectByType(type);
        return Result.ok(list);
    }

    /**
     * 新增字典项
     */
    @PostMapping
    public Result<Void> add(@RequestBody Dict dict) {
        dictMapper.insert(dict);
        return Result.ok(null);
    }

    /**
     * 更新字典项
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Dict dict) {
        dict.setId(id);
        dictMapper.update(dict);
        return Result.ok(null);
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictMapper.deleteById(id);
        return Result.ok(null);
    }
}
