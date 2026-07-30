package com.simats.neoomfs.scheduler;

import com.simats.neoomfs.repository.NotificationRepository;
import com.simats.neoomfs.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NightlyJobScheduler {

    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runNightlyJobs() {
        log.info("Starting nightly scheduler job execution.");
        
        // Log scheduler started
        auditLogService.log(null, "SYSTEM", null, "SCHEDULER", "RUN", "Scheduler execution started.", "Scheduler", null);

        int deletedNotifications = 0;

        // 1. Notification Cleanup
        try {
            deletedNotifications = cleanupOldNotifications();
        } catch (Exception e) {
            log.error("Failed to execute notification cleanup", e);
        }

        // 2. Assessment Reports
        // TODO: Implement report archival and deletion logic in the future.
        log.info("Report archiving skipped (TODO for future phase).");

        // Log scheduler completed
        String summary = String.format("Scheduler execution completed. Notifications deleted: %d.",
                deletedNotifications);
        log.info(summary);
        auditLogService.log(null, "SYSTEM", null, "SCHEDULER", "RUN", summary, "Scheduler", null);
    }

    public int cleanupOldNotifications() {
        log.info("Cleaning up notifications older than 30 days.");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        int deletedCount = notificationRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Successfully deleted {} notifications older than 30 days.", deletedCount);
        return deletedCount;
    }
}
