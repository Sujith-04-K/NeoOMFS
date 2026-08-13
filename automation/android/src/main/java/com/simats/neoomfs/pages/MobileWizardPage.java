package com.simats.neoomfs.pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MobileWizardPage {
    private final AppiumDriver driver;
    private final WebDriverWait wait;

    // Step 1: Profile
    private final By nameField = By.id("com.simats.neoomfs:id/etName");
    private final By ageField = By.id("com.simats.neoomfs:id/etAge");
    private final By genderSpinner = By.id("com.simats.neoomfs:id/spinnerGender");
    private final By nextStepBtn = By.id("com.simats.neoomfs:id/btnNextStep");

    // Step 2: Vitals
    private final By sysBpField = By.id("com.simats.neoomfs:id/etBpSystolic");
    private final By diaBpField = By.id("com.simats.neoomfs:id/etBpDiastolic");
    private final By spo2Field = By.id("com.simats.neoomfs:id/etSpo2");

    public MobileWizardPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void fillStep1Demographics(String name, String age) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameField)).sendKeys(name);
        driver.findElement(ageField).sendKeys(age);
        driver.findElement(nextStepBtn).click();
    }

    public void fillStep2Vitals(String systolic, String diastolic, String spo2) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(sysBpField));
        driver.findElement(sysBpField).sendKeys(systolic);
        driver.findElement(diaBpField).sendKeys(diastolic);
        driver.findElement(spo2Field).sendKeys(spo2);
        driver.findElement(nextStepBtn).click();
    }
}
