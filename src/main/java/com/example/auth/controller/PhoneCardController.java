package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneCard;
import com.example.auth.mapper.PhoneCardMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/phone/cards")
@CrossOrigin
public class PhoneCardController {

    @Autowired
    private PhoneCardMapper cardMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "手机卡管理";

    private static final String[] EXPORT_HEADERS = {
            // "ICCID",
            "运营商", "使用状态", "卡状态", "代理商", "手机号", "实名人", "备注", "创建时间", "更新时间"
    };
    private static final String[] IMPORT_HEADERS = {
            "ICCID", "运营商", "使用状态", "卡状态", "代理商", "手机号", "实名人", "备注"
    };

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
        if (card.getRealnameName() == null || card.getRealnameName().trim().isEmpty()) {
            return Result.fail("实名人不能为空");
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

    // ==================== 导入/导出/模板下载 ====================

    /**
     * 导出手机卡数据为 Excel
     */
    @GetMapping("/export")
    public void exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer operatorType,
            @RequestParam(required = false) Integer usageStatus,
            @RequestParam(required = false) Integer cardStatus,
            HttpServletResponse response) {
        try {
            List<PhoneCard> list = cardMapper.selectByCondition(keyword, usageStatus, cardStatus, operatorType, null, 0, Integer.MAX_VALUE);
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("手机卡数据");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowIdx = 1;
            for (PhoneCard card : list) {
                Row row = sheet.createRow(rowIdx++);
                // row.createCell(0).setCellValue(card.getIccd() != null ? card.getIccd() : "");
                row.createCell(0).setCellValue(operatorText(card.getOperatorType()));
                row.createCell(1).setCellValue(usageText(card.getUsageStatus()));
                row.createCell(2).setCellValue(cardStatusText(card.getCardStatus()));
                row.createCell(3).setCellValue(card.getAgentName() != null ? card.getAgentName() : "");
                row.createCell(4).setCellValue(card.getPhoneNumber() != null ? card.getPhoneNumber() : "");
                row.createCell(5).setCellValue(card.getRealnameName() != null ? card.getRealnameName() : "");
                row.createCell(6).setCellValue(card.getRemark() != null ? card.getRemark() : "");
                row.createCell(7).setCellValue(card.getCreateTime() != null ? sdf.format(card.getCreateTime()) : "");
                row.createCell(8).setCellValue(card.getUpdateTime() != null ? sdf.format(card.getUpdateTime()) : "");
            }

            // 自动列宽
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            String filename = "手机卡数据_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.flush();
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("手机卡导入模板");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // 表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(IMPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ICCID 列设置为文本格式，避免长数字精度丢失
            CellStyle textStyle = wb.createCellStyle();
            DataFormat dataFormat = wb.createDataFormat();
            textStyle.setDataFormat(dataFormat.getFormat("@"));
            sheet.setDefaultColumnStyle(0, textStyle);
            sheet.setColumnWidth(0, 22 * 256);

            for (int i = 1; i < IMPORT_HEADERS.length; i++) {
                sheet.setColumnWidth(i, 16 * 256);
            }

            // 示例行
            Row exampleRow = sheet.createRow(1);
            String[] exampleValues = {"89860012345678901234", "移动", "在用", "正常", "XX科技有限公司", "13800000000", "张三", "备注"};
            for (int i = 0; i < exampleValues.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleValues[i]);
            }

            String filename = "手机卡导入模板.xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            OutputStream out = response.getOutputStream();
            wb.write(out);
            out.flush();
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导入手机卡数据
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要导入的文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.fail("仅支持 .xlsx 或 .xls 格式的 Excel 文件");
        }
        try {
            Workbook wb = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = wb.getSheetAt(0);
            int startRow = 1;
            List<PhoneCard> cards = new ArrayList<>();
            Set<String> seenIccd = new HashSet<>();
            List<String> duplicates = new ArrayList<>();

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                boolean rowEmpty = true;
                for (int c = 0; c < IMPORT_HEADERS.length; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        String v = getCellString(cell);
                        if (v != null && !v.trim().isEmpty()) {
                            rowEmpty = false;
                            break;
                        }
                    }
                }
                if (rowEmpty) continue;

                String iccd = getCellString(row.getCell(0));
                if (iccd == null || iccd.trim().isEmpty()) continue;
                iccd = iccd.trim();
                if (seenIccd.contains(iccd)) {
                    duplicates.add(iccd);
                    continue;
                }
                seenIccd.add(iccd);

                PhoneCard card = new PhoneCard();
                card.setIccd(iccd);
                card.setOperatorType(parseOperator(getCellString(row.getCell(1))));
                card.setUsageStatus(parseUsageStatus(getCellString(row.getCell(2))));
                card.setCardStatus(parseCardStatus(getCellString(row.getCell(3))));
                card.setAgentName(getCellString(row.getCell(4)));
                card.setPhoneNumber(getCellString(row.getCell(5)));
                String realname = getCellString(row.getCell(6));
                if (realname == null || realname.trim().isEmpty()) {
                    // 实名人必填，缺失则跳过
                    continue;
                }
                card.setRealnameName(realname.trim());
                card.setRemark(getCellString(row.getCell(7)));

                cards.add(card);
            }
            wb.close();

            int successCount = 0;
            for (PhoneCard card : cards) {
                try {
                    cardMapper.insert(card);
                    logUtil.logAdd(MODULE_NAME, card.getId(), card.getIccd(), card, currentUser(request));
                    successCount++;
                } catch (Exception ignored) {
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("successCount", successCount);
            data.put("failCount", cards.size() - successCount + duplicates.size());
            data.put("total", cards.size() + duplicates.size());
            return Result.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("导入失败: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private String operatorText(Integer v) {
        if (v == null) return "-";
        switch (v) {
            case 1: return "移动";
            case 2: return "联通";
            case 3: return "电信";
            case 4: return "其他";
            default: return "-";
        }
    }

    private String usageText(Integer v) {
        if (v == null) return "-";
        switch (v) {
            case 1: return "在用";
            case 2: return "备用";
            default: return "-";
        }
    }

    private String cardStatusText(Integer v) {
        if (v == null) return "-";
        switch (v) {
            case 1: return "正常";
            case 2: return "二次实名";
            case 3: return "欠费";
            default: return "-";
        }
    }

    private Integer parseOperator(String v) {
        if (v == null) return 1;
        v = v.trim();
        if (v.contains("移动") || "1".equals(v)) return 1;
        if (v.contains("联通") || "2".equals(v)) return 2;
        if (v.contains("电信") || "3".equals(v)) return 3;
        if (v.contains("其他") || "4".equals(v)) return 4;
        return 1;
    }

    private Integer parseUsageStatus(String v) {
        if (v == null) return 1;
        v = v.trim();
        if (v.contains("在用") || "1".equals(v)) return 1;
        if (v.contains("备用") || "2".equals(v)) return 2;
        return 1;
    }

    private Integer parseCardStatus(String v) {
        if (v == null) return 1;
        v = v.trim();
        if (v.contains("正常") || "1".equals(v)) return 1;
        if (v.contains("二次实名") || "2".equals(v)) return 2;
        if (v.contains("欠费") || "3".equals(v)) return 3;
        return 1;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return sdf.format(cell.getDateCellValue());
                }
                // 长数字（如 ICCID）避免科学计数法/精度丢失：先用 DataFormatter 读取
                try {
                    DataFormatter fmt = new DataFormatter();
                    String formatted = fmt.formatCellValue(cell);
                    if (formatted != null && !formatted.isEmpty()) {
                        // 若含小数点（如 ".0"），清理；否则直接返回
                        if (formatted.matches("\\d+\\.0*")) {
                            formatted = formatted.substring(0, formatted.indexOf('.'));
                        }
                        return formatted.replace(",", "").trim();
                    }
                } catch (Exception ignored) {}
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); } catch (Exception e) { return String.valueOf((long) cell.getNumericCellValue()); }
            default: return null;
        }
    }
}
