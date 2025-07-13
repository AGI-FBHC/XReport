package com.example.medicalreportstructurizer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.medicalreportstructurizer.mapper")
public class MedicalReportStructurizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalReportStructurizerApplication.class, args);
    }
}