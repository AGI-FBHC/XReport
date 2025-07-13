package com.example.medicalreportstructurizer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;
import com.example.medicalreportstructurizer.mapper.UnstructuredReportMapper;
import com.example.medicalreportstructurizer.service.DeepSeekApiService;
import com.example.medicalreportstructurizer.service.MedicalReportService;

import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MedicalReportServiceImpl implements MedicalReportService {

    @Autowired
    private UnstructuredReportMapper unstructuredReportMapper;

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
}
