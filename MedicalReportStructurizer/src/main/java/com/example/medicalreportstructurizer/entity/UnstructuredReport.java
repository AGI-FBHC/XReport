package com.example.medicalreportstructurizer.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("unstructured_report")
public class UnstructuredReport {
    private Long id;// id，不是序号
    private String serialNumber;// 序号
    private boolean gender;
    private Integer age;
    private String examinationType;
    private String examinationParts;
    private String examinationMethod;
    private LocalDate examinationDate;
    private LocalTime examinationTime;
    private String radiologicalFindings;
    private String imageNumber;
    private String outpatientNumber;
    private String inpatientNumber;
}