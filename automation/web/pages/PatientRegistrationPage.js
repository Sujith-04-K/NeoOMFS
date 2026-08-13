import { By, until } from 'selenium-webdriver';

export class PatientRegistrationPage {
  constructor(driver) {
    this.driver = driver;
    // Step 1 Elements
    this.fullNameInput = By.name('fullName');
    this.ageInput = By.name('ageInput');
    this.dobInput = By.name('dateOfBirth');
    this.genderSelect = By.name('gender');
    this.bloodGroupSelect = By.name('bloodGroup');
    this.phoneInput = By.name('phoneNumber');
    this.nextButton = By.css('button[type="submit"]');

    // Step 2 (Vitals) Elements
    this.bpSystolic = By.name('bpSystolic');
    this.bpDiastolic = By.name('bpDiastolic');
    this.pulseRate = By.name('pulseRate');
    this.spo2 = By.name('spo2');

    // Step 3 (Radiology)
    this.opgCheckbox = By.name('opgTaken');
    this.opgFindings = By.name('opgFindings');

    // Step 4 (Labs)
    this.hemoglobin = By.name('hemoglobin');
    this.platelets = By.name('plateletCount');
    this.inr = By.name('inr');

    // Step 5 (Medical History)
    this.hypertensionCheckbox = By.name('hypertension');
    this.diabetesCheckbox = By.name('diabetes');

    // Step 6 (Dental Impaction)
    this.toothSelect = By.name('toothNumber');
    this.pellGregorySelect = By.name('pellGregoryClass');
    this.winterSelect = By.name('winterClassification');

    // Step 7 (Clinical Decision)
    this.evaluateBtn = By.css('[data-testid="evaluate-decision-btn"]');

    // Step 8 (Report Generation)
    this.compileReportBtn = By.css('[data-testid="generate-report-btn"]');
    this.downloadPdfBtn = By.css('[data-testid="download-pdf-btn"]');
  }

  async fillStep1(name, age, phone) {
    await this.driver.findElement(this.fullNameInput).sendKeys(name);
    await this.driver.findElement(this.ageInput).sendKeys(age);
    await this.driver.findElement(this.phoneInput).sendKeys(phone);
    await this.driver.findElement(this.nextButton).click();
  }

  async fillStep2(sys, dia, pulse, ox) {
    await this.driver.wait(until.elementLocated(this.bpSystolic), 3000);
    await this.driver.findElement(this.bpSystolic).sendKeys(sys);
    await this.driver.findElement(this.bpDiastolic).sendKeys(dia);
    await this.driver.findElement(this.pulseRate).sendKeys(pulse);
    await this.driver.findElement(this.spo2).sendKeys(ox);
    await this.driver.findElement(this.nextButton).click();
  }

  async fillStep3(findings) {
    await this.driver.wait(until.elementLocated(this.opgCheckbox), 3000);
    await this.driver.findElement(this.opgCheckbox).click();
    await this.driver.findElement(this.opgFindings).sendKeys(findings);
    await this.driver.findElement(this.nextButton).click();
  }

  async fillStep4(hb, plt, ratio) {
    await this.driver.wait(until.elementLocated(this.hemoglobin), 3000);
    await this.driver.findElement(this.hemoglobin).sendKeys(hb);
    await this.driver.findElement(this.platelets).sendKeys(plt);
    await this.driver.findElement(this.inr).sendKeys(ratio);
    await this.driver.findElement(this.nextButton).click();
  }

  async fillStep5() {
    await this.driver.wait(until.elementLocated(this.hypertensionCheckbox), 3000);
    await this.driver.findElement(this.hypertensionCheckbox).click();
    await this.driver.findElement(this.nextButton).click();
  }

  async fillStep6() {
    await this.driver.wait(until.elementLocated(this.toothSelect), 3000);
    await this.driver.findElement(this.nextButton).click();
  }

  async evaluateClinicalTriage() {
    await this.driver.wait(until.elementLocated(this.evaluateBtn), 3000);
    await this.driver.findElement(this.evaluateBtn).click();
    await this.driver.findElement(this.nextButton).click();
  }

  async compileReport() {
    await this.driver.wait(until.elementLocated(this.compileReportBtn), 3000);
    await this.driver.findElement(this.compileReportBtn).click();
    await this.driver.wait(until.elementLocated(this.downloadPdfBtn), 3000);
  }
}
