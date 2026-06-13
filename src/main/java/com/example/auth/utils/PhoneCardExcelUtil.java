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

            // 将ICCID列设置为文本格式，避免Excel将长数字转为科学计数法丢失精度
            CellStyle textStyle = workbook.createCellStyle();
            DataFormat textFormat = workbook.createDataFormat();
            textStyle.setDataFormat(textFormat.getFormat("@"));
            for (int i = 1; i <= 2000; i++) {
                Row row = sheet.getRow(i);
                if (row == null) row = sheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellStyle(textStyle);
            }

            // 添加示例数据行(以文本形式写入，避免精度丢失)
            Row exampleRow = sheet.getRow(1);
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

            // 创建DataFormatter:按单元格显示格式转为文本,避免长数字精度丢失
            DataFormatter formatter = new DataFormatter();
            // 创建公式求值器(用于计算公式单元格的值)
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 优先用DataFormatter读取,确保ICCID等长数字不丢失精度
                String iccd = safeGetCellValue(row.getCell(0), formatter, evaluator);
                if (iccd == null || iccd.trim().isEmpty()) continue;
                iccd = iccd.trim();

                PhoneCard card = new PhoneCard();
                card.setIccd(iccd);
                card.setOperatorType(parseOperatorType(safeGetCellValue(row.getCell(1), formatter, evaluator)));
                card.setUsageStatus(parseUsageStatus(safeGetCellValue(row.getCell(2), formatter, evaluator)));
                card.setCardStatus(parseCardStatus(safeGetCellValue(row.getCell(3), formatter, evaluator)));
                card.setAgentName(safeGetCellValue(row.getCell(4), formatter, evaluator));
                card.setPhoneNumber(safeGetCellValue(row.getCell(5), formatter, evaluator));
                card.setRealnameName(safeGetCellValue(row.getCell(6), formatter, evaluator));
                card.setRemark(safeGetCellValue(row.getCell(7), formatter, evaluator));

                cards.add(card);
            }
        }
        return cards;
    }

    /**
     * 统一按"文本"方式读取单元格值,避免ICCID这类长数字被精度丢失
     * - 若单元格本身是STRING类型,直接取值(最可靠)
     * - 若单元格是NUMERIC类型,先按单元格格式化文本获取,科学计数法再转成纯数字
     * - 公式类型:先求值再按上述规则处理
     */
    private static String safeGetCellValue(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) return null;
        try {
            CellType cellType = cell.getCellType();

            // 公式单元格:先求结果类型再处理
            if (cellType == CellType.FORMULA) {
                try {
                    CellValue cv = evaluator.evaluate(cell);
                    if (cv != null) {
                        cellType = cv.getCellType();
                    }
                } catch (Exception ignored) {}
            }

            String value;
            if (cellType == CellType.STRING) {
                // 文本类型:直接取(最可靠,ICCID若存为文本就能完整获取)
                value = cell.getStringCellValue();
            } else if (cellType == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                } else {
                    // 先用formatter获取"显示格式"(可能是8.986E+19这种科学计数法)
                    String formatted = formatter.formatCellValue(cell, evaluator);
                    // 如果包含科学计数法,用BigDecimal转成普通数字文本(但末尾精度可能因double而丢失)
                    if (formatted != null && (formatted.contains("E") || formatted.contains("e"))) {
                        try {
                            value = new java.math.BigDecimal(formatted).toPlainString();
                        } catch (NumberFormatException e) {
                            value = formatted;
                        }
                    } else {
                        value = formatted;
                    }
                    // 回退:若formatter没取到,用cell.getNumericCellValue()+BigDecimal兜底
                    if (value == null || value.trim().isEmpty()) {
                        value = new java.math.BigDecimal(String.valueOf(cell.getNumericCellValue())).toPlainString();
                    }
                }
            } else if (cellType == CellType.BOOLEAN) {
                value = String.valueOf(cell.getBooleanCellValue());
            } else {
                // 其它类型:用formatter兜底
                value = formatter.formatCellValue(cell, evaluator);
            }

            // 清理:去除千位分隔符和多余空格
            if (value != null) {
                value = value.trim();
                if (value.matches("[0-9,\\s.]+") && value.contains(",")) {
                    value = value.replace(",", "").replace(" ", "");
                }
                return value.isEmpty() ? null : value;
            }
            return null;
        } catch (Exception e) {
            try {
                // 异常回退:尝试直接读取原始字符串
                return cell.getStringCellValue().trim();
            } catch (Exception ignored) {
                return null;
            }
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
