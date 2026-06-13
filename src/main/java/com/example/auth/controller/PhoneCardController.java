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
        if (card.getIccd() == null || card.getIccd().trim().isEmpty()) {
            return Result.fail("ICCID不能为空");
        }
        if (card.getCardStatus() == null) card.setCardStatus(1);
        int rows = cardMapper.insert(card);
        logUtil.logAdd(MODULE_NAME, card.getId(), card.getIccd(), card, currentUser(request));
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
        logUtil.logUpdate(MODULE_NAME, id, card.getIccd() != null ? card.getIccd() : (oldCard != null ? oldCard.getIccd() : null), oldCard, newCard, currentUser(request));
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        PhoneCard oldCard = cardMapper.selectById(id);
        int rows = cardMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldCard != null ? oldCard.getIccd() : String.valueOf(id), oldCard, currentUser(request));
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
                return Result.fail("文件中没有有效数据(请确认ICCID列有值)");
            }
            int successCount = 0;
            int failCount = 0;
            StringBuilder failMsg = new StringBuilder();
            java.util.Set<String> seenIccids = new java.util.HashSet<>();
            java.util.List<String> duplicateInFile = new java.util.ArrayList<>();
            java.util.List<String> duplicateInDb = new java.util.ArrayList<>();
            java.util.List<String> otherErrors = new java.util.ArrayList<>();

            for (PhoneCard card : cards) {
                String iccd = card.getIccd();
                if (iccd == null || iccd.trim().isEmpty()) continue;
                iccd = iccd.trim();
                card.setIccd(iccd);

                // 1. 先检测Excel内部的重复ICCID
                if (seenIccids.contains(iccd)) {
                    duplicateInFile.add(iccd);
                    failCount++;
                    continue;
                }
                seenIccids.add(iccd);

                try {
                    if (card.getCardStatus() == null) card.setCardStatus(1);
                    if (card.getUsageStatus() == null) card.setUsageStatus(1);
                    cardMapper.insert(card);
                    logUtil.logAdd(MODULE_NAME, card.getId(), card.getIccd(), card, currentUser(request));
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    String msg = e.getMessage() == null ? "未知错误" : e.getMessage();
                    // 判断是否为唯一键冲突(数据库已存在相同ICCID)
                    boolean isDupInDb = msg.contains("Duplicate") || msg.contains("duplicate") || msg.contains("uk_iccd") || msg.contains("UNIQUE");
                    if (isDupInDb) {
                        duplicateInDb.add(iccd);
                    } else {
                        otherErrors.add(iccd + "(" + msg + ")");
                    }
                }
            }

            // 组织详细报告
            if (failCount > 0) {
                if (!duplicateInFile.isEmpty()) {
                    failMsg.append("文件内重复ICCID[").append(duplicateInFile.size()).append("]条:");
                    failMsg.append(String.join(", ", duplicateInFile.subList(0, Math.min(5, duplicateInFile.size()))));
                    if (duplicateInFile.size() > 5) failMsg.append("...");
                    failMsg.append("; ");
                }
                if (!duplicateInDb.isEmpty()) {
                    failMsg.append("数据库已存在[").append(duplicateInDb.size()).append("]条:");
                    failMsg.append(String.join(", ", duplicateInDb.subList(0, Math.min(5, duplicateInDb.size()))));
                    if (duplicateInDb.size() > 5) failMsg.append("...");
                    failMsg.append("; ");
                }
                if (!otherErrors.isEmpty()) {
                    failMsg.append("其它失败[").append(otherErrors.size()).append("]条:");
                    failMsg.append(String.join("; ", otherErrors.subList(0, Math.min(3, otherErrors.size()))));
                    if (otherErrors.size() > 3) failMsg.append("...");
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
