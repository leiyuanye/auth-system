package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.PhoneRealname;
import com.example.auth.mapper.PhoneRealnameMapper;
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
@RequestMapping("/api/phone/realnames")
@CrossOrigin
public class PhoneRealnameController {

    @Autowired
    private PhoneRealnameMapper realnameMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "实名人员管理";

    private static final String[] EXPORT_HEADERS = {
            "ID", "真实姓名", "同事状态", "同事姓名", "扫脸便捷性", "备注", "创建时间"
    };
    private static final String[] IMPORT_HEADERS = {
            "真实姓名", "同事状态", "同事姓名", "扫脸便捷性", "备注"
    };

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<PageResult<PhoneRealname>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer scanStatus,
            @RequestParam(required = false) String colleagueStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<PhoneRealname> list = realnameMapper.selectByCondition(keyword, scanStatus, colleagueStatus, offset, size);
        int total = realnameMapper.countByCondition(keyword, scanStatus, colleagueStatus);
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
        if (realname.getColleagueStatus() == null || realname.getColleagueStatus().trim().isEmpty()) {
            realname.setColleagueStatus("active");
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

    // ==================== 导入/导出/模板下载 ====================

    /**
     * 导出实名人员数据为 Excel
     */
    @GetMapping("/export")
    public void exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer scanStatus,
            @RequestParam(required = false) String colleagueStatus,
            HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        List<PhoneRealname> list = realnameMapper.selectByCondition(keyword, scanStatus, colleagueStatus, 0, Integer.MAX_VALUE);
        String filename = "实名人员数据_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        try (Workbook wb = new XSSFWorkbook();
             OutputStream outputStream = response.getOutputStream()) {
            Sheet sheet = wb.createSheet("实名人员数据");
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

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int rowIdx = 1;
            for (PhoneRealname realname : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(realname.getId() != null ? realname.getId() : 0);
                row.createCell(1).setCellValue(realname.getRealName() != null ? realname.getRealName() : "");
                row.createCell(2).setCellValue(colleagueStatusText(realname.getColleagueStatus()));
                row.createCell(3).setCellValue(realname.getColleagueName() != null ? realname.getColleagueName() : "");
                row.createCell(4).setCellValue(scanStatusText(realname.getScanStatus()));
                row.createCell(5).setCellValue(realname.getRemark() != null ? realname.getRemark() : "");
                row.createCell(6).setCellValue(realname.getCreateTime() != null ? sdf.format(realname.getCreateTime()) : "");
            }

            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws java.io.UnsupportedEncodingException {
        String filename = "实名人员导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        try (Workbook wb = new XSSFWorkbook();
             OutputStream outputStream = response.getOutputStream()) {
            Sheet sheet = wb.createSheet("实名人员导入模板");
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
                sheet.setColumnWidth(i, 18 * 256);
            }

            Row exampleRow = sheet.createRow(1);
            String[] exampleValues = {"张三", "在职", "李四", "方便扫脸", "备注"};
            for (int i = 0; i < exampleValues.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleValues[i]);
            }

            wb.write(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 导入实名人员数据
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
            List<PhoneRealname> realnames = new ArrayList<>();

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

                String realName = getCellString(row.getCell(0));
                if (realName == null || realName.trim().isEmpty()) {
                    continue;
                }

                PhoneRealname realname = new PhoneRealname();
                realname.setRealName(realName.trim());
                realname.setColleagueStatus(parseColleagueStatus(getCellString(row.getCell(1))));
                realname.setColleagueName(getCellString(row.getCell(2)));
                realname.setScanStatus(parseScanStatus(getCellString(row.getCell(3))));
                realname.setRemark(getCellString(row.getCell(4)));

                realnames.add(realname);
            }
            wb.close();

            int successCount = 0;
            for (PhoneRealname realname : realnames) {
                try {
                    realnameMapper.insert(realname);
                    logUtil.logAdd(MODULE_NAME, realname.getId(), realname.getRealName(), realname, currentUser(request));
                    successCount++;
                } catch (Exception ignored) {
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("successCount", successCount);
            data.put("failCount", realnames.size() - successCount);
            data.put("total", realnames.size());
            return Result.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("导入失败: " + e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    private String colleagueStatusText(String v) {
        if (v == null) return "-";
        switch (v) {
            case "active": return "在职";
            case "resigned": return "已离职";
            case "other": return "其他";
            default: return v;
        }
    }

    private String scanStatusText(Integer v) {
        if (v == null) return "-";
        switch (v) {
            case 1: return "不能扫脸";
            case 2: return "方便扫脸";
            case 3: return "较难扫脸";
            default: return "-";
        }
    }

    private String parseColleagueStatus(String v) {
        if (v == null) return "active";
        v = v.trim();
        if (v.contains("在职") || "active".equals(v)) return "active";
        if (v.contains("离职") || "resigned".equals(v)) return "resigned";
        if (v.contains("其他") || "other".equals(v)) return "other";
        return "active";
    }

    private Integer parseScanStatus(String v) {
        if (v == null) return 1;
        v = v.trim();
        if (v.contains("不能") || "1".equals(v)) return 1;
        if (v.contains("方便") || "2".equals(v)) return 2;
        if (v.contains("较难") || "3".equals(v)) return 3;
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
                try {
                    DataFormatter fmt = new DataFormatter();
                    String formatted = fmt.formatCellValue(cell);
                    if (formatted != null && !formatted.isEmpty()) {
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
