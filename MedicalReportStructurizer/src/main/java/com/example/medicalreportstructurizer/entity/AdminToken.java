package com.example.medicalreportstructurizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin") // 映射数据库表名
public class AdminToken {
    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;
    private String token;
    private Long adminId; // 关联Admin表的id
    private LocalDateTime expireTime; // token过期时间
}