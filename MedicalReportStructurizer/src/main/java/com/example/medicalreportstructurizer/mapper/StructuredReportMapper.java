package com.example.medicalreportstructurizer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StructuredReportMapper extends BaseMapper<StructuredReport> {
}