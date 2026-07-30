package com.simats.neoomfs.pdf;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.simats.neoomfs.entity.*;
import com.simats.neoomfs.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ReportGeneratorService {

    @Value("${storage.reports-dir:backend/reports}")
    private String reportsDirStr;

    public byte[] generateReportPdf(Patient patient, PatientVitals vitals, Radiology radiology,
                                     LaboratoryInvestigations labs, MedicalHistory history,
                                     DentalExamination dental, ClinicalDecision decision) {
        log.info("Generating PDF report for patient MRN: {}", patient.getMrn());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Add Header/Footer helper
            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            // Font configurations
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(26, 54, 93));
            Font sectionHeaderFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(44, 82, 130));
            Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
            Font alertFont = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(155, 44, 44));

            // Document Header / Title
            Paragraph title = new Paragraph("NeoOMFS Preoperative Assessment Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            // Demographics Section
            addSectionTitle(document, "1. Patient Demographics", sectionHeaderFont);
            PdfPTable demoTable = new PdfPTable(4);
            demoTable.setWidthPercentage(100);
            demoTable.setWidths(new float[]{1.2f, 1.8f, 1.2f, 1.8f});

            addCell(demoTable, "Patient Name:", boldFont);
            addCell(demoTable, patient.getFullName(), normalFont);
            addCell(demoTable, "MRN / ID:", boldFont);
            addCell(demoTable, patient.getMrn(), normalFont);

            addCell(demoTable, "Age:", boldFont);
            addCell(demoTable, String.valueOf(patient.getAge()), normalFont);
            addCell(demoTable, "Gender:", boldFont);
            addCell(demoTable, patient.getGender(), normalFont);

            addCell(demoTable, "Date of Birth:", boldFont);
            addCell(demoTable, DateUtil.formatDate(patient.getDateOfBirth()), normalFont);
            addCell(demoTable, "Blood Group:", boldFont);
            addCell(demoTable, patient.getBloodGroup() != null ? patient.getBloodGroup() : "Not Tested", normalFont);

            addCell(demoTable, "Phone Number:", boldFont);
            addCell(demoTable, patient.getPhoneNumber(), normalFont);
            addCell(demoTable, "Emergency Contact:", boldFont);
            addCell(demoTable, patient.getEmergencyContact() != null ? patient.getEmergencyContact() : "N/A", normalFont);

            document.add(demoTable);
            document.add(new Paragraph("\n"));

            // Vitals Section
            if (vitals != null) {
                addSectionTitle(document, "2. Clinical Vitals", sectionHeaderFont);
                PdfPTable vitalsTable = new PdfPTable(4);
                vitalsTable.setWidthPercentage(100);
                vitalsTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});

                addCell(vitalsTable, "Blood Pressure:", boldFont);
                addCell(vitalsTable, vitals.getBpSystolic() + "/" + vitals.getBpDiastolic() + " mmHg", normalFont);
                addCell(vitalsTable, "Pulse Rate:", boldFont);
                addCell(vitalsTable, vitals.getPulseRate() + " bpm", normalFont);

                addCell(vitalsTable, "Temperature:", boldFont);
                addCell(vitalsTable, vitals.getTemperature() + " °C", normalFont);
                addCell(vitalsTable, "SpO2 Oxygen Saturation:", boldFont);
                addCell(vitalsTable, (vitals.getSpo2() != null ? vitals.getSpo2().intValue() : 0) + " %", normalFont);

                addCell(vitalsTable, "Height / Weight:", boldFont);
                addCell(vitalsTable, vitals.getHeightCm() + " cm / " + vitals.getWeightKg() + " kg", normalFont);
                addCell(vitalsTable, "Body Mass Index (BMI):", boldFont);
                addCell(vitalsTable, vitals.getBmi() != null ? vitals.getBmi() + " kg/m²" : "N/A", normalFont);

                document.add(vitalsTable);
                document.add(new Paragraph("\n"));
            }

            // Laboratory Investigations Section
            if (labs != null) {
                addSectionTitle(document, "3. Laboratory Parameters", sectionHeaderFont);
                PdfPTable labTable = new PdfPTable(4);
                labTable.setWidthPercentage(100);
                labTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});

                addCell(labTable, "Hemoglobin (Hb):", boldFont);
                addCell(labTable, labs.getHemoglobin() != null ? labs.getHemoglobin() + " g/dL" : "N/A", normalFont);
                addCell(labTable, "Platelet Count:", boldFont);
                addCell(labTable, labs.getPlateletCount() != null ? labs.getPlateletCount() + " /mcL" : "N/A", normalFont);

                addCell(labTable, "Blood Sugar (RBS/FBS):", boldFont);
                addCell(labTable, "Fasting: " + labs.getFastingBloodSugar() + " / Random: " + labs.getRandomBloodSugar(), normalFont);
                addCell(labTable, "HbA1c Glycated Hb:", boldFont);
                addCell(labTable, labs.getHba1c() != null ? labs.getHba1c() + " %" : "N/A", normalFont);

                addCell(labTable, "Serum Creatinine:", boldFont);
                addCell(labTable, labs.getSerumCreatinine() != null ? labs.getSerumCreatinine() + " mg/dL" : "N/A", normalFont);
                addCell(labTable, "Prothrombin Time / INR:", boldFont);
                addCell(labTable, "INR: " + labs.getInr() + " / PT: " + labs.getPt() + "s", normalFont);

                addCell(labTable, "HIV Serology Status:", boldFont);
                addCell(labTable, labs.getHivStatus() != null ? labs.getHivStatus() : "Not Tested", normalFont);
                addCell(labTable, "Hepatitis (HBsAg/HCV):", boldFont);
                addCell(labTable, "HBsAg: " + labs.getHbsagStatus() + " / HCV: " + labs.getHcvStatus(), normalFont);

                document.add(labTable);
                document.add(new Paragraph("\n"));
            }

            // Dental Examination Section
            if (dental != null) {
                addSectionTitle(document, "4. Dental & Maxillofacial Findings", sectionHeaderFont);
                PdfPTable dentalTable = new PdfPTable(4);
                dentalTable.setWidthPercentage(100);
                dentalTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});

                addCell(dentalTable, "Pell-Gregory Class:", boldFont);
                addCell(dentalTable, dental.getPellGregoryClass(), normalFont);
                addCell(dentalTable, "Winter's Position:", boldFont);
                addCell(dentalTable, dental.getWinterClassification(), normalFont);

                addCell(dentalTable, "Third Molars:", boldFont);
                addCell(dentalTable, "Upper: " + dental.getUpperThirdMolar() + " / Lower: N/A", normalFont);
                addCell(dentalTable, "Difficulty Index Score:", boldFont);
                addCell(dentalTable, String.valueOf(dental.getDifficultyScore()), normalFont);

                addCell(dentalTable, "Mouth Opening (mm):", boldFont);
                addCell(dentalTable, dental.getMouthOpeningMm() + " mm", normalFont);
                addCell(dentalTable, "ASA Physical Status:", boldFont);
                addCell(dentalTable, dental.getAsaClass() != null ? dental.getAsaClass() : "N/A", normalFont);

                document.add(dentalTable);
                document.add(new Paragraph("\n"));
            }

            // Decision Results Section
            if (decision != null) {
                addSectionTitle(document, "5. Preoperative Risk & Fitness Clearance", sectionHeaderFont);

                PdfPTable decisionTable = new PdfPTable(2);
                decisionTable.setWidthPercentage(100);
                decisionTable.setWidths(new float[]{2f, 4f});

                addCell(decisionTable, "Calculated Risk Score:", boldFont);
                addCell(decisionTable, decision.getRiskScore() + " / 100", normalFont);

                addCell(decisionTable, "Preoperative Risk Level:", boldFont);
                addCell(decisionTable, decision.getRiskLevel() != null ? decision.getRiskLevel().name() : "N/A", boldFont);

                addCell(decisionTable, "Fitness Status Decision:", boldFont);
                addCell(decisionTable, decision.getFitnessDecision() != null ? decision.getFitnessDecision().name() : "N/A", alertFont);

                addCell(decisionTable, "Specialist Clinical Alerts:", boldFont);
                addCell(decisionTable, decision.getClinicalAlerts() != null ? decision.getClinicalAlerts() : "No alerts", normalFont);

                addCell(decisionTable, "Risk-Mitigation Guidance:", boldFont);
                addCell(decisionTable, decision.getRecommendations() != null ? decision.getRecommendations() : "Routine care", normalFont);

                addCell(decisionTable, "Surgeon Decision Notes:", boldFont);
                addCell(decisionTable, decision.getDecisionNotes() != null ? decision.getDecisionNotes() : "N/A", normalFont);

                document.add(decisionTable);
                document.add(new Paragraph("\n"));
            }

            // Signatures block
            document.add(new Paragraph("\n\n"));
            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setWidths(new float[]{3f, 3f});

            PdfPCell c1 = new PdfPCell(new Paragraph("___________________________\nSurgeon / Evaluated By Signature", normalFont));
            c1.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell c2 = new PdfPCell(new Paragraph("___________________________\nPreoperative Chief Clearance", normalFont));
            c2.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);

            signTable.addCell(c1);
            signTable.addCell(c2);
            document.add(signTable);

            document.close();

        } catch (Exception e) {
            log.error("Failed to generate PDF document", e);
        }

        return out.toByteArray();
    }

    private void addSectionTitle(Document doc, String titleStr, Font font) throws DocumentException {
        Paragraph p = new Paragraph(titleStr, font);
        p.setSpacingBefore(10);
        p.setSpacingAfter(5);
        doc.add(p);
        // Add divider line
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthBottom(1f);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBorder(com.lowagie.text.Rectangle.BOTTOM);
        line.addCell(cell);
        doc.add(line);
        doc.add(new Paragraph("\n"));
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", font));
        cell.setPadding(5);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }

    public String saveReportToDisk(byte[] pdfBytes, String mrn) {
        try {
            Path reportsDir = Paths.get(reportsDirStr).toAbsolutePath().normalize();
            Files.createDirectories(reportsDir);

            String fileName = "PreOp_Assessment_" + mrn + "_" + UUID.randomUUID().toString() + ".pdf";
            Path targetPath = reportsDir.resolve(fileName);
            Files.write(targetPath, pdfBytes);

            log.info("Saved report PDF file at: {}", targetPath);
            return targetPath.toString();

        } catch (Exception e) {
            log.error("Could not write assessment report PDF to disk", e);
            throw new RuntimeException("Could not save assessment report PDF on disk", e);
        }
    }

    // Inner class for footer and page numbers
    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(113, 128, 150));

            // Print footer
            String footerText = "NeoOMFS Clinical Assessment Platform | Date: " + DateUtil.todayFormatted() + " | Page " + writer.getPageNumber();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(footerText, footerFont),
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}
