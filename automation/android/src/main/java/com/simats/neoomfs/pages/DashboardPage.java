package com.simats.neoomfs.pages;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * DashboardPage — Page Object for NeoOMFS Android Main Dashboard.
 */
public class DashboardPage {

    private static final Logger log = LoggerFactory.getLogger(DashboardPage.class);
    private final AndroidDriver driver;
    private final WebDriverWait wait;

    @AndroidFindBy(accessibility = "dashboard-title")
    private WebElement dashboardTitle;

    @AndroidFindBy(accessibility = "total-patients-stat")
    private WebElement totalPatientsStat;

    @AndroidFindBy(accessibility = "today-assessments-stat")
    private WebElement todayAssessmentsStat;

    @AndroidFindBy(accessibility = "pending-reports-stat")
    private WebElement pendingReportsStat;

    @AndroidFindBy(accessibility = "new-assessment-button")
    private WebElement newAssessmentButton;

    @AndroidFindBy(accessibility = "search-patients-input")
    private WebElement searchInput;

    @AndroidFindBy(accessibility = "refresh-dashboard-button")
    private WebElement refreshButton;

    @AndroidFindBy(accessibility = "patient-list-table")
    private WebElement patientListTable;

    @AndroidFindBy(accessibility = "logout-button")
    private WebElement logoutButton;

    @AndroidFindBy(accessibility = "user-profile-icon")
    private WebElement userProfileIcon;

    @AndroidFindBy(accessibility = "nav-home")
    private WebElement navHome;

    @AndroidFindBy(accessibility = "nav-patients")
    private WebElement navPatients;

    @AndroidFindBy(accessibility = "nav-reports")
    private WebElement navReports;

    @AndroidFindBy(accessibility = "nav-settings")
    private WebElement navSettings;

    public DashboardPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    public boolean isDashboardVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(dashboardTitle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickNewAssessment() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(newAssessmentButton));
            newAssessmentButton.click();
            log.info("New Assessment button clicked.");
        } catch (Exception e) {
            log.warn("New Assessment button not found: {}", e.getMessage());
        }
    }

    public void searchPatients(String query) {
        try {
            searchInput.clear();
            searchInput.sendKeys(query);
            log.info("Searched for: {}", query);
        } catch (Exception e) {
            log.warn("Search not available: {}", e.getMessage());
        }
    }

    public void refresh() {
        try {
            refreshButton.click();
            log.info("Dashboard refreshed.");
        } catch (Exception e) {
            log.warn("Refresh failed: {}", e.getMessage());
        }
    }

    public String getTotalPatientsCount() {
        try {
            return totalPatientsStat.getText();
        } catch (Exception e) {
            return "N/A";
        }
    }

    public String getTodayAssessmentsCount() {
        try {
            return todayAssessmentsStat.getText();
        } catch (Exception e) {
            return "N/A";
        }
    }

    public void clickLogout() {
        try {
            logoutButton.click();
            log.info("Logout clicked.");
        } catch (Exception e) {
            log.warn("Logout failed: {}", e.getMessage());
        }
    }

    public void navigateToPatients() {
        try {
            navPatients.click();
            log.info("Navigated to Patients.");
        } catch (Exception e) {
            log.warn("Navigation failed: {}", e.getMessage());
        }
    }

    public void navigateToReports() {
        try {
            navReports.click();
            log.info("Navigated to Reports.");
        } catch (Exception e) {
            log.warn("Navigation failed: {}", e.getMessage());
        }
    }

    public boolean isPatientListVisible() {
        try {
            return patientListTable.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
