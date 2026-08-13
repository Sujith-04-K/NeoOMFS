package com.simats.neoomfs.pages;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MobileLoginPage {
    private final AppiumDriver driver;
    private final WebDriverWait wait;

    // Mobile View Locators
    private final By emailField = By.id("com.simats.neoomfs:id/etEmail");
    private final By passwordField = By.id("com.simats.neoomfs:id/etPassword");
    private final By signInButton = By.id("com.simats.neoomfs:id/btnSignIn");
    private final By forgotPasswordLink = By.id("com.simats.neoomfs:id/tvForgotPassword");

    public MobileLoginPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField));
        driver.findElement(emailField).sendKeys(email);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(signInButton).click();
    }

    public void clickForgotPassword() {
        wait.until(ExpectedConditions.elementToBeClickable(forgotPasswordLink)).click();
    }
}
