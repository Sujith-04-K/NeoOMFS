import { By, until } from 'selenium-webdriver';

export class LoginPage {
  constructor(driver) {
    this.driver = driver;
    this.emailInput = By.css('[data-testid="login-email"]');
    this.passwordInput = By.css('[data-testid="login-password"]');
    this.submitButton = By.id('login-submit');
    this.forgotPasswordLink = By.css('[data-testid="forgot-password-link"]');
    
    // Forgot Password Modal
    this.forgotEmailInput = By.css('[data-testid="forgot-email-input"]');
    this.sendOtpButton = By.css('[data-testid="forgot-send-otp-btn"]');
    this.otpInput = By.css('[data-testid="forgot-otp-input"]');
    this.newPasswordInput = By.css('[data-testid="forgot-new-password-input"]');
    this.resetSubmitButton = By.css('[data-testid="forgot-reset-btn"]');
  }

  async navigateTo(url) {
    await this.driver.get(url);
    await this.driver.wait(until.elementLocated(this.emailInput), 5000);
  }

  async login(email, password) {
    await this.driver.findElement(this.emailInput).sendKeys(email);
    await this.driver.findElement(this.passwordInput).sendKeys(password);
    await this.driver.findElement(this.submitButton).click();
  }

  async triggerForgotPassword(email, otp, newPassword) {
    await this.driver.findElement(this.forgotPasswordLink).click();
    await this.driver.wait(until.elementLocated(this.forgotEmailInput), 3000);
    await this.driver.findElement(this.forgotEmailInput).sendKeys(email);
    await this.driver.findElement(this.sendOtpButton).click();
    
    // OTP entry step
    await this.driver.wait(until.elementLocated(this.otpInput), 3000);
    await this.driver.findElement(this.otpInput).sendKeys(otp);
    await this.driver.findElement(this.newPasswordInput).sendKeys(newPassword);
    await this.driver.findElement(this.resetSubmitButton).click();
  }
}
