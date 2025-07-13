package com.example.medicalreportstructurizer.service.impl;

import com.example.medicalreportstructurizer.config.DeepSeekApiConfig;
import com.example.medicalreportstructurizer.entity.DeepSeekRequest;
import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.service.DeepSeekApiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeepSeekApiServiceImpl implements DeepSeekApiService {

    private final DeepSeekApiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String buildPrompt(String rawReport) {
        return "你是一个专业的医学文本分析助手。请根据以下结肠癌检查报告，提取出结构化数据，并严格按照指定的字段名和格式输出JSON，确保字段名与实体类完全一致。\n\n" +
                "【报告内容】\n" +
                rawReport + "\n\n" +
                "【实体类字段映射】\n" +
                "- id: 自动生成，无需提取\n" +
                "- reportId: 对应非结构化报告ID\n" +
                "- serialNumber: 检查编号（医院提供）\n" +
                "- examItem: 检查项目\n" +
                "- examDate: 检查日期（格式：YYYY-MM-DD）\n" +
                "- patientName: 患者姓名\n" +
                "- gender: 性别\n" +
                "- age: 年龄\n" +
                "- clinicalDiagnosis: 临床诊断\n" +
                "- isLeftColon: 是否左半结肠（true/false）\n" +
                "- isRightColon: 是否右半结肠（true/false）\n" +
                "- isAppendix: 是否盲肠（true/false）\n" +
                "- isAscendingColon: 是否升结肠（true/false）\n" +
                "- isColonLivCerurve: 是否结肠肝曲（true/false）\n" +
                "- isTransverseColon: 是否横结肠（true/false）\n" +
                "- isColonSplenicFlexure: 是否结肠脾曲（true/false）\n" +
                "- isDescendingColon: 是否降结肠（true/false）\n" +
                "- isSigmoidColon: 是否乙状结肠（true/false）\n" +
                "- massLength: 肿块长度(cm)，仅提取数值\n" +
                "- massWidth: 肿块宽度(cm)，仅提取数值\n" +
                "- massHeight: 肿块高度(cm)，仅提取数值\n" +
                "- wallThickness: 肠壁最厚处(cm)，仅提取数值\n" +
                "- isT1: 侵犯至黏膜下层(T1)（true/false）\n" +
                "- isT2: 侵犯固有肌层(T2)（true/false）\n" +
                "- isT3Less5mm: 突破固有肌层(T3)<5mm（true/false）\n" +
                "- isT3More5mm: 突破固有肌层(T3)>5mm（true/false）\n" +
                "- isT4a: 侵犯脏层腹膜(T4a)（true/false）\n" +
                "- isT4b: 侵犯邻近结构(T4b)（true/false）\n" +
                "- isRsmPositive: 腹膜后手术切缘阳性（true/false）\n" +
                "- regionalLymphNodeCount: 区域淋巴结数目\n" +
                "- largestLymphNodeSize: 最大淋巴结短径(cm)，仅提取数值\n" +
                "- retroperitonealLymphNodeCount: 腹膜后淋巴结数目\n" +
                "- largestRetroperitonealLymphNodeSize: 最大腹膜后淋巴结短径(cm)，仅提取数值\n" +
                "- isEmviPositive: 肠壁外血管侵犯阳性（true/false）\n" +
                "- hasLiverMetastasis: 肝转移（true/false）\n" +
                "- hasLeftLungMetastasis: 左肺转移（true/false）\n" +
                "- hasRightLungMetastasis: 右肺转移（true/false）\n" +
                "- hasPeritonealMetastasis: 腹膜转移（true/false）\n" +
                "- hasOtherMetastasisDetails: 是否存在其他转移部位（true/false）\n" +
                "- hasBowelObstruction: 肠梗阻（true/false）\n" +
                "- hasBowelPerforation: 肠穿孔（true/false）\n" +
                "- diagnosisConclusion: 诊断结论\n" +
                "- treatmentSuggestion: 治疗建议\n" +
                "- radiologist: 报告医生\n" +
                "- reportDate: 报告日期（格式：YYYY-MM-DD HH:MM:SS）\n\n" +
                "【输出格式要求】\n" +
                "1. 数值字段仅提取数字，去除单位（如\"4.5cm\"→4.5）\n" +
                "2. 布尔字段\"是\"/\"阳性\"映射为true，\"否\"/\"阴性\"映射为false，未提及则留空null\n" +
                "3. 日期格式必须为YYYY-MM-DD或YYYY-MM-DD HH:MM:SS\n" +
                "4. 仅返回纯JSON字符串，**禁止添加任何前缀（如```json）、后缀（如```）或解释文字**\n" +
                "5. 严格按照以下JSON结构输出，字段名必须与实体类完全一致：\n\n" +
                "{" +
                "  \"reportId\": null," +
                "  \"serialNumber\": \"\"," +
                "  \"examItem\": \"\"," +
                "  \"examDate\": \"\"," +
                "  \"patientName\": \"\"," +
                "  \"gender\": \"\"," +
                "  \"age\": null," +
                "  \"clinicalDiagnosis\": \"\"," +
                "  \"isLeftColon\": null," +
                "  \"isRightColon\": null," +
                "  \"isAppendix\": null," +
                "  \"isAscendingColon\": null," +
                "  \"isColonLivCerurve\": null," +
                "  \"isTransverseColon\": null," +
                "  \"isColonSplenicFlexure\": null," +
                "  \"isDescendingColon\": null," +
                "  \"isSigmoidColon\": null," +
                "  \"massLength\": null," +
                "  \"massWidth\": null," +
                "  \"massHeight\": null," +
                "  \"wallThickness\": null," +
                "  \"isT1\": null," +
                "  \"isT2\": null," +
                "  \"isT3Less5mm\": null," +
                "  \"isT3More5mm\": null," +
                "  \"isT4a\": null," +
                "  \"isT4b\": null," +
                "  \"isRsmPositive\": null," +
                "  \"regionalLymphNodeCount\": null," +
                "  \"largestLymphNodeSize\": null," +
                "  \"retroperitonealLymphNodeCount\": null," +
                "  \"largestRetroperitonealLymphNodeSize\": null," +
                "  \"isEmviPositive\": null," +
                "  \"hasLiverMetastasis\": null," +
                "  \"hasLeftLungMetastasis\": null," +
                "  \"hasRightLungMetastasis\": null," +
                "  \"hasPeritonealMetastasis\": null," +
                "  \"hasOtherMetastasisDetails\": null," +
                "  \"hasBowelObstruction\": null," +
                "  \"hasBowelPerforation\": null," +
                "  \"diagnosisConclusion\": \"\"," +
                "  \"treatmentSuggestion\": \"\"," +
                // "  \"radiologist\": \"\"," +
                // "  \"reportDate\": null" +
                "}";
    }

    /**
     * 核心工具方法：将JSON字符串解析为StructuredReport实体类
     */
    private StructuredReport parseJsonToEntity(String jsonResponse) throws Exception {
        StructuredReport report = new StructuredReport();
        // 先清洗JSON再解析（处理可能的残留标记）
        String cleanedJson = cleanJsonString(jsonResponse);
        JsonNode jsonNode = objectMapper.readTree(cleanedJson);

        // 1. 基础文本字段映射（空值处理为默认空字符串）
        report.setSerialNumber(jsonNode.path("serialNumber").asText(""));
        report.setExamItem(jsonNode.path("examItem").asText(""));
        report.setPatientName(jsonNode.path("patientName").asText(""));
        report.setGender(jsonNode.path("gender").asText(""));
        report.setClinicalDiagnosis(jsonNode.path("clinicalDiagnosis").asText(""));
        report.setDiagnosisConclusion(jsonNode.path("diagnosisConclusion").asText(""));
        report.setTreatmentSuggestion(jsonNode.path("treatmentSuggestion").asText(""));
        // report.setRadiologist(jsonNode.path("radiologist").asText(""));

        // 2. 日期字段映射（格式校验）
        String examDateStr = jsonNode.path("examDate").asText();
        if (!examDateStr.isEmpty()) {
            try {
                report.setExamDate(LocalDate.parse(examDateStr));
            } catch (Exception e) {
                throw new RuntimeException("检查日期格式错误，应为YYYY-MM-DD: " + examDateStr, e);
            }
        }

        // String reportDateStr = jsonNode.path("reportDate").asText();
        // if (!reportDateStr.isEmpty()) {
        //     try {
        //         report.setReportDate(LocalDateTime.parse(reportDateStr,
        //                 java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        //     } catch (Exception e) {
        //         throw new RuntimeException("报告日期格式错误，应为YYYY-MM-DD HH:MM:SS: " + reportDateStr, e);
        //     }
        // }

        // 3. 数值字段映射（BigDecimal/Integer，处理null值）
        report.setMassLength(parseBigDecimal(jsonNode, "massLength"));
        report.setMassWidth(parseBigDecimal(jsonNode, "massWidth"));
        report.setMassHeight(parseBigDecimal(jsonNode, "massHeight"));
        report.setWallThickness(parseBigDecimal(jsonNode, "wallThickness"));
        report.setAge(parseInteger(jsonNode, "age"));
        report.setRegionalLymphNodeCount(parseInteger(jsonNode, "regionalLymphNodeCount"));
        report.setLargestLymphNodeSize(parseBigDecimal(jsonNode, "largestLymphNodeSize"));
        report.setRetroperitonealLymphNodeCount(parseInteger(jsonNode, "retroperitonealLymphNodeCount"));
        report.setLargestRetroperitonealLymphNodeSize(parseBigDecimal(jsonNode, "largestRetroperitonealLymphNodeSize"));

        // 4. 布尔字段映射（处理null值，未提及则为null）
        report.setIsLeftColon(parseBoolean(jsonNode, "isLeftColon"));
        report.setIsRightColon(parseBoolean(jsonNode, "isRightColon"));
        report.setIsAppendix(parseBoolean(jsonNode, "isAppendix"));
        report.setIsAscendingColon(parseBoolean(jsonNode, "isAscendingColon"));
        report.setIsColonLivCerurve(parseBoolean(jsonNode, "isColonLivCerurve"));
        report.setIsTransverseColon(parseBoolean(jsonNode, "isTransverseColon"));
        report.setIsColonSplenicFlexure(parseBoolean(jsonNode, "isColonSplenicFlexure"));
        report.setIsDescendingColon(parseBoolean(jsonNode, "isDescendingColon"));
        report.setIsSigmoidColon(parseBoolean(jsonNode, "isSigmoidColon"));
        report.setIsT1(parseBoolean(jsonNode, "isT1"));
        report.setIsT2(parseBoolean(jsonNode, "isT2"));
        report.setIsT3Less5mm(parseBoolean(jsonNode, "isT3Less5mm"));
        report.setIsT3More5mm(parseBoolean(jsonNode, "isT3More5mm"));
        report.setIsT4a(parseBoolean(jsonNode, "isT4a"));
        report.setIsT4b(parseBoolean(jsonNode, "isT4b"));
        report.setIsRsmPositive(parseBoolean(jsonNode, "isRsmPositive"));
        report.setIsEmviPositive(parseBoolean(jsonNode, "isEmviPositive"));
        report.setHasLiverMetastasis(parseBoolean(jsonNode, "hasLiverMetastasis"));
        report.setHasLeftLungMetastasis(parseBoolean(jsonNode, "hasLeftLungMetastasis"));
        report.setHasRightLungMetastasis(parseBoolean(jsonNode, "hasRightLungMetastasis"));
        report.setHasPeritonealMetastasis(parseBoolean(jsonNode, "hasPeritonealMetastasis"));
        report.setHasBowelObstruction(parseBoolean(jsonNode, "hasBowelObstruction"));
        report.setHasBowelPerforation(parseBoolean(jsonNode, "hasBowelPerforation"));
        report.setHasOtherMetastasisDetails(parseBoolean(jsonNode, "hasOtherMetastasisDetails"));

        return report;
    }

    // 辅助方法：解析BigDecimal类型字段
    private BigDecimal parseBigDecimal(JsonNode jsonNode, String fieldName) {
        if (jsonNode.path(fieldName).isNumber()) {
            return jsonNode.path(fieldName).decimalValue();
        }
        return null;
    }

    // 辅助方法：解析Integer类型字段
    private Integer parseInteger(JsonNode jsonNode, String fieldName) {
        if (jsonNode.path(fieldName).isNumber()) {
            return jsonNode.path(fieldName).intValue();
        }
        return null;
    }

    // 辅助方法：解析Boolean类型字段
    private Boolean parseBoolean(JsonNode jsonNode, String fieldName) {
        if (jsonNode.path(fieldName).isBoolean()) {
            return jsonNode.path(fieldName).booleanValue();
        }
        return null;
    }

    private String cleanJsonString(String rawJson) {
        // 移除开头的```json标记（忽略大小写和空格）
        String cleaned = rawJson.replaceAll("^\\s*```\\s*json\\s*", "");
        // 移除结尾的```标记（忽略空格）
        cleaned = cleaned.replaceAll("\\s*```\\s*$", "");
        // 去除前后空白字符
        return cleaned.trim();
    }

    private boolean isValidJson(String json) {
        try {
            String cleaned = cleanJsonString(json);
            objectMapper.readTree(cleaned);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String callDeepSeekApi(String promptContent) {
        try {
            // 1. 构建消息体
            DeepSeekRequest.Message message = new DeepSeekRequest.Message("user", promptContent);
            DeepSeekRequest request = new DeepSeekRequest(
                    "deepseek-chat",
                    List.of(message),
                    0.0 // 进一步降低随机性，避免格式错误
            );

            // 2. 构建请求头
            HttpHeaders headers = createRequestHeaders();

            // 3. 构建请求实体
            HttpEntity<String> requestEntity = createRequestEntity(request, headers);

            // 4. 发送请求并获取响应
            ResponseEntity<String> response = sendPostRequest(requestEntity);

            // 5. 解析并返回有效内容
            return parseApiResponse(response);

        } catch (Exception e) {
            throw new RuntimeException("DeepSeek API调用过程失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建HTTP请求头
     */
    private HttpHeaders createRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());
        return headers;
    }

    /**
     * 构建HTTP请求实体（包含请求体和头信息）
     */
    private HttpEntity<String> createRequestEntity(DeepSeekRequest request, HttpHeaders headers)
            throws JsonProcessingException {
        String requestBody = objectMapper.writeValueAsString(request);
        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * 发送POST请求到DeepSeek API
     */
    private ResponseEntity<String> sendPostRequest(HttpEntity<String> requestEntity) {
        return restTemplate.postForEntity(
                config.getBaseUrl(),
                requestEntity,
                String.class);
    }

    /**
     * 解析API响应，提取并校验结构化JSON内容（先清洗再校验）
     */
    private String parseApiResponse(ResponseEntity<String> response) throws JsonProcessingException {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // 解析响应JSON获取content字段
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode choiceNode = rootNode.path("choices").get(0);
            String content = choiceNode.path("message").path("content").asText();

            // 清洗后校验
            if (isValidJson(content)) {
                return cleanJsonString(content); // 返回清洗后的纯JSON
            } else {
                throw new RuntimeException("API返回内容非JSON格式: " + content);
            }
        } else {
            throw new RuntimeException("API调用失败，状态码: " + response.getStatusCodeValue());
        }
    }

    @Override
    public StructuredReport convertToStructuredMsg(String prePrompt, String unstructuredMsg, String postPrompt) {
        String prompt = buildPrompt(unstructuredMsg);
        String structuredMsg = callDeepSeekApi(prompt);
        try {
            return parseJsonToEntity(cleanJsonString(structuredMsg));
        } catch (Exception e) {
            throw new RuntimeException("解析JSON到实体类时出错", e);
        }
    }
}