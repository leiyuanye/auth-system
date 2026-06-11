package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneRealname;
import com.example.auth.mapper.PhoneRealnameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phone/realnames")
@CrossOrigin
public class PhoneRealnameController {

    @Autowired
    private PhoneRealnameMapper realnameMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "实名人员管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

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

    @GetMapping("/{id}")
    public Result<PhoneRealname> getById(@PathVariable Long id) {
        PhoneRealname realname = realnameMapper.selectById(id);
        return Result.ok(realname);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody PhoneRealname realname, HttpServletRequest request) {
        if (realname.getRealName() == null || realname.getRealName().trim().isEmpty()) {
            return Result.fail("真实姓名不能为空");
        }
        if (realname.getScanStatus() == null) {
            realname.setScanStatus(1);
        }
        realnameMapper.insert(realname);
        logUtil.logAdd(MODULE_NAME, realname.getId(), realname.getRealName(), realname, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", realname.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PhoneRealname realname, HttpServletRequest request) {
        PhoneRealname old = realnameMapper.selectById(id);
        realname.setId(id);
        realnameMapper.update(realname);
        PhoneRealname newOne = realnameMapper.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id, realname.getRealName() != null ? realname.getRealName() : (old != null ? old.getRealName() : null), old, newOne, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        PhoneRealname old = realnameMapper.selectById(id);
        realnameMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, old != null ? old.getRealName() : String.valueOf(id), old, currentUser(request));
        return Result.ok(null);
    }
}
