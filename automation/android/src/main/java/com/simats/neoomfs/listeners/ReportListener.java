package com.simats.neoomfs.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;

/**
 * ReportListener — Generates ExtentReports HTML report for CI/CD.
 * Output: automation/android/reports/NeoOMFS-E2E-Report.html
 */
public class ReportListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(ReportListener.class);
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        new File("automation/android/reports").mkdirs();
        new File("automation/android/Test Results/HTML").mkdirs();

        String reportPath = "automation/android/reports/NeoOMFS-E2E-Report.html";
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("NeoOMFS Android E2E Report");
        spark.config().setReportName("NeoOMFS — Appium Automation Report");
        spark.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", "NeoOMFS — Oral & Maxillofacial Surgery");
        extent.setSystemInfo("Platform", "Android 13 (API 33)");
        extent.setSystemInfo("Framework", "Appium 9.x + TestNG 7.x + Java 17");
        extent.setSystemInfo("Environment", "CI/CD — GitHub Actions");
        extent.setSystemInfo("Author", "SIMATS QA Team");

        log.info("ExtentReports initialized: {}", reportPath);
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (extent != null) {
            ExtentTest extentTest = extent.createTest(result.getName())
                    .assignCategory(result.getTestClass().getSimpleName())
                    .assignAuthor("NeoOMFS QA");
            test.set(extentTest);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (test.get() != null) {
            test.get().pass("✓ Test Passed in "
                    + (result.getEndMillis() - result.getStartMillis()) + " ms");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (test.get() != null) {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage() : "Unknown failure";
            test.get().fail("✗ Test Failed: " + reason);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (test.get() != null) {
            test.get().skip("⊘ Test Skipped");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
            log.info("ExtentReports HTML report generated at: automation/android/reports/NeoOMFS-E2E-Report.html");
        }
    }
}
