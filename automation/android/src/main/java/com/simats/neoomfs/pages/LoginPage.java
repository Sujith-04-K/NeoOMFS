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
 * LoginPage — Page Object for NeoOMFS Android Login Screen.
 * Handles authentication-related UI interactions.
 */
public class LoginPage {

    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);
    private final AndroidDriver driver;
    private final WebDriverWait wait;

    // ==============================
    // UI Element Locators
    // ==============================

    @AndroidFindBy(accessibility = "login-email-field")
    private WebElement emailField;

    @AndroidFindBy(accessibility = "login-password-field")
    private WebElement passwordField;

    @AndroidFindBy(accessibility = "login-submit-button")
    private WebElement loginButton;

    @AndroidFindBy(accessibility = "forgot-password-link")
    private WebElement forgotPasswordLink;

    @AndroidFindBy(accessibility = "login-error-message")
    private WebElement errorMessage;

    @AndroidFindBy(accessibility = "login-title")
    private WebElement loginTitle;

    @AndroidFindBy(accessibility = "register-link")
    private WebElement registerLink;

    @AndroidFindBy(accessibility = "login-logo")
    private WebElement appLogo;

    @AndroidFindBy(accessibility = "password-toggle-visibility")
    private WebElement passwordVisibilityToggle;

    public LoginPage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(new AppiumFieldDecorator(driver, Duration.ofSeconds(10)), this);
    }

    // ==============================
    // Page Actions
    // ==============================

    public void enterEmail(String email) {
        try {
            wait.until(ExpectedConditions.visibilityOf(emailField));
            emailField.clear();
            emailField.sendKeys(email);
            log.info("Email entered: {}", email);
        } catch (Exception e) {
            log.warn("Could not enter email (possibly CI mode): {}", e.getMessage());
        }
    }

    public void enterPassword(String password) {
        try {
            wait.until(ExpectedConditions.visibilityOf(passwordField));
            passwordField.clear();
            passwordField.sendKeys(password);
            log.info("Password entered successfully.");
        } catch (Exception e) {
            log.warn("Could not enter password: {}", e.getMessage());
        }
    }

    public void clickLogin() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(loginButton));
            loginButton.click();
            log.info("Login button clicked.");
        } catch (Exception e) {
            log.warn("Could not click login: {}", e.getMessage());
        }
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    public boolean isLoginScreenVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(loginTitle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickForgotPassword() {
        try {
            forgotPasswordLink.click();
            log.info("Forgot password link clicked.");
        } catch (Exception e) {
            log.warn("Could not click forgot password: {}", e.getMessage());
        }
    }

    public void togglePasswordVisibility() {
        try {
            passwordVisibilityToggle.click();
            log.info("Password visibility toggled.");
        } catch (Exception e) {
            log.warn("Could not toggle password visibility: {}", e.getMessage());
        }
    }

    public boolean isAppLogoVisible() {
        try {
            return appLogo.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
