package com.example.auth.utils;

import com.example.auth.entity.PhoneCard;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 手机卡Excel导入导出工具类
 */
public class PhoneCardExcelUtil {

    private static final String[] HEADERS = {"ICCID", "运营商", "使用状态", "状态", "代理商", "手机号", "实名人", "备注"};

    /**
     * 生成Excel模板
     */
    public static void generateTemplate(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("手机卡导入模板");

            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // 添加示例数据行
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("89860012345678901234");
            exampleRow.createCell(1).setCellValue("移动");
            exampleRow.createCell(2).setCellValue("在用");
            exampleRow.createCell(3).setCellValue("正常");
            exampleRow.createCell(4).setCellValue("XX科技有限公司");
            exampleRow.createCell(5).setCellValue("13800138000");
            exampleRow.createCell(6).setCellValue("张三");
            exampleRow.createCell(7).setCellValue("备注信息");

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("手机卡导入模板.xlsx", "UTF-8"));
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 导出手机卡数据到Excel
     */
    public static void exportExcel(List<PhoneCard> cards, HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("手机卡数据");

            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建日期样式
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper creationHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] exportHeaders = {"ID", "ICCID", "运营商", "使用状态", "状态", "代理商", "手机号", "实名人", "备注", "创建时间", "更新时间"};
            for (int i = 0; i < exportHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(exportHeaders[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 18 * 256);
            }

            // 填充数据
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < cards.size(); i++) {
                PhoneCard card = cards.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(card.getId() != null ? card.getId() : 0);
                row.createCell(1).setCellValue(card.getIccd() != null ? card.getIccd() : "");
                row.createCell(2).setCellValue(getOperatorTypeText(card.getOperatorType()));
                row.createCell(3).setCellValue(getUsageStatusText(card.getUsageStatus()));
                row.createCell(4).setCellValue(getCardStatusText(card.getCardStatus()));
                row.createCell(5).setCellValue(card.getAgentName() != null ? card.getAgentName() : "");
                row.createCell(6).setCellValue(card.getPhoneNumber() != null ? card.getPhoneNumber() : "");
                row.createCell(7).setCellValue(card.getRealnameName() != null ? card.getRealnameName() : "");
                row.createCell(8).setCellValue(card.getRemark() != null ? card.getRemark() : "");
                row.createCell(9).setCellValue(card.getCreateTime() != null ? sdf.format(card.getCreateTime()) : "");
                row.createCell(10).setCellValue(card.getUpdateTime() != null ? sdf.format(card.getUpdateTime()) : "");
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "手机卡数据_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 从Excel导入手机卡数据
     */
    public static List<PhoneCard> importExcel(InputStream inputStream) throws IOException {
        List<PhoneCard> cards = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String iccd = getCellValue(row.getCell(0));
                if (iccd == null || iccd.trim().isEmpty()) continue;

                PhoneCard card = new PhoneCard();
                card.setIccd(iccd.trim());
                card.setOperatorType(parseOperatorType(getCellValue(row.getCell(1))));
                card.setUsageStatus(parseUsageStatus(getCellValue(row.getCell(2))));
                card.setCardStatus(parseCardStatus(getCellValue(row.getCell(3))));
                card.setAgentName(getCellValue(row.getCell(4)));
                card.setPhoneNumber(getCellValue(row.getCell(5)));
                card.setRealnameName(getCellValue(row.getCell(6)));
                card.setRemark(getCellValue(row.getCell(7)));

                cards.add(card);
            }
        }
        return cards;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    long longVal = (long) num;
                    if (longVal <= 9_000_000_000_000_000_000L && longVal >= 0) {
                        return String.valueOf(longVal);
                    }
                }
                return new java.math.BigDecimal(String.valueOf(num)).toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    CellValue val = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator().evaluate(cell);
                    if (val != null && val.getCellType() == CellType.NUMERIC) {
                        double fNum = val.getNumberValue();
                        if (fNum == Math.floor(fNum) && !Double.isInfinite(fNum)) {
                            long fLong = (long) fNum;
                            if (fLong <= 9_000_000_000_000_000_000L && fLong >= 0) {
                                return String.valueOf(fLong);
                            }
                        }
                        return new java.math.BigDecimal(String.valueOf(fNum)).toPlainString();
                    }
                    if (val != null) return val.getStringValue();
                    return cell.getCellFormula();
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return null;
        }
    }

    private static Integer parseOperatorType(String value) {
        if (value == null) return 1;
        value = value.trim();
        if (value.contains("移动") || value.equals("1")) return 1;
        if (value.contains("联通") || value.equals("2")) return 2;
        if (value.contains("电信") || value.equals("3")) return 3;
        if (value.contains("其他") || value.equals("4")) return 4;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static Integer parseUsageStatus(String value) {
        if (value == null) return 1;
        value = value.trim();
        if (value.contains("在用") || value.equals("1")) return 1;
        if (value.contains("备用") || value.equals("2")) return 2;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static Integer parseCardStatus(String value) {
        if (value == null) return 1;
        value = value.trim();
        if (value.contains("正常") || value.equals("1")) return 1;
        if (value.contains("二次实名") || value.equals("2")) return 2;
        if (value.contains("欠费") || value.equals("3")) return 3;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static String getOperatorTypeText(Integer value) {
        if (value == null) return "-";
        switch (value) {
            case 1: return "移动";
            case 2: return "联通";
            case 3: return "电信";
            case 4: return "其他";
            default: return "-";
        }
    }

    private static String getUsageStatusText(Integer value) {
        if (value == null) return "-";
        switch (value) {
            case 1: return "在用";
            case 2: return "备用";
            default: return "-";
        }
    }

    private static String getCardStatusText(Integer value) {
        if (value == null) return "-";
        switch (value) {
            case 1: return "正常";
            case 2: return "二次实名";
            case 3: return "欠费";
            default: return "-";
        }
    }
}
