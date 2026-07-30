package com.simats.neoomfs.scheduler;

import com.simats.neoomfs.repository.NotificationRepository;
import com.simats.neoomfs.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NightlyJobSchedulerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private NightlyJobScheduler nightlyJobScheduler;

    @Test
    public void testCleanupNotifications() {
        when(notificationRepository.deleteByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(10);
        
        int result = nightlyJobScheduler.cleanupOldNotifications();
        
        assertEquals(10, result);
        verify(notificationRepository, times(1)).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }

    @Test
    public void testSchedulerExecutionLogsCorrectly() {
        when(notificationRepository.deleteByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(5);

        nightlyJobScheduler.runNightlyJobs();

        // Verify start audit log
        verify(auditLogService, times(1)).log(
                eq(null), eq("SYSTEM"), eq(null), eq("SCHEDULER"), eq("RUN"),
                eq("Scheduler execution started."), eq("Scheduler"), eq(null)
        );

        // Verify end audit log contains deleted statistics
        verify(auditLogService, times(1)).log(
                eq(null), eq("SYSTEM"), eq(null), eq("SCHEDULER"), eq("RUN"),
                contains("Scheduler execution completed. Notifications deleted: 5."), eq("Scheduler"), eq(null)
        );

        verify(notificationRepository, times(1)).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }
}
