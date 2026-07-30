package com.simats.neoomfs.controller;

import com.simats.neoomfs.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Export", description = "Endpoints for exporting patients, reports, and analytics in CSV, Excel, and PDF formats")
@PreAuthorize("hasAnyRole('ROLE_DOCTOR','ROLE_ADMIN','ROLE_FACULTY')")
public class ExportController {

    private final ExportService exportService;

    @Operation(summary = "Export patients data list")
    @GetMapping("/patients")
    public ResponseEntity<byte[]> exportPatients(@RequestParam(defaultValue = "csv") String format) {
        try {
            byte[] data;
            String fileName;
            String contentType;

            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    data = exportService.exportPatientsExcel();
                    fileName = "patients_export.xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "pdf":
                    data = exportService.exportPatientsPdf();
                    fileName = "patients_export.pdf";
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                    break;
                case "csv":
                default:
                    data = exportService.exportPatientsCsv();
                    fileName = "patients_export.csv";
                    contentType = "text/csv";
                    break;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);

        } catch (Exception e) {
            log.error("Failed to export patients", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Export assessment reports list")
    @GetMapping("/reports")
    public ResponseEntity<byte[]> exportReports(@RequestParam(defaultValue = "csv") String format) {
        try {
            byte[] data;
            String fileName;
            String contentType;

            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    data = exportService.exportReportsExcel();
                    fileName = "reports_export.xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "pdf":
                    data = exportService.exportReportsPdf();
                    fileName = "reports_export.pdf";
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                    break;
                case "csv":
                default:
                    data = exportService.exportReportsCsv();
                    fileName = "reports_export.csv";
                    contentType = "text/csv";
                    break;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);

        } catch (Exception e) {
            log.error("Failed to export reports", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Export clinic statistics and metrics summary")
    @GetMapping("/analytics")
    public ResponseEntity<byte[]> exportAnalytics(@RequestParam(defaultValue = "csv") String format) {
        try {
            byte[] data;
            String fileName;
            String contentType;

            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    data = exportService.exportAnalyticsExcel();
                    fileName = "analytics_export.xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
                case "pdf":
                    data = exportService.exportAnalyticsPdf();
                    fileName = "analytics_export.pdf";
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                    break;
                case "csv":
                default:
                    data = exportService.exportAnalyticsCsv();
                    fileName = "analytics_export.csv";
                    contentType = "text/csv";
                    break;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);

        } catch (Exception e) {
            log.error("Failed to export analytics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
