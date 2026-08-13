import { By, until } from 'selenium-webdriver';

export class DashboardPage {
  constructor(driver) {
    this.driver = driver;
    this.searchInput = By.css('[data-testid="dashboard-search-input"]');
    this.clearSearchButton = By.css('[data-testid="dashboard-clear-search"]');
    this.refreshButton = By.css('[data-testid="dashboard-refresh-btn"]');
    this.newAssessmentButton = By.css('[data-testid="quick-action-new-assessment"]');
    this.recentPatientsTable = By.css('.clinical-table-card table');
  }

  async search(query) {
    const input = await this.driver.findElement(this.searchInput);
    await input.clear();
    await input.sendKeys(query);
  }

  async clearSearch() {
    await this.driver.findElement(this.clearSearchButton).click();
  }

  async syncDatabase() {
    await this.driver.findElement(this.refreshButton).click();
  }

  async clickNewAssessment() {
    await this.driver.findElement(this.newAssessmentButton).click();
  }
}
