package com.example.medicalreportstructurizer.service;

import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface DeepSeekApiService {

    StructuredReport convertToStructuredMsg(String prePrompt, String unstructuredMsg, String postPrompt);
}
