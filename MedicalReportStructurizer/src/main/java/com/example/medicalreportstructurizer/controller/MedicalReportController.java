package com.example.medicalreportstructurizer.controller;

import com.example.medicalreportstructurizer.entity.UnstructuredReport;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.service.DocumentService;
import com.example.medicalreportstructurizer.service.MedicalReportService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/report")
public class MedicalReportController {
    @Autowired
    private MedicalReportService unstructuredReportService;

    @Autowired
    private DocumentService documentService;

    // 原始的非结构化文本按照表格形式解析到实体并存储到非结构化数据表中
    @PostMapping("/rawToTable")
    public Map<String, Object> rawToTable(@RequestBody String rawText) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> map = unstructuredReportService.rawToTable(rawText);
        if (map.get("result").equals(1)) {
            result.put("code", 200);
            result.put("message", "解析成功");
            result.put("data", map.get("data"));
            return result;
        } else {
            result.put("code", 500);
            result.put("message", "解析失败：" + map.get("message"));
            return result;
        }
    }

    @PostMapping("/rawToStructured")
    public Map<String, Object> rawToStructured(@RequestBody String rawText) {
        Map<String, Object> result = new HashMap<>();
        StructuredReport resultReport = unstructuredReportService.RawToStructedRaw(rawText);
        if (resultReport != null) {
            result.put("code", 200);
            result.put("message", "解析成功");
            result.put("data", resultReport);
            return result;
        } else {
            result.put("code", 500);
            result.put("message", "解析失败");
            return result;
        }
    }

    @PostMapping("/structuredDocument")
    public ResponseEntity<ByteArrayResource> downloadDocument(@RequestBody String rawText) throws IOException {

        StructuredReport resultReport = unstructuredReportService.RawToStructedRaw(rawText);
        UnstructuredReport unstructuredReport = unstructuredReportService.rawToUnstructuredReport(rawText);

        byte[] documentBytes = documentService.generateDocument(resultReport, unstructuredReport);
        ByteArrayResource resource = new ByteArrayResource(documentBytes);

        HttpHeaders headers = new HttpHeaders();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        String fileName = "结肠癌结构化报告_" + unstructuredReport.getSerialNumber() + "_" + timestamp + ".docx";

        String encodedFilename = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFilename);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(documentBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}