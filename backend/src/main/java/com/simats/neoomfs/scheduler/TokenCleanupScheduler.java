package com.simats.neoomfs.scheduler;

import com.simats.neoomfs.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    // Run daily at 1:00 AM: purge expired refresh tokens from DB
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Starting purge of expired refresh tokens.");
        try {
            int deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
            log.info("Completed purge of expired refresh tokens. Deleted: {} tokens", deletedCount);
        } catch (Exception e) {
            log.error("Failed to purge expired refresh tokens", e);
        }
    }
}
