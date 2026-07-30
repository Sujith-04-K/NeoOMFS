package com.simats.neoomfs.scheduler;

import com.simats.neoomfs.entity.AssessmentReport;
import com.simats.neoomfs.repository.AssessmentReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReportCleanupScheduler {

    private final AssessmentReportRepository reportRepository;

    // Run daily at midnight: delete PDF report documents older than 30 days
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldReports() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        log.info("Starting cleanup of PDF reports generated before: {}", cutoff);

        List<AssessmentReport> oldReports = reportRepository.findByReportGeneratedAtBefore(cutoff);
        for (AssessmentReport report : oldReports) {
            try {
                File f = new File(report.getReportFilePath());
                if (f.exists()) {
                    if (f.delete()) {
                        log.info("Deleted report file from disk: {}", report.getReportFilePath());
                    } else {
                        log.warn("Failed to delete report file from disk: {}", report.getReportFilePath());
                    }
                }
                reportRepository.delete(report);
                log.info("Removed report metadata from database: ID {}", report.getId());
            } catch (Exception e) {
                log.error("Error cleaning up report ID: {}", report.getId(), e);
            }
        }
    }
}
