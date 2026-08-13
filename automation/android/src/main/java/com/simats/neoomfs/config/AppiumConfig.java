package com.simats.neoomfs.config;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;

/**
 * AppiumConfig — Driver factory for NeoOMFS Android automation.
 * Reads capabilities from config.properties and initializes AndroidDriver.
 */
public class AppiumConfig {

    private static final Logger log = LoggerFactory.getLogger(AppiumConfig.class);
    private static final Properties props = new Properties();
    private static ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    static {
        try {
            String configPath = System.getProperty("config.file",
                    "src/main/resources/config.properties");
            props.load(new FileInputStream(configPath));
            log.info("Configuration loaded from: {}", configPath);
        } catch (IOException e) {
            log.warn("Config file not found, using defaults: {}", e.getMessage());
        }
    }

    public static AndroidDriver initDriver() {
        try {
            UiAutomator2Options options = new UiAutomator2Options();

            // Device capabilities
            options.setDeviceName(getProperty("deviceName", "emulator-5554"));
            options.setPlatformVersion(getProperty("platformVersion", "13.0"));
            options.setApp(resolveApkPath());

            // App capabilities
            options.setAppPackage(getProperty("appPackage", "com.simats.neoomfs"));
            options.setAppActivity(getProperty("appActivity", "com.simats.neoomfs.MainActivity"));

            // Performance options
            options.setNoReset(false);
            options.setFullReset(false);
            options.setAutoGrantPermissions(true);
            options.setNewCommandTimeout(Duration.ofSeconds(60));
            options.setCapability("settings[waitForIdleTimeout]", 10);
            options.setCapability("settings[waitForSelectorTimeout]", 5000);

            // Logging
            options.setCapability("appium:eventTimings", true);

            String appiumUrl = getProperty("appiumUrl", "http://127.0.0.1:4723");
            AndroidDriver driver = new AndroidDriver(new URL(appiumUrl), options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            driverThreadLocal.set(driver);
            log.info("AndroidDriver initialized successfully on {}", appiumUrl);
            return driver;

        } catch (Exception e) {
            log.error("Failed to initialize AndroidDriver: {}", e.getMessage());
            // Return null — tests will handle gracefully via try/catch
            return null;
        }
    }

    public static AndroidDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void quitDriver() {
        AndroidDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("AndroidDriver quit successfully.");
            } catch (Exception e) {
                log.warn("Error quitting driver: {}", e.getMessage());
            }
            driverThreadLocal.remove();
        }
    }

    private static String resolveApkPath() {
        String apkPath = getProperty("apkPath",
                "../../frontend/app/build/outputs/apk/debug/app-debug.apk");
        log.info("APK path resolved: {}", apkPath);
        return apkPath;
    }

    public static String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
