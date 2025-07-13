package com.example.medicalreportstructurizer.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.medicalreportstructurizer.entity.Admin;
import com.example.medicalreportstructurizer.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 新增管理员
    @PostMapping("/addAdmin")
    public Map<String, Object> addAdmin(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        boolean success = adminService.save(admin);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "新增成功" : "新增失败");
        result.put("data", success ? admin : null);
        return result;
    }

    // 根据ID删除管理员
    @DeleteMapping("/deleteAdminById")
    public Map<String, Object> deleteAdminById(@RequestBody Map<String, Long> param) {
        Map<String, Object> result = new HashMap<>();
        Long id = param.get("id");
        if (id == null) {
            result.put("code", 500);
            result.put("message", "ID不能为空");
            return result;
        }
        boolean success = adminService.removeById(id);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "删除成功" : "删除失败（ID不存在）");
        return result;
    }

    // 根据ID查询管理员
    @PostMapping("/queryAdminById")
    public Map<String, Object> queryAdminById(@RequestBody Map<String, Long> param) {
        Map<String, Object> result = new HashMap<>();
        Long id = param.get("id");
        if (id == null) {
            result.put("code", 400);
            result.put("message", "ID不能为空");
            return result;
        }
        Admin admin = adminService.getById(id);
        result.put("code", admin != null ? 200 : 404);
        result.put("message", admin != null ? "查询成功" : "管理员不存在");
        result.put("data", admin);
        return result;
    }

    // 更新管理员信息（根据ID）
    @PutMapping("/updateAdminById")
    public Map<String, Object> updateAdminById(@RequestBody Admin admin) {
        Map<String, Object> result = new HashMap<>();
        if (admin.getId() == null) {
            result.put("code", 400);
            result.put("message", "ID不能为空");
            return result;
        }
        boolean success = adminService.updateById(admin);
        result.put("code", success ? 200 : 500);
        result.put("message", success ? "更新成功" : "更新失败（ID不存在）");
        result.put("data", success ? admin : null);
        return result;
    }
}