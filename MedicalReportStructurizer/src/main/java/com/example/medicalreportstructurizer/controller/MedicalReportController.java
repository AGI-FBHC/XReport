package com.example.medicalreportstructurizer.controller;

import com.example.medicalreportstructurizer.entity.UnstructuredReport;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.service.DocumentService;
import com.example.medicalreportstructurizer.service.MedicalReportService;

import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        if (!rawText.startsWith("序号")) {
            System.out.println("no");
            rawText = "序号	性别	年龄	检查类型	检查部位	检查方法	检查日期	检查时间	放射学表现	放射学表现	影像号	门诊号	住院号 \n" + rawText;
        } else {
            System.out.println("yes");
        }

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

    /**
     * 上传Excel文件，解析后生成多个Word文档并打包成ZIP下载
     */
    @PostMapping("/excel-to-zip")
    public void uploadExcelAndGenerateZip(@RequestParam("file") MultipartFile file,
            HttpServletResponse response) throws Exception {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 检查文件格式是否为Excel
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))) {
            throw new IllegalArgumentException("请上传Excel格式的文件（.xlsx 或 .xls）");
        }

        // 禁用响应缓存
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            // 解析Excel文件，生成结构化报告列表（这里假设Excel中每行对应一个报告）
            List<ResponseEntity<ByteArrayResource>> structuredReportDocs = unstructuredReportService
                    .parseExcelToReports(file.getInputStream());

            // 设置响应头，指定为ZIP文件下载
            response.setContentType("application/zip");
            response.setCharacterEncoding("utf-8");
            String zipFileName = URLEncoder.encode("医疗报告集合_" + System.currentTimeMillis() + ".zip", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");

            // 创建ZIP输出流并将所有文档添加到ZIP包中
            try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
                for (ResponseEntity<ByteArrayResource> doc : structuredReportDocs) {
                    String contentDisposition = doc.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
                    String docFileName = extractFileName(contentDisposition);

                    zipOut.putNextEntry(new ZipEntry(docFileName));
                    ByteArrayResource resource = doc.getBody();
                    if (resource != null) {
                        zipOut.write(resource.getByteArray());
                    }
                    zipOut.closeEntry();
                }
                // 强制刷新输出流确保所有数据已写出
                zipOut.flush();
            }
        } catch (Exception e) {
            // 重置响应，清除之前可能已写入的内容
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain; charset=UTF-8");

            // 使用PrintWriter确保中文错误信息正常显示
            try (PrintWriter writer = response.getWriter()) {
                writer.write("处理文件时出错: " + e.getMessage());
            } catch (IOException ioException) {
                // 记录日志但不抛出新异常，避免掩盖原始错误
                ioException.printStackTrace();
            }

            // 记录原始异常堆栈信息
            e.printStackTrace();
        }
    }

    /**
     * 从Content-Disposition头中提取文件名
     */
    private String extractFileName(String contentDisposition) {
        if (contentDisposition == null) {
            return "未命名文件_" + System.currentTimeMillis() + ".docx";
        }

        // 从Content-Disposition中提取文件名
        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("filename=")) {
                String fileName = part.substring("filename=".length()).trim();
                // 去除引号
                if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }
                return fileName;
            }
        }

        return "文件_" + System.currentTimeMillis() + ".docx";
    }

}