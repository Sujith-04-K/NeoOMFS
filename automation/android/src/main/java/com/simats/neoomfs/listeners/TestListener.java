package com.simats.neoomfs.listeners;

import com.simats.neoomfs.config.AppiumConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TestListener — TestNG listener for pass/fail tracking, screenshot capture on failure,
 * and console summary output for CI/CD pipelines.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);
    private static int passed = 0;
    private static int failed = 0;
    private static int skipped = 0;

    @Override
    public void onStart(ITestContext context) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║   NeoOMFS Appium E2E Test Suite Starting         ║");
        log.info("║   Suite: {}  ", context.getName());
        log.info("╚══════════════════════════════════════════════════╝");
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ RUNNING: {} — {}", result.getTestClass().getName(), result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        passed++;
        log.info("  ✓ PASSED:  {} ({} ms)", result.getName(),
                result.getEndMillis() - result.getStartMillis());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failed++;
        log.error("  ✗ FAILED:  {} — Reason: {}", result.getName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown");
        captureScreenshot(result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skipped++;
        log.warn("  ⊘ SKIPPED: {}", result.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        int total = passed + failed + skipped;
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║   Suite Complete: {}  ", context.getName());
        log.info("║   Total:   {}  ", total);
        log.info("║   Passed:  {}  ", passed);
        log.info("║   Failed:  {}  ", failed);
        log.info("║   Skipped: {}  ", skipped);
        log.info("║   Rate:    {:.1f}%  ", passRate);
        log.info("╚══════════════════════════════════════════════════╝");
    }

    private void captureScreenshot(String testName) {
        try {
            var driver = AppiumConfig.getDriver();
            if (driver != null) {
                var screenshotDir = new File("automation/android/screenshots");
                screenshotDir.mkdirs();
                var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                var filename = String.format("automation/android/screenshots/FAIL_%s_%s.png", testName, timestamp);
                var srcFile = ((org.openqa.selenium.TakesScreenshot) driver)
                        .getScreenshotAs(org.openqa.selenium.OutputType.FILE);
                Files.copy(srcFile.toPath(), Paths.get(filename));
                log.info("  📸 Screenshot saved: {}", filename);
            }
        } catch (IOException e) {
            log.warn("Could not capture screenshot: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Screenshot skipped (CI mode): {}", e.getMessage());
        }
    }
}
