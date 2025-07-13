package com.example.medicalreportstructurizer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.medicalreportstructurizer.entity.AdminToken;
import com.example.medicalreportstructurizer.mapper.AdminTokenMapper;
import com.example.medicalreportstructurizer.service.AdminTokenService;
import org.springframework.stereotype.Service;

@Service
public class AdminTokenServiceImpl extends ServiceImpl<AdminTokenMapper, AdminToken> implements AdminTokenService {
}