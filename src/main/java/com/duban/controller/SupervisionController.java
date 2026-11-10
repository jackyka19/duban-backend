package com.duban.controller;

import com.duban.model.SupervisionItem;
import com.duban.service.SupervisionService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervision")
@CrossOrigin(origins = "*")
public class SupervisionController {

    @Autowired
    private SupervisionService service;

    @GetMapping("/items")
    public List<SupervisionItem> getItems(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<String> progresses,
            @RequestParam(defaultValue = "false") boolean onlyUnreported) {
        return service.filterItems(startDate, endDate, progresses, onlyUnreported);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<String> progresses,
            @RequestParam(defaultValue = "false") boolean onlyUnreported) throws IOException {

        List<SupervisionItem> items = service.filterItems(startDate, endDate, progresses, onlyUnreported);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("督辦事項進度統計");

            // Header
            Row header = sheet.createRow(0);
            String[] headers = {"ID", "事項名稱", "發起日期", "時限要求", "當前進度", "責任部門", "是否超期未反饋", "反饋時限"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowNum = 1;
            for (SupervisionItem item : items) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getName());
                row.createCell(2).setCellValue(item.getStartDate());
                row.createCell(3).setCellValue(item.getDeadline());
                row.createCell(4).setCellValue(item.getProgress());
                row.createCell(5).setCellValue(item.getDepartment());
                row.createCell(6).setCellValue(item.isOverdue() ? "是" : "否");
                row.createCell(7).setCellValue(item.getFeedbackDeadline());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=supervision_report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(out.toByteArray());
        }
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<SupervisionItem> all = service.getAllItems();
        long total = all.size();
        long notStarted = all.stream().filter(i -> "未開展".equals(i.getProgress())).count();
        long inProgress = all.stream().filter(i -> "推進中".equals(i.getProgress())).count();
        long completed = all.stream().filter(i -> "已完成".equals(i.getProgress())).count();
        long overdue = all.stream().filter(SupervisionItem::isOverdue).count();

        return Map.of(
            "total", total,
            "notStarted", notStarted,
            "inProgress", inProgress,
            "completed", completed,
            "overdue", overdue
        );
    }
}
