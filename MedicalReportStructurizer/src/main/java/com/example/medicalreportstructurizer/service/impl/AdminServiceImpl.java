package com.example.medicalreportstructurizer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.medicalreportstructurizer.entity.Admin;
import com.example.medicalreportstructurizer.mapper.AdminMapper;
import com.example.medicalreportstructurizer.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
}