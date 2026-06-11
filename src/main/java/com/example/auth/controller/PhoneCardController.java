package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneCard;
import com.example.auth.mapper.PhoneCardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phone/cards")
@CrossOrigin
public class PhoneCardController {

    @Autowired
    private PhoneCardMapper cardMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "手机卡管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<PageResult<PhoneCard>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer cardType,
            @RequestParam(required = false) Integer cardStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<PhoneCard> list = cardMapper.selectByCondition(keyword, cardType, cardStatus, offset, size);
        int total = cardMapper.countByCondition(keyword, cardType, cardStatus);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<PhoneCard> getById(@PathVariable Long id) {
        PhoneCard card = cardMapper.selectById(id);
        return Result.ok(card);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody PhoneCard card, HttpServletRequest request) {
        if (card.getCardNumber() == null || card.getCardNumber().trim().isEmpty()) {
            return Result.fail("卡号不能为空");
        }
        if (card.getCardStatus() == null) card.setCardStatus(1);
        if (card.getCardType() == null) card.setCardType(1);
        int rows = cardMapper.insert(card);
        logUtil.logAdd(MODULE_NAME, card.getId(), card.getCardNumber(), card, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", card.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PhoneCard card, HttpServletRequest request) {
        PhoneCard oldCard = cardMapper.selectById(id);
        card.setId(id);
        int rows = cardMapper.update(card);
        PhoneCard newCard = cardMapper.selectById(id);
        logUtil.logUpdate(MODULE_NAME, id, card.getCardNumber() != null ? card.getCardNumber() : (oldCard != null ? oldCard.getCardNumber() : null), oldCard, newCard, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        PhoneCard oldCard = cardMapper.selectById(id);
        int rows = cardMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldCard != null ? oldCard.getCardNumber() : String.valueOf(id), oldCard, currentUser(request));
        return Result.ok(null);
    }
}
