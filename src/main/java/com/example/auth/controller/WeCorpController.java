package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.WeCorp;
import com.example.auth.mapper.WeCorpMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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

    private static final String[] EXPORT_HEADERS = {
            "主体简称", "企业全称", "客户类型", "主体状态", "企业认证到期",
            "规模额度", "已用额度", "外部联系人有效期", "主体创建人", "手机号码",
            "法人姓名", "法人身份证", "法人手机号", "注册资金", "注册日期",
            "经营范围", "注册地址", "备注"
    };

    private static final String[] IMPORT_HEADERS = {
            "主体简称", "企业全称", "客户类型", "主体状态", "企业认证到期",
            "规模额度", "已用额度", "外部联系人有效期", "主体创建人", "手机号码",
            "法人姓名", "法人身份证", "法人手机号", "注册资金", "注册日期",
            "经营范围", "注册地址", "备注"
    };

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
        WeCorp corp = weCorpMapper.selectById(id);
        if (corp == null) return Result.fail("主体不存在");
        return Result.ok(corp);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody WeCorp corp, HttpServletRequest request) {
        if (corp.getSubjectShort() == null || corp.getSubjectShort().trim().isEmpty()) {
            return Result.fail("主体简称不能为空");
        }
        if (corp.getCorpStatus() == null || corp.getCorpStatus().trim().isEmpty()) {
            corp.setCorpStatus("active");
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
        if (old == null) return Result.fail("主体不存在");
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
        logUtil.logDelete(MODULE_NAME, id, old != null ? old.getSubjectShort() : null, old, currentUser(request));
        return Result.ok(null);
    }

    // ==================== 导入/导出/模板下载 ====================

    /**
     * 导出企微主体数据为 Excel
     */
    @GetMapping("/export")
    public StreamingResponseBody exportExcel(
            @RequestParam(required = false) String subjectShorts,
            @RequestParam(required = false) String customerTypes,
            @RequestParam(required = false) String keyword,
            HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        List<String> shorts = splitList(subjectShorts);
        List<String> ct = splitList(customerTypes);
        List<WeCorp> list = weCorpMapper.selectByCondition(shorts, ct, keyword, 0, Integer.MAX_VALUE);
        String filename = "企微主体数据_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        return outputStream -> {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("企微主体数据");
                CellStyle headerStyle = wb.createCellStyle();
                Font headerFont = wb.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(EXPORT_HEADERS[i]);
                    cell.setCellStyle(headerStyle);
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                int rowIdx = 1;
                for (WeCorp corp : list) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(corp.getSubjectShort() != null ? corp.getSubjectShort() : "");
                    row.createCell(1).setCellValue(corp.getSubjectFull() != null ? corp.getSubjectFull() : "");
                    row.createCell(2).setCellValue(corp.getCustomerType() != null ? corp.getCustomerType() : "");
                    row.createCell(3).setCellValue(corp.getCorpStatus() != null ? corp.getCorpStatus() : "");
                    row.createCell(4).setCellValue(corp.getCertExpire() != null ? sdf.format(corp.getCertExpire()) : "");
                    row.createCell(5).setCellValue(corp.getQuotaTotal() != null ? corp.getQuotaTotal() : 0);
                    row.createCell(6).setCellValue(corp.getQuotaUsed() != null ? corp.getQuotaUsed() : 0);
                    row.createCell(7).setCellValue(corp.getContactValidDate() != null ? sdf.format(corp.getContactValidDate()) : "");
                    row.createCell(8).setCellValue(corp.getCreator() != null ? corp.getCreator() : "");
                    row.createCell(9).setCellValue(corp.getPhone() != null ? corp.getPhone() : "");
                    row.createCell(10).setCellValue(corp.getLegalName() != null ? corp.getLegalName() : "");
                    row.createCell(11).setCellValue(corp.getLegalIdCard() != null ? corp.getLegalIdCard() : "");
                    row.createCell(12).setCellValue(corp.getLegalPhone() != null ? corp.getLegalPhone() : "");
                    row.createCell(13).setCellValue(corp.getRegisterCapital() != null ? corp.getRegisterCapital() : "");
                    row.createCell(14).setCellValue(corp.getRegisterDate() != null ? sdf.format(corp.getRegisterDate()) : "");
                    row.createCell(15).setCellValue(corp.getBusinessScope() != null ? corp.getBusinessScope() : "");
                    row.createCell(16).setCellValue(corp.getRegisterAddress() != null ? corp.getRegisterAddress() : "");
                    row.createCell(17).setCellValue(corp.getRemark() != null ? corp.getRemark() : "");
                }

                for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 12 * 256));
                }

                wb.write(outputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public StreamingResponseBody downloadTemplate(HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        String filename = "企微主体导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        return outputStream -> {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("企微主体导入模板");
                CellStyle headerStyle = wb.createCellStyle();
                Font headerFont = wb.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);

                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(IMPORT_HEADERS[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                    sheet.setColumnWidth(i, 16 * 256);
                }

                Row exampleRow = sheet.createRow(1);
                String[] exampleValues = {
                        "主体A", "XX科技有限公司", "企业", "正常", "2025-12-31",
                        "10000", "5000", "2025-12-31", "张三", "13800000000",
                        "李四", "110101199001011234", "13900000000", "100万", "2020-01-01",
                        "技术服务", "北京市朝阳区", "备注信息"
                };
                for (int i = 0; i < exampleValues.length; i++) {
                    exampleRow.createCell(i).setCellValue(exampleValues[i]);
                }

                wb.write(outputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 导入企微主体数据
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
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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

                int line = i + 1;
                try {
                    WeCorp corp = new WeCorp();
                    corp.setSubjectShort(getCellString(row.getCell(0)));
                    corp.setSubjectFull(getCellString(row.getCell(1)));
                    corp.setCustomerType(getCellString(row.getCell(2)));
                    corp.setCorpStatus(getCellString(row.getCell(3)));
                    String certExpireStr = getCellString(row.getCell(4));
                    if (certExpireStr != null && !certExpireStr.trim().isEmpty()) {
                        corp.setCertExpire(new SimpleDateFormat("yyyy-MM-dd").parse(certExpireStr.trim()));
                    }
                    String quotaTotalStr = getCellString(row.getCell(5));
                    corp.setQuotaTotal(quotaTotalStr != null && !quotaTotalStr.trim().isEmpty() ? Integer.parseInt(quotaTotalStr.trim()) : 0);
                    String quotaUsedStr = getCellString(row.getCell(6));
                    corp.setQuotaUsed(quotaUsedStr != null && !quotaUsedStr.trim().isEmpty() ? Integer.parseInt(quotaUsedStr.trim()) : 0);
                    String contactValidStr = getCellString(row.getCell(7));
                    if (contactValidStr != null && !contactValidStr.trim().isEmpty()) {
                        corp.setContactValidDate(new SimpleDateFormat("yyyy-MM-dd").parse(contactValidStr.trim()));
                    }
                    corp.setCreator(getCellString(row.getCell(8)));
                    corp.setPhone(getCellString(row.getCell(9)));
                    corp.setLegalName(getCellString(row.getCell(10)));
                    corp.setLegalIdCard(getCellString(row.getCell(11)));
                    corp.setLegalPhone(getCellString(row.getCell(12)));
                    corp.setRegisterCapital(getCellString(row.getCell(13)));
                    String registerDateStr = getCellString(row.getCell(14));
                    if (registerDateStr != null && !registerDateStr.trim().isEmpty()) {
                        corp.setRegisterDate(new SimpleDateFormat("yyyy-MM-dd").parse(registerDateStr.trim()));
                    }
                    corp.setBusinessScope(getCellString(row.getCell(15)));
                    corp.setRegisterAddress(getCellString(row.getCell(16)));
                    corp.setRemark(getCellString(row.getCell(17)));

                    if (corp.getSubjectShort() == null || corp.getSubjectShort().trim().isEmpty()) {
                        throw new IllegalArgumentException("主体简称不能为空");
                    }
                    if (corp.getCorpStatus() == null || corp.getCorpStatus().trim().isEmpty()) {
                        corp.setCorpStatus("active");
                    }

                    weCorpMapper.insert(corp);
                    logUtil.logAdd(MODULE_NAME, corp.getId(), corp.getSubjectShort(), corp, currentUser(request));
                    successCount++;
                } catch (Exception ex) {
                    failCount++;
                    errors.add("第" + line + "行：" + ex.getMessage());
                }
            }
        } catch (Exception e) {
            return Result.fail("导入失败: " + e.getMessage());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        data.put("failCount", failCount);
        data.put("total", successCount + failCount);
        data.put("message", String.join("；", errors));
        return Result.ok(data);
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
