package com.simats.neoomfs.pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MobileDashboardPage {
    private final AppiumDriver driver;
    private final WebDriverWait wait;

    private final By greetingText = By.id("com.simats.neoomfs:id/tvGreeting");
    private final By newAssessmentBtn = By.id("com.simats.neoomfs:id/btnNewAssessment");
    private final By searchField = By.id("com.simats.neoomfs:id/etSearch");
    private final By registryLink = By.id("com.simats.neoomfs:id/btnRegistryLink");

    public MobileDashboardPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getGreetingText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(greetingText)).getText();
    }

    public void clickNewAssessment() {
        wait.until(ExpectedConditions.elementToBeClickable(newAssessmentBtn)).click();
    }

    public void searchPatient(String query) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchField)).sendKeys(query);
    }
}
