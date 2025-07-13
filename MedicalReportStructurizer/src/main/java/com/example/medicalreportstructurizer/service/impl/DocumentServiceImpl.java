package com.example.medicalreportstructurizer.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;

import com.example.medicalreportstructurizer.entity.StructuredReport;
import com.example.medicalreportstructurizer.entity.UnstructuredReport;
import com.example.medicalreportstructurizer.service.DocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // private static final DateTimeFormatter DATETIME_FORMATTER =
    // DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_FONT = "宋体"; // 统一使用宋体
    private static final int TABLE_WIDTH = 8000; // 表格总宽度（单位： twentieths of a point）

    @Override
    public byte[] generateDocument(StructuredReport report, UnstructuredReport unstructuredReport) throws IOException {
        if (report == null) {
            report = new StructuredReport();
        }

        XWPFDocument document = new XWPFDocument();

        // 添加标题
        addTitle(document, "结肠癌结构化报告");

        addEmptyParagraph(document);

        // 一、患者基本信息
        addSection(document, "一、患者基本信息", 16);
        XWPFTable infoTable = createStandardTable(document, 1, 6);

        CTTbl ctTbl = infoTable.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() == null ? ctTbl.addNewTblPr() : ctTbl.getTblPr(); // 创建并设置表格边框
        CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        // 设置上边框
        CTBorder topBorder = borders.addNewTop();
        topBorder.setColor("000000"); // 黑色
        topBorder.setSz(new BigInteger("4")); // 4点宽
        topBorder.setVal(STBorder.SINGLE); // 单线
        // 设置下边框

        // CTBorder bottomBorder = borders.addNewBottom();
        // bottomBorder.setVal(STBorder.NIL); // 无边框样式
        // // 设置左边框为透明（无边框）
        // CTBorder leftBorder = borders.addNewLeft();
        // leftBorder.setVal(STBorder.NIL); // 无边框样式

        // // 设置右边框为透明（无边框）
        // CTBorder rightBorder = borders.addNewRight();
        // rightBorder.setVal(STBorder.NIL);

        // // 设置内横边框为透明（无边框）
        // CTBorder insideH = borders.addNewInsideH();
        // insideH.setVal(STBorder.NIL);

        // // 设置内竖边框为透明（无边框）
        // CTBorder insideV = borders.addNewInsideV();
        // insideV.setVal(STBorder.NIL);

        setTableCellValue(infoTable, 0, 0, "姓名", true);
        setTableCellValue(infoTable, 0, 1,
                report.getPatientName() == null || report.getPatientName().trim().isEmpty() ? " "
                        : report.getPatientName().trim(),
                false);
        setTableCellValue(infoTable, 0, 2, "性别", true);
        setTableCellValue(infoTable, 0, 3, getSafeStringValue(report.getGender()), false);
        setTableCellValue(infoTable, 0, 4, "年龄", true);
        setTableCellValue(infoTable, 0, 5, getSafeIntegerValue(report.getAge()), false);
        for (int col = 0; col < 6; col++)
            setTableColumnWidth(infoTable, 0, col, 1300);

        addEmptyParagraph(document);

        XWPFTable info2Table = createStandardTable(document, 1, 4);

        CTTbl ctTbl2 = info2Table.getCTTbl();
        CTTblPr tblPr2 = ctTbl2.getTblPr() == null ? ctTbl2.addNewTblPr() : ctTbl2.getTblPr(); // 创建并设置表格边框
        CTTblBorders borders2 = tblPr2.isSetTblBorders() ? tblPr2.getTblBorders() : tblPr2.addNewTblBorders();
        // 设置下边框
        CTBorder bottomBorder2 = borders2.addNewBottom();
        bottomBorder2.setColor("000000");
        bottomBorder2.setSz(new BigInteger("4"));
        bottomBorder2.setVal(STBorder.SINGLE);
        setTableCellValue(info2Table, 0, 0, "检查编号", true);
        setTableCellValue(info2Table, 0, 1, getSafeStringValue(report.getSerialNumber()), false);
        setTableCellValue(info2Table, 0, 2, "检查日期", true);
        setTableCellValue(info2Table, 0, 3, getSafeDateValue(report.getExamDate()), false);
        // 设置第一列宽度（标题列稍窄）
        for (int col = 0; col < 4; col++)
            setTableColumnWidth(info2Table, 0, col, 2000);
        //

        addEmptyParagraph(document);

        // 二、肿瘤部位信息
        addSection(document, "二、肿瘤部位信息", 16);
        XWPFTable locationTable = createStandardTable(document, 9, 2);
        setTableCellValue(locationTable, 0, 0, "左半结肠", true);
        setTableCellValue(locationTable, 0, 1, getSafeBooleanValue(report.getIsLeftColon()), false);
        setTableCellValue(locationTable, 1, 0, "右半结肠", true);
        setTableCellValue(locationTable, 1, 1, getSafeBooleanValue(report.getIsRightColon()), false);
        setTableCellValue(locationTable, 2, 0, "盲肠", true);
        setTableCellValue(locationTable, 2, 1, getSafeBooleanValue(report.getIsAppendix()), false);
        setTableCellValue(locationTable, 3, 0, "升结肠", true);
        setTableCellValue(locationTable, 3, 1, getSafeBooleanValue(report.getIsAscendingColon()), false);
        setTableCellValue(locationTable, 4, 0, "结肠肝曲", true);
        setTableCellValue(locationTable, 4, 1, getSafeBooleanValue(report.getIsColonLivCerurve()), false);
        setTableCellValue(locationTable, 5, 0, "横结肠", true);
        setTableCellValue(locationTable, 5, 1, getSafeBooleanValue(report.getIsTransverseColon()), false);
        setTableCellValue(locationTable, 6, 0, "结肠脾曲", true);
        setTableCellValue(locationTable, 6, 1, getSafeBooleanValue(report.getIsColonSplenicFlexure()), false);
        setTableCellValue(locationTable, 7, 0, "降结肠", true);
        setTableCellValue(locationTable, 7, 1, getSafeBooleanValue(report.getIsDescendingColon()), false);
        setTableCellValue(locationTable, 8, 0, "乙状结肠", true);
        setTableCellValue(locationTable, 8, 1, getSafeBooleanValue(report.getIsSigmoidColon()), false);
        // setTableColumnWidth(locationTable, 0, 1800);
        // setTableColumnWidth(locationTable, 1, 3200);

        addEmptyParagraph(document);

        // 三、肿瘤大小信息
        addSection(document, "三、肿瘤大小信息", 16);
        XWPFTable sizeTable = createStandardTable(document, 4, 2);
        setTableCellValue(sizeTable, 0, 0, "肿块长度(cm)", true);
        setTableCellValue(sizeTable, 0, 1, getSafeDecimalValue(report.getMassLength()), false);
        setTableCellValue(sizeTable, 1, 0, "肿块宽度(cm)", true);
        setTableCellValue(sizeTable, 1, 1, getSafeDecimalValue(report.getMassWidth()), false);
        setTableCellValue(sizeTable, 2, 0, "肿块高度(cm)", true);
        setTableCellValue(sizeTable, 2, 1, getSafeDecimalValue(report.getMassHeight()), false);
        setTableCellValue(sizeTable, 3, 0, "肠壁最厚处(cm)", true);
        setTableCellValue(sizeTable, 3, 1, getSafeDecimalValue(report.getWallThickness()), false);
        // setTableColumnWidth(sizeTable, 0, 1800);
        // setTableColumnWidth(sizeTable, 1, 3200);

        addEmptyParagraph(document);

        // 四、肿瘤分期信息
        addSection(document, "四、肿瘤分期信息", 16);
        XWPFTable stageTable = createStandardTable(document, 6, 2);
        setTableCellValue(stageTable, 0, 0, "侵犯至黏膜下层(T1)", true);
        setTableCellValue(stageTable, 0, 1, getSafeBooleanValue(report.getIsT1()), false);
        setTableCellValue(stageTable, 1, 0, "侵犯固有肌层(T2)", true);
        setTableCellValue(stageTable, 1, 1, getSafeBooleanValue(report.getIsT2()), false);
        setTableCellValue(stageTable, 2, 0, "突破固有肌层(T3)<5mm", true);
        setTableCellValue(stageTable, 2, 1, getSafeBooleanValue(report.getIsT3Less5mm()), false);
        setTableCellValue(stageTable, 3, 0, "突破固有肌层(T3)>5mm", true);
        setTableCellValue(stageTable, 3, 1, getSafeBooleanValue(report.getIsT3More5mm()), false);
        setTableCellValue(stageTable, 4, 0, "侵犯脏层腹膜(T4a)", true);
        setTableCellValue(stageTable, 4, 1, getSafeBooleanValue(report.getIsT4a()), false);
        setTableCellValue(stageTable, 5, 0, "侵犯邻近结构(T4b)", true);
        setTableCellValue(stageTable, 5, 1, getSafeBooleanValue(report.getIsT4b()), false);
        // setTableColumnWidth(stageTable, 0, 2000);
        // setTableColumnWidth(stageTable, 1, 3000);

        addEmptyParagraph(document);

        // 五、淋巴结信息
        addSection(document, "五、淋巴结信息", 16);
        XWPFTable lymphTable = createStandardTable(document, 4, 2);
        setTableCellValue(lymphTable, 0, 0, "区域淋巴结数目", true);
        setTableCellValue(lymphTable, 0, 1, getSafeIntegerValue(report.getRegionalLymphNodeCount()), false);
        setTableCellValue(lymphTable, 1, 0, "最大淋巴结短径(cm)", true);
        setTableCellValue(lymphTable, 1, 1, getSafeDecimalValue(report.getLargestLymphNodeSize()), false);
        setTableCellValue(lymphTable, 2, 0, "腹膜后淋巴结数目", true);
        setTableCellValue(lymphTable, 2, 1, getSafeIntegerValue(report.getRetroperitonealLymphNodeCount()), false);
        setTableCellValue(lymphTable, 3, 0, "最大腹膜后淋巴结短径(cm)", true);
        setTableCellValue(lymphTable, 3, 1, getSafeDecimalValue(report.getLargestRetroperitonealLymphNodeSize()),
                false);
        // setTableColumnWidth(lymphTable, 0, 2000);
        // setTableColumnWidth(lymphTable, 1, 3000);

        addEmptyParagraph(document);

        // 六、转移信息
        addSection(document, "六、转移信息", 16);
        XWPFTable metastasisTable = createStandardTable(document, 6, 2);
        setTableCellValue(metastasisTable, 0, 0, "肠壁外血管侵犯阳性", true);
        setTableCellValue(metastasisTable, 0, 1, getSafeBooleanValue(report.getIsEmviPositive()), false);
        setTableCellValue(metastasisTable, 1, 0, "肝转移", true);
        setTableCellValue(metastasisTable, 1, 1, getSafeBooleanValue(report.getHasLiverMetastasis()), false);
        setTableCellValue(metastasisTable, 2, 0, "左肺转移", true);
        setTableCellValue(metastasisTable, 2, 1, getSafeBooleanValue(report.getHasLeftLungMetastasis()), false);
        setTableCellValue(metastasisTable, 3, 0, "右肺转移", true);
        setTableCellValue(metastasisTable, 3, 1, getSafeBooleanValue(report.getHasRightLungMetastasis()), false);
        setTableCellValue(metastasisTable, 4, 0, "腹膜转移", true);
        setTableCellValue(metastasisTable, 4, 1, getSafeBooleanValue(report.getHasPeritonealMetastasis()), false);
        setTableCellValue(metastasisTable, 5, 0, "其他转移部位", true);
        setTableCellValue(metastasisTable, 5, 1, getSafeBooleanValue(report.getHasOtherMetastasisDetails()), false);
        // setTableColumnWidth(metastasisTable, 0, 1800);
        // setTableColumnWidth(metastasisTable, 1, 3200);

        addEmptyParagraph(document);

        // 七、其他临床信息
        addSection(document, "七、其他临床信息", 16);
        XWPFTable clinicalTable = createStandardTable(document, 3, 2);
        setTableCellValue(clinicalTable, 0, 0, "临床诊断", true);
        setTableCellValue(clinicalTable, 0, 1, getSafeStringValue(report.getClinicalDiagnosis()), false);
        setTableCellValue(clinicalTable, 1, 0, "肠梗阻", true);
        setTableCellValue(clinicalTable, 1, 1, getSafeBooleanValue(report.getHasBowelObstruction()), false);
        setTableCellValue(clinicalTable, 2, 0, "肠穿孔", true);
        setTableCellValue(clinicalTable, 2, 1, getSafeBooleanValue(report.getHasBowelPerforation()), false);
        // setTableColumnWidth(clinicalTable, 0, 1800);
        // setTableColumnWidth(clinicalTable, 1, 3200);

        addEmptyParagraph(document);

        // 八、诊断结论与建议
        addSection(document, "八、诊断结论与建议", 16);
        addParagraph(document, "诊断结论：");
        addDetailParagraph(document, getSafeStringValue(report.getDiagnosisConclusion()));
        addParagraph(document, "治疗建议：");
        addDetailParagraph(document, getSafeStringValue(report.getTreatmentSuggestion()));
        // addParagraph(document, "报告医生：");
        // addDetailParagraph(document, getSafeStringValue(report.getRadiologist()));
        // addParagraph(document, "报告日期：");
        // addDetailParagraph(document,
        // getSafeDateTimeValue(report.getReportDate() != null ?
        // report.getReportDate().toLocalDate() : null));

        // 输出到字节流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();
        return out.toByteArray();
    }

    /**
     * 创建标准格式表格（带完整边框、统一宽度）
     */
    private XWPFTable createStandardTable(XWPFDocument document, int rows, int cols) {
        XWPFTable table = document.createTable(rows, cols);
        // 设置表格宽度
        CTTblPr tblPr = table.getCTTbl().getTblPr() == null ? table.getCTTbl().addNewTblPr()
                : table.getCTTbl().getTblPr();
        CTTblWidth tblWidth = tblPr.addNewTblW();
        tblWidth.setW(BigDecimal.valueOf(TABLE_WIDTH));
        tblWidth.setType(STTblWidth.DXA);

        CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        // 设置上边框
        CTBorder topBorder = borders.addNewTop();
        topBorder.setVal(STBorder.NIL); // 无边框样式
        // 设置下边框

        CTBorder bottomBorder = borders.addNewBottom();
        bottomBorder.setVal(STBorder.NIL); // 无边框样式
        // 设置左边框为透明（无边框）
        CTBorder leftBorder = borders.addNewLeft();
        leftBorder.setVal(STBorder.NIL); // 无边框样式

        // 设置右边框为透明（无边框）
        CTBorder rightBorder = borders.addNewRight();
        rightBorder.setVal(STBorder.NIL);

        // 设置内横边框为透明（无边框）
        CTBorder insideH = borders.addNewInsideH();
        insideH.setVal(STBorder.NIL);

        // 设置内竖边框为透明（无边框）
        CTBorder insideV = borders.addNewInsideV();
        insideV.setVal(STBorder.NIL);
        return table;
    }

    /**
     * 设置边框样式和粗细
     */
    private void setBorder(CTBorder border, BigInteger size) {
        border.setVal(STBorder.SINGLE);
        border.setSz(size);
    }

    /**
     * 设置表格列宽度
     */
    private void setTableColumnWidth(XWPFTable table, int rowIndex, int colIndex, int width) {
        if (rowIndex < 0 || rowIndex >= table.getRows().size()) {
            throw new IllegalArgumentException("Invalid row index: " + rowIndex);
        }
        XWPFTableRow row = table.getRow(rowIndex);
        XWPFTableCell cell = row.getCell(colIndex);
        CTTblWidth cellWidth = cell.getCTTc().addNewTcPr().addNewTcW();
        cellWidth.setW(BigDecimal.valueOf(width));
        cellWidth.setType(STTblWidth.DXA);
    }

    /**
     * 添加标题（居中、加粗、20号字体）
     */
    private void addTitle(XWPFDocument document, String title) {
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(20);
        titleRun.setFontFamily(DEFAULT_FONT);
    }

    /**
     * 添加章节标题（16号字体、加粗）
     */
    private void addSection(XWPFDocument document, String sectionTitle, int fontSize) {
        XWPFParagraph sectionParagraph = document.createParagraph();
        XWPFRun sectionRun = sectionParagraph.createRun();
        sectionRun.setText(sectionTitle);
        sectionRun.setBold(true);
        sectionRun.setFontSize(fontSize);
        sectionRun.setFontFamily(DEFAULT_FONT);
        // 段落前间距
        sectionParagraph.setSpacingBefore(120);
    }

    /**
     * 添加普通段落（12号字体）
     */
    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        run.setFontFamily(DEFAULT_FONT);
        // 段落前间距
        paragraph.setSpacingBefore(60);
    }

    /**
     * 添加详情段落（缩进、12号字体）
     */
    private void addDetailParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        // 设置首行缩进（2字符）
        paragraph.setFirstLineIndent(240); // 1字符≈120 twentieths of a point
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        run.setFontFamily(DEFAULT_FONT);
    }

    /**
     * 添加空段落（控制间距）
     */
    private void addEmptyParagraph(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("");
        paragraph.setSpacingBefore(60);
        paragraph.setSpacingAfter(60);
    }

    /**
     * 设置表格单元格值（统一格式）
     */
    private void setTableCellValue(XWPFTable table, int row, int col, String text, boolean isHeader) {
        // 清空单元格原有内容（兼容低版本POI）
        XWPFTableCell cell = table.getRow(row).getCell(col);
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }

        // 添加新段落
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(12);
        run.setFontFamily(DEFAULT_FONT);
        if (isHeader) {
            run.setBold(true);
        }
    }

    // 安全处理字符串
    private String getSafeStringValue(String value) {
        return value == null || value.trim().isEmpty() ? "[  ]" : value.trim();
    }

    // 安全处理日期
    private String getSafeDateValue(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "[  ]";
    }

    // 安全处理日期时间
    // private String getSafeDateTimeValue(LocalDate date) {
    // return date != null ? date.format(DATETIME_FORMATTER) : "[ ]";
    // }

    // 安全处理BigDecimal
    private String getSafeDecimalValue(BigDecimal value) {
        return value != null ? value.toPlainString() : "[  ]";
    }

    // 安全处理布尔值
    private String getSafeBooleanValue(Boolean value) {
        return value != null ? (value ? "[是]" : "[否]") : "[  ]";
    }

    // 安全处理整数
    private String getSafeIntegerValue(Integer value) {
        return value != null ? value.toString() : "[  ]";
    }

}