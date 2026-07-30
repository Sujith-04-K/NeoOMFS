-- NeoOMFS Database Migration - Audit Logs Update
-- DBMS Target: MySQL

ALTER TABLE audit_logs ADD COLUMN patient_id BIGINT DEFAULT NULL AFTER username;
ALTER TABLE audit_logs ADD COLUMN module VARCHAR(50) DEFAULT NULL AFTER patient_id;

ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE;
