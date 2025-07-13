package com.example.medicalreportstructurizer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;
import com.example.medicalreportstructurizer.mapper.UnstructuredReportMapper;
import com.example.medicalreportstructurizer.service.DeepSeekApiService;
import com.example.medicalreportstructurizer.service.DocumentService;
import com.example.medicalreportstructurizer.service.MedicalReportService;

import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
public class MedicalReportServiceImpl implements MedicalReportService {

    @Autowired
    private UnstructuredReportMapper unstructuredReportMapper;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DeepSeekApiService deepSeekApiService;

    @Override
    public Map<String, Object> rawToTable(String rawText) {
        Map<String, Object> map = new HashMap<>();
        UnstructuredReport report = new UnstructuredReport();
        try {
            // 按行分割文本
            String[] lines = rawText.split("\\r?\\n");

            // 跳过标题行
            if (lines.length <= 1) {
                map.put("result", 0);
                map.put("message", "无有效数据");
                return map;
            }

            // 解析数据行
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty())
                    continue;

                // 按制表符分割字段
                String[] fields = line.split("\\t");
                if (fields.length < 13)
                    continue; // 确保字段数量足够

                // 解析序号
                try {
                    String serialNumber = fields[0].trim();
                    report.setSerialNumber(serialNumber);
                    // 查询当前表中是否存在该SerialNumber
                    boolean exists = unstructuredReportMapper.existsBySerialNumber(serialNumber);
                    if (exists) {
                        System.out.println("SerialNumber {} 已存在");
                        map.put("result", 0);
                        map.put("message", "序号：" + serialNumber + " 已存在");
                        return map;
                    }
                } catch (NumberFormatException e) {
                    report.setSerialNumber(null);
                }

                // 解析性别
                report.setGender("男".equals(fields[1].trim()));

                // 解析年龄
                try {
                    String ageText = fields[2].trim();
                    report.setAge(Integer.parseInt(ageText.replaceAll("\\D+", "")));
                } catch (NumberFormatException e) {
                    report.setAge(null);
                }

                // 解析检查类型、部位、方法
                report.setExaminationType(fields[3].trim());
                report.setExaminationParts(fields[4].trim());
                report.setExaminationMethod(fields[5].trim());

                // 解析检查日期和时间
                try {
                    report.setExaminationDate(LocalDate.parse(fields[6].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
                    report.setExaminationTime(LocalTime.parse(fields[7].trim(), DateTimeFormatter.ISO_LOCAL_TIME));
                } catch (Exception e) {
                    report.setExaminationDate(null);
                    report.setExaminationTime(null);
                }

                // 合并放射学表现字段
                String radiologicalFindings = (fields[8].trim() + " " + fields[9].trim()).trim();
                report.setRadiologicalFindings(radiologicalFindings);

                // 解析影像号、门诊号、住院号
                report.setImageNumber(fields[10].trim());
                report.setOutpatientNumber(fields[11].trim());
                report.setInpatientNumber(fields[12].trim());

                // 修正后的代码
                unstructuredReportMapper.insert(report);
            }

            map.put("result", 1);
            map.put("message", "解析成功");
            map.put("data", report);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("result", 0);
            map.put("message", "解析或存储数据失败");
            return map;
        }
    }

    @Override
    public UnstructuredReport rawToUnstructuredReport(String rawText) {
        UnstructuredReport report = new UnstructuredReport();
        try {
            // 按行分割文本
            String[] lines = rawText.split("\\r?\\n");

            // 跳过标题行
            if (lines.length <= 1) {
                return null;
            }

            // 解析数据行
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty())
                    continue;

                // 按制表符分割字段
                String[] fields = line.split("\\t");
                if (fields.length < 13)
                    continue; // 确保字段数量足够

                // 解析序号
                try {
                    String serialNumber = fields[0].trim();
                    report.setSerialNumber(serialNumber);
                    // 查询当前表中是否存在该SerialNumber
                    boolean exists = unstructuredReportMapper.existsBySerialNumber(serialNumber);
                    if (exists) {
                        System.out.println("SerialNumber {} 已存在");
                    }
                } catch (NumberFormatException e) {
                    report.setSerialNumber(null);
                }

                // 解析性别
                report.setGender("男".equals(fields[1].trim()));

                // 解析年龄
                try {
                    String ageText = fields[2].trim();
                    report.setAge(Integer.parseInt(ageText.replaceAll("\\D+", "")));
                } catch (NumberFormatException e) {
                    report.setAge(null);
                }

                // 解析检查类型、部位、方法
                report.setExaminationType(fields[3].trim());
                report.setExaminationParts(fields[4].trim());
                report.setExaminationMethod(fields[5].trim());

                // 解析检查日期和时间
                try {
                    report.setExaminationDate(LocalDate.parse(fields[6].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
                    report.setExaminationTime(LocalTime.parse(fields[7].trim(), DateTimeFormatter.ISO_LOCAL_TIME));
                } catch (Exception e) {
                    report.setExaminationDate(null);
                    report.setExaminationTime(null);
                }

                // 合并放射学表现字段
                String radiologicalFindings = (fields[8].trim() + " " + fields[9].trim()).trim();
                report.setRadiologicalFindings(radiologicalFindings);

                // 解析影像号、门诊号、住院号
                report.setImageNumber(fields[10].trim());
                report.setOutpatientNumber(fields[11].trim());
                report.setInpatientNumber(fields[12].trim());
                return report;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return report;
    }

    @Override
    public StructuredReport RawToStructedRaw(String rawText) {
        StructuredReport structuredText = deepSeekApiService.convertToStructuredMsg(
                "", rawText,
                "");
        return structuredText;
    }

    public List<ResponseEntity<ByteArrayResource>> parseExcelToReports(InputStream inputStream) {
        List<ResponseEntity<ByteArrayResource>> structuredReportDocs = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // 统计需要处理的行数
            int rowCount = sheet.getPhysicalNumberOfRows() - 1;
            if (rowCount <= 0)
                return structuredReportDocs;

            CountDownLatch latch = new CountDownLatch(rowCount);
            ConcurrentLinkedQueue<ResponseEntity<ByteArrayResource>> resultQueue = new ConcurrentLinkedQueue<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // 跳过标题行

                if (row.getCell(0) == null || row.getCell(0).getStringCellValue().trim().isEmpty()) {
                    latch.countDown();
                    continue;
                }

                StringBuilder rawTextBuilder = new StringBuilder();
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    if (i > 0)
                        rawTextBuilder.append("\t");
                    if (row.getCell(i) != null) {
                        switch (row.getCell(i).getCellType()) {
                            case STRING -> rawTextBuilder.append(row.getCell(i).getStringCellValue());
                            case NUMERIC -> rawTextBuilder.append(row.getCell(i).getNumericCellValue());
                            case BOOLEAN -> rawTextBuilder.append(row.getCell(i).getBooleanCellValue());
                            case FORMULA -> rawTextBuilder.append(row.getCell(i).getCellFormula());
                            default -> rawTextBuilder.append("");
                        }
                    } else {
                        rawTextBuilder.append("");
                    }
                }
                String rawText = rawTextBuilder.toString();

                // 多线程处理
                executorService.submit(() -> {
                    try {
                        ResponseEntity<ByteArrayResource> structuredReportDoc = generateReport(rawText);
                        resultQueue.add(structuredReportDoc);
                    } catch (IOException e) {
                        e.printStackTrace(); // 可以改为日志记录
                    } finally {
                        latch.countDown(); // 无论成功与否都减少计数
                    }
                });
            }

            latch.await(); // 等待所有线程完成
            structuredReportDocs.addAll(resultQueue);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown(); // 安全关闭线程池
        }

        return structuredReportDocs;
    }

    public ResponseEntity<ByteArrayResource> generateReport(String rawText) throws IOException {
        rawText = "序号\t性别\t年龄\t检查类型\t检查部位\t检查方法\t检查日期\t检查时间\t放射学表现\t放射学表现\t影像号\t门诊号\t住院号 \n" + rawText;

        StructuredReport resultReport = RawToStructedRaw(rawText);
        UnstructuredReport unstructuredReport = rawToUnstructuredReport(rawText);

        byte[] documentBytes = documentService.generateDocument(resultReport, unstructuredReport);
        ByteArrayResource resource = new ByteArrayResource(documentBytes);

        HttpHeaders headers = new HttpHeaders();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String fileName = "report_" + unstructuredReport.getSerialNumber() + "_" + timestamp
                + RandomStringUtils.randomAlphanumeric(4) + ".docx";
        System.out.println(fileName);

        String encodedFilename = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFilename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(documentBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
