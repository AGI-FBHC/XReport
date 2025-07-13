package com.example.medicalreportstructurizer.service;

import java.io.IOException;

import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;

public interface DocumentService {
    byte[] generateDocument(StructuredReport structuredReport, UnstructuredReport unstructuredReport)
            throws IOException;

}
