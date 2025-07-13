package com.example.medicalreportstructurizer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UnstructuredReportMapper extends BaseMapper<UnstructuredReport> {

    @Select("SELECT COUNT(*) FROM unstructured_report WHERE serial_number = #{serialNumber}")
    boolean existsBySerialNumber(String serialNumber);
}