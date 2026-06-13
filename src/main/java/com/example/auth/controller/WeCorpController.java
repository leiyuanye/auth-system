package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.WeCorp;
import com.example.auth.mapper.WeCorpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 企微主体管理
 */
@RestController
@RequestMapping("/api/wecorps")
@CrossOrigin
public class WeCorpController {

    @Autowired
    private WeCorpMapper weCorpMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "企微主体管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    private static List<String> splitList(String csv) {
        if (csv == null || csv.trim().isEmpty()) return null;
        List<String> list = new ArrayList<>();
        for (String s : csv.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list.isEmpty() ? null : list;
    }

    @GetMapping
    public Result<PageResult<WeCorp>> list(
            @RequestParam(required = false) String subjectShorts,
            @RequestParam(required = false) String customerTypes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<String> shorts = splitList(subjectShorts);
        List<String> ct = splitList(customerTypes);
        List<WeCorp> list = weCorpMapper.selectByCondition(shorts, ct, keyword, offset, size);
        int total = weCorpMapper.countByCondition(shorts, ct, keyword);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<WeCorp> getById(@PathVariable Long id) {
        return Result.ok(weCorpMapper.selectById(id));
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody WeCorp corp, HttpServletRequest request) {
        if (corp.getSubjectShort() == null || corp.getSubjectShort().trim().isEmpty()) {
            return Result.fail("主体简称不能为空");
        }
        weCorpMapper.insert(corp);
        logUtil.logAdd(MODULE_NAME, corp.getId(), corp.getSubjectShort(), corp, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", corp.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WeCorp corp, HttpServletRequest request) {
        WeCorp old = weCorpMapper.selectById(id);
        corp.setId(id);
        weCorpMapper.update(corp);
        WeCorp newOne = weCorpMapper.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id,
                corp.getSubjectShort() != null ? corp.getSubjectShort() : (old != null ? old.getSubjectShort() : null),
                old, newOne, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        WeCorp old = weCorpMapper.selectById(id);
        weCorpMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, old != null ? old.getSubjectShort() : String.valueOf(id), old, currentUser(request));
        return Result.ok(null);
    }
}
