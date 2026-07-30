package com.simats.neoomfs.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.simats.neoomfs.dto.response.AnalyticsResponse;
import com.simats.neoomfs.entity.AssessmentReport;
import com.simats.neoomfs.entity.Patient;
import com.simats.neoomfs.repository.AssessmentReportRepository;
import com.simats.neoomfs.repository.PatientRepository;
import com.simats.neoomfs.service.AnalyticsService;
import com.simats.neoomfs.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final PatientRepository patientRepository;
    private final AssessmentReportRepository reportRepository;
    private final AnalyticsService analyticsService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ── Patients Export ──────────────────────────────────────────────────────

    @Override
    public byte[] exportPatientsCsv() {
        List<Patient> patients = patientRepository.findByDeletedFalse();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,MRN,Full Name,Age,Gender,Blood Group,Phone,Referring Doctor,Status,Created By,Created At\n");

        for (Patient p : patients) {
            csv.append(p.getId()).append(",")
                    .append(escapeCsv(p.getMrn())).append(",")
                    .append(escapeCsv(p.getFullName())).append(",")
                    .append(p.getAge() != null ? p.getAge() : "").append(",")
                    .append(escapeCsv(p.getGender())).append(",")
                    .append(escapeCsv(p.getBloodGroup())).append(",")
                    .append(escapeCsv(p.getPhoneNumber())).append(",")
                    .append(escapeCsv(p.getReferringDoctor())).append(",")
                    .append(p.getAssessmentStatus() != null ? p.getAssessmentStatus().name() : "").append(",")
                    .append(escapeCsv(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : "SYSTEM")).append(",")
                    .append(p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_TIME_FORMATTER) : "").append("\n");
        }
        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportPatientsExcel() throws IOException {
        List<Patient> patients = patientRepository.findByDeletedFalse();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Patients");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "MRN", "Full Name", "Age", "Gender", "Blood Group", "Phone", "Referring Doctor", "Status", "Created By", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Patient p : patients) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getMrn());
                row.createCell(2).setCellValue(p.getFullName());
                row.createCell(3).setCellValue(p.getAge() != null ? p.getAge() : 0);
                row.createCell(4).setCellValue(p.getGender());
                row.createCell(5).setCellValue(p.getBloodGroup());
                row.createCell(6).setCellValue(p.getPhoneNumber());
                row.createCell(7).setCellValue(p.getReferringDoctor());
                row.createCell(8).setCellValue(p.getAssessmentStatus() != null ? p.getAssessmentStatus().name() : "");
                row.createCell(9).setCellValue(p.getCreatedBy() != null ? p.getCreatedBy().getFullName() : "SYSTEM");
                row.createCell(10).setCellValue(p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_TIME_FORMATTER) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportPatientsPdf() {
        List<Patient> patients = patientRepository.findByDeletedFalse();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(26, 54, 93));
            Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);

            Paragraph title = new Paragraph("Patient Directory Export", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 3f, 1f, 1.5f, 2.5f, 2.5f, 2f, 3.5f});

            String[] headers = {"ID", "MRN", "Name", "Age", "Gender", "Phone", "Referring Dr", "Status", "Created At"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(44, 82, 130));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (Patient p : patients) {
                table.addCell(new Phrase(String.valueOf(p.getId()), cellFont));
                table.addCell(new Phrase(p.getMrn(), cellFont));
                table.addCell(new Phrase(p.getFullName(), cellFont));
                table.addCell(new Phrase(p.getAge() != null ? String.valueOf(p.getAge()) : "", cellFont));
                table.addCell(new Phrase(p.getGender(), cellFont));
                table.addCell(new Phrase(p.getPhoneNumber() != null ? p.getPhoneNumber() : "", cellFont));
                table.addCell(new Phrase(p.getReferringDoctor() != null ? p.getReferringDoctor() : "", cellFont));
                table.addCell(new Phrase(p.getAssessmentStatus() != null ? p.getAssessmentStatus().name() : "", cellFont));
                table.addCell(new Phrase(p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_TIME_FORMATTER) : "", cellFont));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate patients PDF export", e);
        }
        return out.toByteArray();
    }

    // ── Reports Export ───────────────────────────────────────────────────────

    @Override
    public byte[] exportReportsCsv() {
        List<AssessmentReport> reports = reportRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Patient MRN,Patient Name,Report File Name,Version,Generated By,Generated At\n");

        for (AssessmentReport r : reports) {
            csv.append(r.getId()).append(",")
                    .append(escapeCsv(r.getPatient() != null ? r.getPatient().getMrn() : "")).append(",")
                    .append(escapeCsv(r.getPatient() != null ? r.getPatient().getFullName() : "")).append(",")
                    .append(escapeCsv(r.getReportFileName())).append(",")
                    .append(r.getReportVersion()).append(",")
                    .append(escapeCsv(r.getGeneratedBy() != null ? r.getGeneratedBy().getFullName() : "SYSTEM")).append(",")
                    .append(r.getReportGeneratedAt() != null ? r.getReportGeneratedAt().format(DATE_TIME_FORMATTER) : "").append("\n");
        }
        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportReportsExcel() throws IOException {
        List<AssessmentReport> reports = reportRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reports");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Patient MRN", "Patient Name", "Report File Name", "Version", "Generated By", "Generated At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (AssessmentReport r : reports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getPatient() != null ? r.getPatient().getMrn() : "");
                row.createCell(2).setCellValue(r.getPatient() != null ? r.getPatient().getFullName() : "");
                row.createCell(3).setCellValue(r.getReportFileName());
                row.createCell(4).setCellValue(r.getReportVersion());
                row.createCell(5).setCellValue(r.getGeneratedBy() != null ? r.getGeneratedBy().getFullName() : "SYSTEM");
                row.createCell(6).setCellValue(r.getReportGeneratedAt() != null ? r.getReportGeneratedAt().format(DATE_TIME_FORMATTER) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportReportsPdf() {
        List<AssessmentReport> reports = reportRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(26, 54, 93));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);

            Paragraph title = new Paragraph("Fitness Assessment Reports Export", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 3f, 4f, 1.5f, 3.5f});

            String[] headers = {"ID", "Patient MRN", "Patient Name", "Report File Name", "Version", "Generated At"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(44, 82, 130));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (AssessmentReport r : reports) {
                table.addCell(new Phrase(String.valueOf(r.getId()), cellFont));
                table.addCell(new Phrase(r.getPatient() != null ? r.getPatient().getMrn() : "", cellFont));
                table.addCell(new Phrase(r.getPatient() != null ? r.getPatient().getFullName() : "", cellFont));
                table.addCell(new Phrase(r.getReportFileName(), cellFont));
                table.addCell(new Phrase(String.valueOf(r.getReportVersion()), cellFont));
                table.addCell(new Phrase(r.getReportGeneratedAt() != null ? r.getReportGeneratedAt().format(DATE_TIME_FORMATTER) : "", cellFont));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate reports PDF export", e);
        }
        return out.toByteArray();
    }

    // ── Analytics Export ─────────────────────────────────────────────────────

    @Override
    public byte[] exportAnalyticsCsv() {
        AnalyticsResponse response = analyticsService.getAnalytics();
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        csv.append("Total Patients,").append(response.getTotalPatients()).append("\n");
        csv.append("Patients Registered This Month,").append(response.getPatientsThisMonth()).append("\n");
        csv.append("Reports Generated,").append(response.getReportsGenerated()).append("\n");
        csv.append("Average Age,").append(String.format("%.1f", response.getAverageAge())).append("\n");
        csv.append("Male Count,").append(response.getMaleCount()).append("\n");
        csv.append("Female Count,").append(response.getFemaleCount()).append("\n");
        csv.append("Male-to-Female Ratio,").append(response.getMaleFemaleRatio()).append("\n");

        for (Map.Entry<String, Long> entry : response.getRiskDistribution().entrySet()) {
            csv.append("Risk Level (").append(entry.getKey()).append("),").append(entry.getValue()).append("\n");
        }
        for (Map.Entry<String, Long> entry : response.getFitnessDecisionDistribution().entrySet()) {
            csv.append("Fitness Status (").append(entry.getKey()).append("),").append(entry.getValue()).append("\n");
        }

        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportAnalyticsExcel() throws IOException {
        AnalyticsResponse response = analyticsService.getAnalytics();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Clinic Statistics");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Metric Name");
            headerRow.createCell(1).setCellValue("Metric Value");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.getCell(1).setCellStyle(headerStyle);

            int rowIdx = 1;
            rowIdx = createExcelRow(sheet, rowIdx, "Total Patients", response.getTotalPatients());
            rowIdx = createExcelRow(sheet, rowIdx, "Patients Registered This Month", response.getPatientsThisMonth());
            rowIdx = createExcelRow(sheet, rowIdx, "Reports Generated", response.getReportsGenerated());
            rowIdx = createExcelRow(sheet, rowIdx, "Average Age", response.getAverageAge());
            rowIdx = createExcelRow(sheet, rowIdx, "Male Patients Count", response.getMaleCount());
            rowIdx = createExcelRow(sheet, rowIdx, "Female Patients Count", response.getFemaleCount());
            rowIdx = createExcelRow(sheet, rowIdx, "Male-to-Female Ratio", response.getMaleFemaleRatio());

            for (Map.Entry<String, Long> entry : response.getRiskDistribution().entrySet()) {
                rowIdx = createExcelRow(sheet, rowIdx, "Risk Level: " + entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, Long> entry : response.getFitnessDecisionDistribution().entrySet()) {
                rowIdx = createExcelRow(sheet, rowIdx, "Fitness Clearance: " + entry.getKey(), entry.getValue());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public byte[] exportAnalyticsPdf() {
        AnalyticsResponse response = analyticsService.getAnalytics();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(26, 54, 93));
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font cellFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
            Font boldCellFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);

            Paragraph title = new Paragraph("Clinic Performance & Analytics Summary", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.setWidths(new float[]{3f, 2f});

            PdfPCell cell1 = new PdfPCell(new Phrase("Clinical Metric Description", headerFont));
            cell1.setBackgroundColor(new Color(44, 82, 130));
            cell1.setPadding(6);
            table.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase("Stat Value", headerFont));
            cell2.setBackgroundColor(new Color(44, 82, 130));
            cell2.setPadding(6);
            table.addCell(cell2);

            addAnalyticsRow(table, "Total Active Patients", String.valueOf(response.getTotalPatients()), boldCellFont, cellFont);
            addAnalyticsRow(table, "Patients Registered This Month", String.valueOf(response.getPatientsThisMonth()), boldCellFont, cellFont);
            addAnalyticsRow(table, "Preoperative Reports Generated", String.valueOf(response.getReportsGenerated()), boldCellFont, cellFont);
            addAnalyticsRow(table, "Patient Population Average Age", String.format("%.1f yrs", response.getAverageAge()), boldCellFont, cellFont);
            addAnalyticsRow(table, "Male Population Count", String.valueOf(response.getMaleCount()), boldCellFont, cellFont);
            addAnalyticsRow(table, "Female Population Count", String.valueOf(response.getFemaleCount()), boldCellFont, cellFont);
            addAnalyticsRow(table, "Male-to-Female Ratio", response.getMaleFemaleRatio(), boldCellFont, cellFont);

            for (Map.Entry<String, Long> entry : response.getRiskDistribution().entrySet()) {
                addAnalyticsRow(table, "Risk Categorization: " + entry.getKey(), String.valueOf(entry.getValue()), boldCellFont, cellFont);
            }
            for (Map.Entry<String, Long> entry : response.getFitnessDecisionDistribution().entrySet()) {
                addAnalyticsRow(table, "Clearance Status: " + entry.getKey(), String.valueOf(entry.getValue()), boldCellFont, cellFont);
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            log.error("Failed to generate analytics PDF export", e);
        }
        return out.toByteArray();
    }

    // ── Helper Exporter Logic ────────────────────────────────────────────────

    private String escapeCsv(String val) {
        if (val == null) return "";
        String escaped = val.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private int createExcelRow(Sheet sheet, int idx, String name, Object val) {
        Row row = sheet.createRow(idx);
        row.createCell(0).setCellValue(name);
        if (val instanceof Number) {
            row.createCell(1).setCellValue(((Number) val).doubleValue());
        } else {
            row.createCell(1).setCellValue(val != null ? val.toString() : "");
        }
        return idx + 1;
    }

    private void addAnalyticsRow(PdfPTable table, String name, String val, Font keyFont, Font valFont) {
        table.addCell(new Phrase(name, keyFont));
        table.addCell(new Phrase(val, valFont));
    }
}
