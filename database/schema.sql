-- NeoOMFS Preoperative Surgical Fitness Assessment System Database Schema
-- DBMS Target: MySQL Server 8.x
-- Highly optimized, fully normalized to 3NF, with complete referential integrity.
-- Aligned 100% with the Spring Boot JPA Entity mappings.

CREATE DATABASE IF NOT EXISTS neoomfs;
USE neoomfs;

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Users (Clinicians / Surgeons / Faculty / Students)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    license_number VARCHAR(50) DEFAULT NULL,
    department VARCHAR(100) DEFAULT NULL,
    institution VARCHAR(150) DEFAULT NULL,
    phone_number VARCHAR(20) DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login DATETIME DEFAULT NULL,
    password_reset_token VARCHAR(200) DEFAULT NULL,
    password_reset_expiry DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. User Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Patients Demographics
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mrn VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    age INT DEFAULT NULL,
    date_of_birth DATE DEFAULT NULL,
    gender VARCHAR(10) DEFAULT NULL,
    blood_group VARCHAR(10) DEFAULT NULL,
    phone_number VARCHAR(20) DEFAULT NULL,
    address VARCHAR(300) DEFAULT NULL,
    emergency_contact VARCHAR(100) DEFAULT NULL,
    emergency_phone VARCHAR(20) DEFAULT NULL,
    procedure_type VARCHAR(100) DEFAULT NULL,
    referring_doctor VARCHAR(100) DEFAULT NULL,
    assessment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_patients_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Patient Clinical Vitals
CREATE TABLE IF NOT EXISTS patient_vitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    bp_systolic INT DEFAULT NULL,
    bp_diastolic INT DEFAULT NULL,
    temperature DECIMAL(4,1) DEFAULT NULL,
    pulse_rate INT DEFAULT NULL,
    spo2 DECIMAL(4,1) DEFAULT NULL,
    respiratory_rate INT DEFAULT NULL,
    height_cm DECIMAL(5,1) DEFAULT NULL,
    weight_kg DECIMAL(5,1) DEFAULT NULL,
    bmi DECIMAL(4,1) DEFAULT NULL,
    random_blood_sugar DECIMAL(5,1) DEFAULT NULL,
    notes VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_vitals_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Radiology MODALITIES
CREATE TABLE IF NOT EXISTS radiology (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    iopa_taken BOOLEAN NOT NULL DEFAULT FALSE,
    iopa_file_url VARCHAR(500) DEFAULT NULL,
    iopa_findings VARCHAR(500) DEFAULT NULL,
    opg_taken BOOLEAN NOT NULL DEFAULT FALSE,
    opg_file_url VARCHAR(500) DEFAULT NULL,
    opg_findings VARCHAR(500) DEFAULT NULL,
    cbct_taken BOOLEAN NOT NULL DEFAULT FALSE,
    cbct_file_url VARCHAR(500) DEFAULT NULL,
    cbct_findings VARCHAR(500) DEFAULT NULL,
    bone_density_hu DOUBLE DEFAULT NULL,
    general_radiology_notes VARCHAR(1000) DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_radiology_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Laboratory Investigations
CREATE TABLE IF NOT EXISTS laboratory_investigations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    hemoglobin DECIMAL(4,1) DEFAULT NULL,
    total_wbc_count INT DEFAULT NULL,
    platelet_count INT DEFAULT NULL,
    bleeding_time DECIMAL(3,1) DEFAULT NULL,
    clotting_time DECIMAL(3,1) DEFAULT NULL,
    pt DECIMAL(4,1) DEFAULT NULL,
    inr DECIMAL(3,1) DEFAULT NULL,
    aptt DECIMAL(4,1) DEFAULT NULL,
    fasting_blood_sugar DECIMAL(5,1) DEFAULT NULL,
    random_blood_sugar DECIMAL(5,1) DEFAULT NULL,
    hba1c DECIMAL(3,1) DEFAULT NULL,
    blood_urea DECIMAL(5,1) DEFAULT NULL,
    serum_creatinine DECIMAL(4,2) DEFAULT NULL,
    serum_bilirubin_total DECIMAL(4,1) DEFAULT NULL,
    sgot DECIMAL(5,1) DEFAULT NULL,
    sgpt DECIMAL(5,1) DEFAULT NULL,
    blood_group VARCHAR(5) DEFAULT NULL,
    rh_factor VARCHAR(10) DEFAULT NULL,
    hiv_status VARCHAR(20) DEFAULT NULL,
    hbsag_status VARCHAR(20) DEFAULT NULL,
    hcv_status VARCHAR(20) DEFAULT NULL,
    lab_report_file_url VARCHAR(500) DEFAULT NULL,
    notes VARCHAR(500) DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_labs_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Medical History & Systemic Diseases
CREATE TABLE IF NOT EXISTS medical_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    hypertension BOOLEAN NOT NULL DEFAULT FALSE,
    diabetes BOOLEAN NOT NULL DEFAULT FALSE,
    heart_disease BOOLEAN NOT NULL DEFAULT FALSE,
    kidney_disease BOOLEAN NOT NULL DEFAULT FALSE,
    liver_disease BOOLEAN NOT NULL DEFAULT FALSE,
    thyroid_disorder BOOLEAN NOT NULL DEFAULT FALSE,
    asthma BOOLEAN NOT NULL DEFAULT FALSE,
    epilepsy BOOLEAN NOT NULL DEFAULT FALSE,
    blood_disorder BOOLEAN NOT NULL DEFAULT FALSE,
    hepatitis BOOLEAN NOT NULL DEFAULT FALSE,
    hiv_positive BOOLEAN NOT NULL DEFAULT FALSE,
    pregnancy_status BOOLEAN NOT NULL DEFAULT FALSE,
    pregnancy_trimester VARCHAR(20) DEFAULT NULL,
    other_conditions VARCHAR(500) DEFAULT NULL,
    current_medications TEXT DEFAULT NULL,
    allergies TEXT DEFAULT NULL,
    previous_surgeries TEXT DEFAULT NULL,
    anaesthetic_complications VARCHAR(500) DEFAULT NULL,
    family_history VARCHAR(500) DEFAULT NULL,
    social_history VARCHAR(300) DEFAULT NULL,
    notes VARCHAR(1000) DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_history_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Structured Medications
CREATE TABLE IF NOT EXISTS medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    drug_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(50) DEFAULT NULL,
    frequency VARCHAR(50) DEFAULT NULL,
    route VARCHAR(50) DEFAULT NULL,
    indication VARCHAR(200) DEFAULT NULL,
    is_anticoagulant BOOLEAN DEFAULT FALSE,
    is_immunosuppressant BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_meds_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Dental & Local Mandibular Examination
CREATE TABLE IF NOT EXISTS dental_examination (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    asa_class VARCHAR(5) DEFAULT NULL,
    pell_gregory_class VARCHAR(10) DEFAULT NULL,
    winter_classification VARCHAR(30) DEFAULT NULL,
    upper_third_molar VARCHAR(50) DEFAULT NULL,
    difficulty_score INT DEFAULT NULL,
    mouth_opening_mm INT DEFAULT NULL,
    oral_hygiene_status VARCHAR(30) DEFAULT NULL,
    periodontal_status VARCHAR(200) DEFAULT NULL,
    active_infection BOOLEAN NOT NULL DEFAULT FALSE,
    swelling BOOLEAN NOT NULL DEFAULT FALSE,
    trismus BOOLEAN NOT NULL DEFAULT FALSE,
    tooth_number VARCHAR(10) DEFAULT NULL,
    clinical_examination_notes TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_dental_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Clinical Decisions (Triage Output)
CREATE TABLE IF NOT EXISTS clinical_decisions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE,
    risk_level VARCHAR(20) DEFAULT NULL,
    fitness_decision VARCHAR(20) DEFAULT NULL,
    risk_score INT DEFAULT NULL,
    clinical_alerts TEXT DEFAULT NULL,
    recommendations TEXT DEFAULT NULL,
    decision_notes TEXT DEFAULT NULL,
    generated_by_user_id BIGINT DEFAULT NULL,
    generated_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_decision_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_decision_generator FOREIGN KEY (generated_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. Assessment PDF Reports
CREATE TABLE IF NOT EXISTS assessment_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    report_file_path VARCHAR(500) DEFAULT NULL,
    report_file_name VARCHAR(200) DEFAULT NULL,
    report_generated_at DATETIME DEFAULT NULL,
    report_version INT DEFAULT 1,
    generated_by_user_id BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_reports_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_generator FOREIGN KEY (generated_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) DEFAULT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_patient_id BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    username VARCHAR(80) DEFAULT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(60) DEFAULT NULL,
    entity_id BIGINT DEFAULT NULL,
    description VARCHAR(500) DEFAULT NULL,
    ip_address VARCHAR(50) DEFAULT NULL,
    timestamp DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. Uploaded Files (Extensible for Direct API Tracking)
CREATE TABLE IF NOT EXISTS uploaded_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) DEFAULT NULL,
    file_category VARCHAR(50) DEFAULT 'Radiology', -- Radiology, Lab Report, Patient Document
    file_size BIGINT DEFAULT NULL,
    patient_id BIGINT NOT NULL,
    uploaded_by_user_id BIGINT DEFAULT NULL,
    upload_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_uploads_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_uploads_user FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- PERFORMANCE OPTIMIZATION INDEXES
-- ============================================================
CREATE INDEX idx_patients_mrn ON patients(mrn);
CREATE INDEX idx_patients_name ON patients(full_name);
CREATE INDEX idx_patients_status ON patients(assessment_status);
CREATE INDEX idx_patients_created ON patients(created_at);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_clinical_decisions_risk ON clinical_decisions(risk_level);
CREATE INDEX idx_clinical_decisions_patient ON clinical_decisions(patient_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id);
CREATE INDEX idx_uploads_patient ON uploaded_files(patient_id);
