package com.example.auth.controller;

import com.example.auth.common.OperateLogUtil;
import com.example.auth.common.PageResult;
import com.example.auth.common.Result;
import com.example.auth.entity.Server;
import com.example.auth.mapper.ServerMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
@RequestMapping("/api/server/servers")
@CrossOrigin
public class ServerController {

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private OperateLogUtil logUtil;

    private static final String MODULE_NAME = "服务器管理";

    private String currentUser(HttpServletRequest request) {
        Object u = request.getAttribute("username");
        return u == null ? "" : u.toString();
    }

    @GetMapping
    public Result<PageResult<Server>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer serverStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Server> list = serverMapper.selectByCondition(keyword, serverStatus, offset, size);
        int total = serverMapper.countByCondition(keyword, serverStatus);
        return Result.ok(new PageResult<>(total, list, page, size));
    }

    @GetMapping("/{id}")
    public Result<Server> getById(@PathVariable Long id) {
        Server server = serverMapper.selectById(id);
        return Result.ok(server);
    }

    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Server server, HttpServletRequest request) {
        int rows = serverMapper.insert(server);
        logUtil.logAdd(MODULE_NAME, server.getId(), server.getServerName(), server, currentUser(request));
        Map<String, Object> data = new HashMap<>();
        data.put("id", server.getId());
        return Result.ok(data);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Server server, HttpServletRequest request) {
        Server oldServer = serverMapper.selectById(id);
        server.setId(id);
        int rows = serverMapper.update(server);
        Server newServer = serverMapper.selectById(id);
        logUtil.logUpdate(
                MODULE_NAME, id,
                server.getServerName() != null ? server.getServerName() : (oldServer != null ? oldServer.getServerName() : null),
                oldServer, newServer, currentUser(request)
        );
        return Result.ok(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Server oldServer = serverMapper.selectById(id);
        int rows = serverMapper.deleteById(id);
        logUtil.logDelete(MODULE_NAME, id, oldServer != null ? oldServer.getServerName() : String.valueOf(id), oldServer, currentUser(request));
        return Result.ok(null);
    }

    // ==================== 导入/导出/模板下载 ====================

    private String[] EXPORT_HEADERS = {
            "服务器名称", "IP地址", "类型", "所在地区", "所在分组", "MFA密钥",
            "状态", "远程账号", "远程密码", "后台账号", "后台密码", "到期时间", "备注"
    };
    private String[] EXPORT_COLS = {
            "serverName", "ipAddress", "serverType", "location", "specs", "mfaKey",
            "serverStatus", "remoteAccount", "remotePwd", "backendAccount", "backendPwd", "expireTime", "remark"
    };
    private String[] IMPORT_HEADERS = {
            "服务器名称", "IP地址", "类型", "所在地区", "所在分组", "MFA密钥",
            "状态", "远程账号", "远程密码", "后台账号", "后台密码", "到期时间", "备注"
    };

    /**
     * 导出全部服务器数据为 Excel
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            List<Server> list = serverMapper.selectAllForExport();
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("服务器列表");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(wb.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

            // 表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMddHHmmss");
            int rowIdx = 1;
            for (Server s : list) {
                Row row = sheet.createRow(rowIdx++);
                for (int i = 0; i < EXPORT_COLS.length; i++) {
                    Cell cell = row.createCell(i);
                    Object val = getFieldValue(s, EXPORT_COLS[i]);
                    if (val == null) {
                        cell.setCellValue("");
                    } else if (val instanceof Date) {
                        cell.setCellValue(sdf.format((Date) val));
                    } else if ("serverStatus".equals(EXPORT_COLS[i])) {
                        cell.setCellValue(statusText(val));
                    } else {
                        cell.setCellValue(String.valueOf(val));
                    }
                }
            }

            // 自动列宽
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            String filename = "服务器列表_" + fileSdf.format(new Date()) + ".xlsx";
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

    private String statusText(Object val) {
        if (val == null) return "";
        int status = val instanceof Integer ? (Integer) val : Integer.parseInt(String.valueOf(val));
        switch (status) {
            case 1: return "运行中";
            case 2: return "维护中";
            case 3: return "已下线";
            case 4: return "到期";
            default: return String.valueOf(status);
        }
    }

    private Object getFieldValue(Server s, String field) {
        switch (field) {
            case "serverName": return s.getServerName();
            case "ipAddress": return s.getIpAddress();
            case "serverType": return s.getServerType();
            case "location": return s.getLocation();
            case "specs": return s.getSpecs();
            case "mfaKey": return s.getMfaKey();
            case "serverStatus": return s.getServerStatus();
            case "remoteAccount": return s.getRemoteAccount();
            case "remotePwd": return s.getRemotePwd();
            case "backendAccount": return s.getBackendAccount();
            case "backendPwd": return s.getBackendPwd();
            case "expireTime": return s.getExpireTime();
            case "remark": return s.getRemark();
            default: return null;
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("服务器导入模板");
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

            // 示例行
            Row exampleRow = sheet.createRow(1);
            String[] exampleValues = {"示例服务器", "192.168.1.100", "腾讯云", "广州", "应用组", "",
                    "1", "root", "password", "admin", "admin123", "2026-12-31 23:59:59", "备注"};
            for (int i = 0; i < exampleValues.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleValues[i]);
            }

            // 状态说明行
            Row noteRow = sheet.createRow(2);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("状态说明：1=运行中 2=维护中 3=已下线 4=到期");

            // 自动列宽
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            String filename = "服务器导入模板.xlsx";
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
     * 导入服务器（批量新增）
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importServers(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
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
            int startRow = 1; // 第0行是表头，从第1行开始读数据
            int imported = 0;
            List<Server> servers = new ArrayList<>();
            SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("yyyy/MM/dd"),
                new SimpleDateFormat("yyyy-MM-dd"),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            };

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                // 检查整行是否为空（所有关键列都没有值则跳过）
                boolean rowEmpty = true;
                for (int c = 0; c <= 12; c++) {
                    Cell checkCell = row.getCell(c);
                    if (checkCell != null && checkCell.getCellType() != CellType.BLANK) {
                        String val = getCellString(checkCell);
                        if (val != null && !val.trim().isEmpty()) {
                            rowEmpty = false;
                            break;
                        }
                    }
                }
                if (rowEmpty) continue;

                Server s = new Server();
                s.setServerName(getCellString(row.getCell(0)));
                s.setIpAddress(getCellString(row.getCell(1)));
                s.setServerType(getCellString(row.getCell(2)));
                s.setLocation(getCellString(row.getCell(3)));
                s.setSpecs(getCellString(row.getCell(4)));
                s.setMfaKey(getCellString(row.getCell(5)));
                String statusStr = getCellString(row.getCell(6));
                if (statusStr != null && !statusStr.isEmpty()) {
                    s.setServerStatus(Integer.parseInt(statusStr));
                }
                s.setRemoteAccount(getCellString(row.getCell(7)));
                s.setRemotePwd(getCellString(row.getCell(8)));
                s.setBackendAccount(getCellString(row.getCell(9)));
                s.setBackendPwd(getCellString(row.getCell(10)));
                String expireStr = getCellString(row.getCell(11));
                if (expireStr != null && !expireStr.isEmpty()) {
                    for (SimpleDateFormat fmt : dateFormats) {
                        try { s.setExpireTime(fmt.parse(expireStr)); break; } catch (Exception ignored) {}
                    }
                }
                s.setRemark(getCellString(row.getCell(12)));
                servers.add(s);
            }
            wb.close();

            if (!servers.isEmpty()) {
                serverMapper.batchInsert(servers);
                imported = servers.size();
                logUtil.logAdd(MODULE_NAME, null, "批量导入" + imported + "条", null, currentUser(request));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("imported", imported);
            return Result.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("导入失败：" + e.getMessage());
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); } catch (Exception e) { return String.valueOf((long) cell.getNumericCellValue()); }
            default: return null;
        }
    }
}
