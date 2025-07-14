/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 90300
 Source Host           : localhost:3306
 Source Schema         : medical_report

 Target Server Type    : MySQL
 Target Server Version : 90300
 File Encoding         : 65001

 Date: 13/07/2025 14:19:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（存储哈希值）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for admin_token
-- ----------------------------
DROP TABLE IF EXISTS `admin_token`;
CREATE TABLE `admin_token`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录Token（唯一）',
  `admin_id` bigint UNSIGNED NOT NULL COMMENT '关联管理员ID',
  `expire_time` datetime NOT NULL COMMENT 'Token过期时间',
  `last_used_time` datetime NULL DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `token`(`token` ASC) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  CONSTRAINT `admin_token_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `admin` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员Token表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for structured_report
-- ----------------------------
DROP TABLE IF EXISTS `structured_report`;
CREATE TABLE `structured_report`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `unstructured_report_id` bigint UNSIGNED NOT NULL COMMENT '关联非结构化报告ID',
  `patient_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '患者姓名',
  `gender` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `age` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '年龄',
  `clinical_diagnosis` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '临床诊断',
  `is_left_colon` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否左结肠',
  `is_right_colon` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否右结肠',
  `is_appendix` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于盲肠',
  `is_ascending_colon` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于升结肠',
  `is_colon_liv_cerurve` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于结肠肝曲',
  `is_transverse_colon` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于横结肠',
  `is_colon_splenic_flexure` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于结肠脾曲',
  `is_descending_colon` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于降结肠',
  `is_sigmoid_colon` tinyint(1) NULL DEFAULT NULL COMMENT '肿瘤位置是否位于乙状结肠',
  `mass_length` decimal(5, 2) NULL DEFAULT NULL COMMENT '肿块长度(cm)',
  `mass_width` decimal(5, 2) NULL DEFAULT NULL COMMENT '肿块宽度(cm)',
  `mass_height` decimal(5, 2) NULL DEFAULT NULL COMMENT '肿块高度(cm)',
  `wall_thickness` decimal(5, 2) NULL DEFAULT NULL COMMENT '肠壁最厚处(cm)',
  `is_t1` tinyint(1) NULL DEFAULT NULL COMMENT '侵犯至黏膜下层(T1)',
  `is_t2` tinyint(1) NULL DEFAULT NULL COMMENT '侵犯固有肌层(T2)',
  `is_t3_less_5mm` tinyint(1) NULL DEFAULT NULL COMMENT '突破固有肌层(T3)<5mm',
  `is_t3_more_5mm` tinyint(1) NULL DEFAULT NULL COMMENT '突破固有肌层(T3)>5mm',
  `is_t4a` tinyint(1) NULL DEFAULT NULL COMMENT '侵犯脏层腹膜(T4a)',
  `is_t4b` tinyint(1) NULL DEFAULT NULL COMMENT '侵犯邻近结构(T4b)',
  `is_rsm_positive` tinyint(1) NULL DEFAULT NULL COMMENT 'RSM阳性',
  `regional_lymph_node_count` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '区域淋巴结数目',
  `largest_lymph_node_size` decimal(5, 2) NULL DEFAULT NULL COMMENT '最大淋巴结短径(cm)',
  `retroperitoneal_lymph_node_count` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '腹膜后淋巴结数目',
  `largest_retroperitoneal_lymph_node_size` decimal(5, 2) NULL DEFAULT NULL COMMENT '最大腹膜后淋巴结短径(cm)',
  `is_emvi_positive` tinyint(1) NULL DEFAULT NULL COMMENT 'EMVI阳性',
  `has_liver_metastasis` tinyint(1) NULL DEFAULT NULL COMMENT '肝转移',
  `has_left_lung_metastasis` tinyint(1) NULL DEFAULT NULL COMMENT '左肺转移',
  `has_right_lung_metastasis` tinyint(1) NULL DEFAULT NULL COMMENT '右肺转移',
  `has_peritoneal_metastasis` tinyint(1) NULL DEFAULT NULL COMMENT '腹膜转移',
  `has_other_metastasis_details` tinyint(1) NULL DEFAULT NULL COMMENT '其他转移部位及详情',
  `has_bowel_obstruction` tinyint(1) NULL DEFAULT NULL COMMENT '肠梗阻',
  `has_bowel_perforation` tinyint(1) NULL DEFAULT NULL COMMENT '肠穿孔',
  `diagnosis_conclusion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '诊断结论',
  `treatment_suggestion` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '治疗建议',
  `radiologist` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报告医生',
  `report_date` datetime NULL DEFAULT NULL COMMENT '报告日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `unstructured_report_id`(`unstructured_report_id` ASC) USING BTREE,
  CONSTRAINT `structured_report_ibfk_1` FOREIGN KEY (`unstructured_report_id`) REFERENCES `unstructured_report` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '结构化报告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for unstructured_report
-- ----------------------------
DROP TABLE IF EXISTS `unstructured_report`;
CREATE TABLE `unstructured_report`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `serial_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报告序号（唯一）',
  `gender` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别（男/女）',
  `age` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年龄（如67岁）',
  `examination_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检查类型（如CT）',
  `examination_parts` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检查部位（如胸部,上腹部,盆腔）',
  `examination_method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检查方法（如平扫,增强）',
  `examination_date` date NULL DEFAULT NULL COMMENT '检查日期',
  `examination_time` time NULL DEFAULT NULL COMMENT '检查时间',
  `radiological_findings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '放射学表现（长文本）',
  `image_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '影像号',
  `outpatient_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门诊号',
  `inpatient_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '住院号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `serial_number`(`serial_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '非结构化报告表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
