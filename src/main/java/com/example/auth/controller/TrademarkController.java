package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.Trademark;
import com.example.auth.mapper.TrademarkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商标管理
 */
@RestController
@RequestMapping("/api/trademarks")
@CrossOrigin
public class TrademarkController {

    @Autowired
    private TrademarkMapper trademarkMapper;

    @GetMapping
    public Result<PageResult<Trademark>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String companyName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Trademark> list = trademarkMapper.selectByCondition(keyword, category, companyName, offset, size);
        int total = trademarkMapper.countByCondition(keyword, category, companyName);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<Trademark> getById(@PathVariable Long id) {
        Trademark trademark = trademarkMapper.selectById(id);
        if (trademark == null) return Result.fail("商标不存在");
        return Result.ok(trademark);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Trademark trademark) {
        if (trademark.getTrademarkName() == null || trademark.getTrademarkName().trim().isEmpty()) {
            return Result.fail("商标名称不能为空");
        }
        trademarkMapper.insert(trademark);
        Map<String, Object> data = new HashMap<>();
        data.put("id", trademark.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Trademark trademark) {
        Trademark old = trademarkMapper.selectById(id);
        if (old == null) return Result.fail("商标不存在");
        trademark.setId(id);
        trademarkMapper.update(trademark);
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trademarkMapper.deleteById(id);
        return Result.ok(null);
    }
}
