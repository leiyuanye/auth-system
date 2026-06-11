package com.example.auth.controller;

import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneRealname;
import com.example.auth.mapper.PhoneRealnameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phone/realnames")
@CrossOrigin
public class PhoneRealnameController {

    @Autowired
    private PhoneRealnameMapper realnameMapper;

    /**
     * 分页查询实名人员列表
     * GET /api/phone/realnames?keyword=&scanStatus=&page=1&size=10
     */
    @GetMapping
    public Result<PageResult<PhoneRealname>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer scanStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<PhoneRealname> list = realnameMapper.selectByCondition(keyword, scanStatus, offset, size);
        int total = realnameMapper.countByCondition(keyword, scanStatus);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    /**
     * 查询单个实名人员详情
     */
    @GetMapping("/{id}")
    public Result<PhoneRealname> getById(@PathVariable Long id) {
        PhoneRealname realname = realnameMapper.selectById(id);
        return Result.ok(realname);
    }

    /**
     * 新增实名人员
     */
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody PhoneRealname realname) {
        if (realname.getRealName() == null || realname.getRealName().trim().isEmpty()) {
            return Result.fail("真实姓名不能为空");
        }
        if (realname.getScanStatus() == null) {
            realname.setScanStatus(1);
        }
        realnameMapper.insert(realname);
        Map<String, Object> data = new HashMap<>();
        data.put("id", realname.getId());
        return Result.ok(data);
    }

    /**
     * 更新实名人员
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PhoneRealname realname) {
        realname.setId(id);
        realnameMapper.update(realname);
        return Result.ok(null);
    }

    /**
     * 删除实名人员
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        realnameMapper.deleteById(id);
        return Result.ok(null);
    }
}
