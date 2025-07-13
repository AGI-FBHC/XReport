package com.example.medicalreportstructurizer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

@Data
@Component
@TableName("structured_report")
public class StructuredReport {
    @TableId(type = IdType.AUTO)
    private Long id; // 结构化报告id
    private Long reportId; // 对应非结构化报告id
    private String serialNumber; // 序号（医院提供）
    private String examItem; // 检查项目
    private LocalDate examDate; // 检查日期

    // 患者基本信息
    private String patientName; // 患者姓名
    private String gender; // 性别
    private Integer age; // 年龄
    private String clinicalDiagnosis; // 临床诊断

    // 肿瘤位置信息
    private Boolean isLeftColon; // 肿瘤位置是否左结肠
    private Boolean isRightColon; // 肿瘤位置是否右结肠
    private Boolean isAppendix; // 肿瘤位置是否位于盲肠
    private Boolean isAscendingColon; // 肿瘤位置是否位于升结肠
    private Boolean isColonLivCerurve; // 肿瘤位置是否位于结肠肝曲
    private Boolean isTransverseColon; // 肿瘤位置是否位于横结肠
    private Boolean isColonSplenicFlexure; // 肿瘤位置是否位于结肠脾曲
    private Boolean isDescendingColon; // 肿瘤位置是否位于降结肠
    private Boolean isSigmoidColon; // 肿瘤位置是否位于乙状结肠

    // 肿瘤大小信息（三维尺寸）
    private BigDecimal massLength; // 肿块长度(cm)
    private BigDecimal massWidth; // 肿块宽度(cm)
    private BigDecimal massHeight; // 肿块高度(cm)
    private BigDecimal wallThickness; // 肠壁最厚处(cm)

    // 肿瘤侵犯深度(T分期)
    private Boolean isT1; // 侵犯至黏膜下层(T1)
    private Boolean isT2; // 侵犯固有肌层(T2)
    private Boolean isT3Less5mm; // 突破固有肌层(T3)<5mm
    private Boolean isT3More5mm; // 突破固有肌层(T3)>5mm
    private Boolean isT4a; // 侵犯脏层腹膜(T4a)
    private Boolean isT4b; // 侵犯邻近结构(T4b)

    // 肿瘤侵犯腹膜后手术切缘(RSM)，仅适用于升/降阶段
    private Boolean isRsmPositive; // RSM阳性

    // 淋巴结转移情况
    private Integer regionalLymphNodeCount; // 区域淋巴结数目
    private BigDecimal largestLymphNodeSize; // 最大淋巴结短径(cm)
    private Integer retroperitonealLymphNodeCount; // 腹膜后淋巴结数目
    private BigDecimal largestRetroperitonealLymphNodeSize; // 最大腹膜后淋巴结短径(cm)

    // 肠壁外血管侵犯(EMVI)
    private Boolean isEmviPositive; // EMVI阳性

    // 远处转移情况
    private Boolean hasLiverMetastasis; // 肝转移
    private Boolean hasLeftLungMetastasis; // 左肺转移
    private Boolean hasRightLungMetastasis; // 右肺转移
    private Boolean hasPeritonealMetastasis; // 腹膜转移
    private Boolean hasOtherMetastasisDetails; // 是否存在其他转移部位及详情

    // 其他异常征象
    private Boolean hasBowelObstruction; // 肠梗阻
    private Boolean hasBowelPerforation; // 肠穿孔

    // 诊断意见
    private String diagnosisConclusion; // 诊断结论
    private String treatmentSuggestion; // 治疗建议

    // 报告信息
    // private String radiologist; // 报告医生
    // private LocalDateTime reportDate; // 报告日期
}