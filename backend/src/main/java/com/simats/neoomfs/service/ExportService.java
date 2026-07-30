package com.simats.neoomfs.service;

import java.io.IOException;

public interface ExportService {
    byte[] exportPatientsCsv();
    byte[] exportPatientsExcel() throws IOException;
    byte[] exportPatientsPdf();

    byte[] exportReportsCsv();
    byte[] exportReportsExcel() throws IOException;
    byte[] exportReportsPdf();

    byte[] exportAnalyticsCsv();
    byte[] exportAnalyticsExcel() throws IOException;
    byte[] exportAnalyticsPdf();
}
