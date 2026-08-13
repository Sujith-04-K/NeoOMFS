import { By, until } from 'selenium-webdriver';

/**
 * PatientListPage — Page Object for the patient list/search screen.
 */
export class PatientListPage {
  constructor(driver) {
    this.driver = driver;
    this.searchInput     = By.css('[data-testid="patient-search-input"]');
    this.searchResults   = By.css('[data-testid="patient-search-results"]');
    this.patientRows     = By.css('[data-testid="patient-row"]');
    this.sortNameAZ      = By.css('[data-testid="sort-name-az"]');
    this.sortDateNewest  = By.css('[data-testid="sort-date-newest"]');
    this.filterStatus    = By.css('[data-testid="filter-status"]');
    this.filterGender    = By.css('[data-testid="filter-gender"]');
    this.paginationNext  = By.css('[data-testid="pagination-next"]');
    this.paginationPrev  = By.css('[data-testid="pagination-prev"]');
    this.pageSize        = By.css('[data-testid="page-size-select"]');
    this.emptyState      = By.css('[data-testid="patients-empty-state"]');
    this.newPatientBtn   = By.css('[data-testid="new-patient-btn"]');
  }

  async searchFor(query) {
    try {
      const el = await this.driver.findElement(this.searchInput);
      await el.clear();
      await el.sendKeys(query);
    } catch (_) {}
  }

  async getPatientCount() {
    try {
      const rows = await this.driver.findElements(this.patientRows);
      return rows.length;
    } catch (_) { return 0; }
  }

  async clickNextPage() {
    try {
      await this.driver.findElement(this.paginationNext).click();
    } catch (_) {}
  }

  async clickFirstPatient() {
    try {
      const rows = await this.driver.findElements(this.patientRows);
      if (rows.length > 0) await rows[0].click();
    } catch (_) {}
  }

  async isEmptyStateVisible() {
    try {
      return await this.driver.findElement(this.emptyState).isDisplayed();
    } catch (_) { return false; }
  }
}
