package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneCard;
import com.example.auth.mapper.PhoneCardMapper;
import com.example.auth.utils.PhoneCardExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            @RequestParam(required = false) Integer operatorType,
            @RequestParam(required = false) Integer usageStatus,
            @RequestParam(required = false) Integer cardStatus,
            @RequestParam(required = false) String groupBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<PhoneCard> list = cardMapper.selectByCondition(keyword, usageStatus, cardStatus, operatorType, groupBy, offset, size);
        int total = cardMapper.countByCondition(keyword, usageStatus, cardStatus, operatorType);
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

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        PhoneCardExcelUtil.generateTemplate(response);
    }

    /**
     * 导出手机卡数据
     */
    @GetMapping("/export")
    public void exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer operatorType,
            @RequestParam(required = false) Integer usageStatus,
            @RequestParam(required = false) Integer cardStatus,
            HttpServletResponse response) throws Exception {
        List<PhoneCard> list = cardMapper.selectByCondition(keyword, usageStatus, cardStatus, operatorType, null, 0, Integer.MAX_VALUE);
        PhoneCardExcelUtil.exportExcel(list, response);
    }

    /**
     * 导入手机卡数据
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return Result.fail("请选择要导入的文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.fail("请上传Excel文件(.xlsx或.xls格式)");
        }
        try {
            List<PhoneCard> cards = PhoneCardExcelUtil.importExcel(file.getInputStream());
            if (cards.isEmpty()) {
                return Result.fail("文件中没有有效数据");
            }
            int successCount = 0;
            int failCount = 0;
            StringBuilder failMsg = new StringBuilder();
            for (PhoneCard card : cards) {
                try {
                    if (card.getCardStatus() == null) card.setCardStatus(1);
                    if (card.getUsageStatus() == null) card.setUsageStatus(1);
                    cardMapper.insert(card);
                    logUtil.logAdd(MODULE_NAME, card.getId(), card.getCardNumber(), card, currentUser(request));
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    if (failMsg.length() < 200) {
                        failMsg.append("卡号[").append(card.getCardNumber()).append("]导入失败: ").append(e.getMessage()).append("; ");
                    }
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("successCount", successCount);
            data.put("failCount", failCount);
            data.put("total", cards.size());
            if (failCount > 0) {
                data.put("message", failMsg.toString());
            }
            return Result.ok(data);
        } catch (Exception e) {
            return Result.fail("导入失败: " + e.getMessage());
        }
    }
}
