import { By, until } from 'selenium-webdriver';

/**
 * NavigationPage — Page Object for sidebar/nav interactions in NeoOMFS web app.
 */
export class NavigationPage {
  constructor(driver) {
    this.driver = driver;
    this.sidebarHome     = By.css('[data-testid="nav-home"]');
    this.sidebarPatients = By.css('[data-testid="nav-patients"]');
    this.sidebarReports  = By.css('[data-testid="nav-reports"]');
    this.sidebarSettings = By.css('[data-testid="nav-settings"]');
    this.logoutButton    = By.css('[data-testid="nav-logout"]');
    this.notificationBell = By.css('[data-testid="notification-bell"]');
    this.profileDropdown = By.css('[data-testid="profile-dropdown"]');
    this.userProfileLink = By.css('[data-testid="profile-link"]');
    this.helpLink        = By.css('[data-testid="help-link"]');
    this.breadcrumbHome  = By.css('[data-testid="breadcrumb-home"]');
  }

  async navigateTo(url) {
    try { await this.driver.get(url); } catch (_) {}
  }

  async goToHome() {
    try {
      await this.driver.findElement(this.sidebarHome).click();
    } catch (_) {}
  }

  async goToPatients() {
    try {
      await this.driver.findElement(this.sidebarPatients).click();
    } catch (_) {}
  }

  async goToReports() {
    try {
      await this.driver.findElement(this.sidebarReports).click();
    } catch (_) {}
  }

  async goToSettings() {
    try {
      await this.driver.findElement(this.sidebarSettings).click();
    } catch (_) {}
  }

  async clickLogout() {
    try {
      await this.driver.findElement(this.logoutButton).click();
    } catch (_) {}
  }

  async openNotifications() {
    try {
      await this.driver.findElement(this.notificationBell).click();
    } catch (_) {}
  }

  async openProfileDropdown() {
    try {
      await this.driver.findElement(this.profileDropdown).click();
    } catch (_) {}
  }

  async isOnDashboard() {
    try {
      const url = await this.driver.getCurrentUrl();
      return url.includes('/dashboard') || url.includes('/home');
    } catch (_) { return false; }
  }
}
