package com.example.medicalreportstructurizer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

public interface MedicalReportService {

    Map<String, Object> rawToTable(String rawText);

    StructuredReport RawToStructedRaw(String rawText);

    public UnstructuredReport rawToUnstructuredReport(String rawText);

    List<ResponseEntity<ByteArrayResource>> parseExcelToReports(InputStream inputStream);
}