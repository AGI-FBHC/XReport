package com.example.medicalreportstructurizer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;

import java.util.Map;

public interface MedicalReportService {

    Map<String, Object> rawToTable(String rawText);

    StructuredReport RawToStructedRaw(String rawText);

    public UnstructuredReport rawToUnstructuredReport(String rawText);
}