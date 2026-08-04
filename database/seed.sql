-- Seed Data for NeoOMFS Database
-- Target DBMS: MySQL Server 8.x
-- Aligned 100% with verified entities, role names, and BCrypt-hashed passwords.

USE neoomfs;

-- 1. Insert System Roles
INSERT INTO roles (id, name) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_DOCTOR'),
(3, 'ROLE_FACULTY'),
(4, 'ROLE_STUDENT')
ON DUPLICATE KEY UPDATE name=name;

-- 2. Insert Clinicians / Users
-- BCrypt password hash for word 'password' (strength 12 matches SecurityConfig):
-- $2a$12$V.oE1fH10qD0Z5e2v4RzNeQ8s3T9RzM0Uv4/oE1fH10qD0Z5e2v4R
INSERT INTO users (id, username, full_name, email, password, license_number, department, institution, phone_number, is_active, created_at)
VALUES 
(1, 'aditi_omfs', 'Dr. Aditi Sharma', 'aditi.sharma@simats.edu', '$2a$12$V.oE1fH10qD0Z5e2v4RzNeQ8s3T9RzM0Uv4/oE1fH10qD0Z5e2v4R', 'DCI-OMFS-7729', 'Oral & Maxillofacial Surgery', 'Saveetha Dental College', '+919876543210', TRUE, NOW()),
(2, 'admin_sys', 'NeoOMFS Admin', 'admin@simats.edu', '$2a$12$V.oE1fH10qD0Z5e2v4RzNeQ8s3T9RzM0Uv4/oE1fH10qD0Z5e2v4R', 'SYS-001', 'Clinical Informatics', 'Saveetha Dental College', '+919999999999', TRUE, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- Link Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2), -- Dr. Aditi as ROLE_DOCTOR
(2, 1)  -- Admin as ROLE_ADMIN
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 3. Insert 6 Diverse Clinical Patients
INSERT INTO patients (id, mrn, full_name, age, date_of_birth, gender, blood_group, phone_number, address, emergency_contact, emergency_phone, procedure_type, referring_doctor, assessment_status, is_deleted, created_by_user_id, created_at)
VALUES
(1, 'MRN2026001', 'Rajesh Kumar', 24, '2002-04-12', 'Male', 'O+', '+919840123456', 'No. 12, Gandhi Street, Chennai', 'Karan Kumar', '+919840123457', 'Third Molar Extraction', 'Self', 'APPROVED', FALSE, 1, NOW()),
(2, 'MRN2026002', 'Priya Patel', 42, '1984-08-22', 'Female', 'B+', '+919840223456', 'No. 45, Nehru Nagar, Coimbatore', 'Sanjay Patel', '+919840223457', 'Third Molar Extraction', 'Dr. Aris', 'PENDING_REVIEW', FALSE, 1, NOW()),
(3, 'MRN2026003', 'Somnath Sen', 68, '1958-11-05', 'Male', 'A-', '+919840323456', 'No. 88, Lake View Road, Madurai', 'Rita Sen', '+919840323457', 'Multiple Extractions', 'Dr. Maxillofacial', 'PENDING_REVIEW', FALSE, 1, NOW()),
(4, 'MRN2026004', 'Lakshmi Bai', 28, '1998-01-30', 'Female', 'AB+', '+919840423456', 'No. 3, Temple St, Trichy', 'Ravi Bai', '+919840423457', 'Third Molar Extraction', 'Dr. Prema (OBG)', 'PENDING_REVIEW', FALSE, 1, NOW()),
(5, 'MRN2026005', 'George Varghese', 55, '1971-06-15', 'Male', 'O-', '+919840523456', 'No. 17, Church Rd, Cochin', 'Mini George', '+919840523457', 'Implant Placement', 'Dr. Thomas (Cardio)', 'APPROVED', FALSE, 1, NOW()),
(6, 'MRN2026006', 'Amit Mishra', 35, '1991-09-08', 'Male', 'A+', '+919840623456', 'No. 5, Park Street, Salem', 'Deepa Mishra', '+919840623457', 'Biopsy', 'Dr. Kumar (Hematology)', 'DRAFT', FALSE, 1, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 4. Step 2: Patient Vitals (Fahrenheit temperatures will be normalized to Celsius by VitalsServiceImpl)
INSERT INTO patient_vitals (id, patient_id, bp_systolic, bp_diastolic, temperature, pulse_rate, spo2, respiratory_rate, height_cm, weight_kg, bmi, random_blood_sugar, notes, created_at)
VALUES
-- Rajesh Kumar (Healthy Vitals)
(1, 1, 118, 76, 36.8, 72, 99.0, 16, 172.5, 68.0, 22.9, 90.0, 'Vitals stable and normal.', NOW()),
-- Priya Patel (Uncontrolled Stage II Hypertension)
(2, 2, 178, 104, 37.0, 88, 97.0, 18, 161.0, 72.5, 28.0, 110.0, 'Patient anxious; BP persistently high.', NOW()),
-- Somnath Sen (Elevated BP, normal pulse)
(3, 3, 138, 82, 36.6, 76, 98.0, 17, 168.0, 79.0, 28.0, 240.0, 'RBS is high; scheduled for complete labs.', NOW()),
-- Lakshmi Bai (Pregnant, stable)
(4, 4, 116, 74, 37.1, 84, 99.0, 16, 158.0, 62.0, 24.8, 95.0, 'Vitals normal for gestation.', NOW()),
-- George Varghese (Mildly hypertensive)
(5, 5, 134, 82, 36.9, 70, 98.0, 18, 175.0, 85.0, 27.8, 100.0, 'Patient stable on cardiac therapy.', NOW()),
-- Amit Mishra (Stable vitals)
(6, 6, 120, 80, 36.7, 78, 98.0, 16, 170.0, 70.0, 24.2, 90.0, 'Baseline vitals stable.', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 5. Step 3: Radiology Modalities
INSERT INTO radiology (id, patient_id, iopa_taken, iopa_file_url, iopa_findings, opg_taken, opg_file_url, opg_findings, cbct_taken, cbct_file_url, cbct_findings, bone_density_hu, general_radiology_notes, created_at)
VALUES
(1, 1, TRUE, '/files/radiology/iopa_rajesh.jpg', 'Normal trabeculae; clear margins', TRUE, '/files/radiology/opg_rajesh.jpg', 'Mesioangular impaction #38', FALSE, NULL, NULL, NULL, 'Sufficient bone density for routine procedure.', NOW()),
(2, 2, TRUE, '/files/radiology/iopa_priya.jpg', 'Slight bone loss distally #48', TRUE, '/files/radiology/opg_priya.jpg', 'Horizontal impaction #48', FALSE, NULL, NULL, NULL, 'Clear inferior alveolar nerve proximity.', NOW()),
(3, 3, TRUE, '/files/radiology/iopa_somnath.jpg', 'Horizontal bone loss', TRUE, '/files/radiology/opg_somnath.jpg', 'Distoangular impaction #38', TRUE, '/files/radiology/cbct_somnath.jpg', 'Inferior alveolar nerve contact #38', 450.0, 'Generalised bone density reduction.', NOW()),
(4, 4, FALSE, NULL, NULL, FALSE, NULL, NULL, FALSE, NULL, NULL, NULL, 'Radiology deferred due to pregnancy.', NOW()),
(5, 5, TRUE, '/files/radiology/iopa_george.jpg', 'Adequate bone height', TRUE, '/files/radiology/opg_george.jpg', 'Normal alveolar ridge', TRUE, '/files/radiology/cbct_george.jpg', 'Good density for implant placement', 800.0, 'CBCT done to evaluate cortical bone thickness.', NOW()),
(6, 6, TRUE, '/files/radiology/iopa_amit.jpg', 'Bony radiolucency around #36', FALSE, NULL, NULL, FALSE, NULL, NULL, NULL, 'Evaluating lesion margins.', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 6. Step 4: Laboratory Investigations
INSERT INTO laboratory_investigations (id, patient_id, hemoglobin, total_wbc_count, platelet_count, bleeding_time, clotting_time, pt, inr, aptt, fasting_blood_sugar, random_blood_sugar, hba1c, blood_urea, serum_creatinine, serum_bilirubin_total, sgot, sgpt, blood_group, rh_factor, hiv_status, hbsag_status, hcv_status, lab_report_file_url, notes, created_at)
VALUES
-- Rajesh (Normal Labs)
(1, 1, 14.8, 7200, 260, 3.5, 7.5, 12.5, 1.0, 30.0, 88.0, 90.0, 5.2, 22.0, 0.80, 0.6, 24.0, 22.0, 'O', 'Positive', 'NON-REACTIVE', 'NON-REACTIVE', 'NON-REACTIVE', NULL, 'All haematological markers healthy.', NOW()),
-- Priya (Hypertensive - normal labs)
(2, 2, 13.0, 8400, 220, 4.0, 8.0, 13.0, 1.1, 31.0, 95.0, 110.0, 5.6, 26.0, 0.90, 0.5, 28.0, 25.0, 'B', 'Positive', 'NON-REACTIVE', 'NON-REACTIVE', 'NON-REACTIVE', NULL, 'Kidney function normal.', NOW()),
-- Somnath (Severe Diabetes - HbA1c 9.2%)
(3, 3, 12.5, 6800, 180, 4.5, 8.5, 13.5, 1.1, 33.0, 150.0, 240.0, 9.2, 34.0, 1.10, 0.7, 30.0, 28.0, 'A', 'Negative', 'NON-REACTIVE', 'NON-REACTIVE', 'NON-REACTIVE', NULL, 'Glycemic control poor; postpone elective work.', NOW()),
-- Lakshmi (Pregnant - borderline anemia)
(4, 4, 10.8, 9800, 240, 3.8, 7.8, 12.8, 1.0, 29.0, 85.0, 95.0, 5.0, 18.0, 0.65, 0.4, 20.0, 18.0, 'AB', 'Positive', 'NON-REACTIVE', 'NON-REACTIVE', 'NON-REACTIVE', NULL, 'Mild gestational anemia.', NOW()),
-- George (Hypertensive/Cardiac - elevated INR 3.2 on Warfarin)
(5, 5, 13.5, 7000, 200, 5.0, 10.0, 35.0, 3.2, 35.0, 90.0, 100.0, 5.5, 28.0, 0.95, 0.6, 25.0, 22.0, 'O', 'Negative', 'NON-REACTIVE', 'NON-REACTIVE', 'NON-REACTIVE', NULL, 'High bleeding risk (INR > 2.5). Requires bridging.', NOW()),
-- Amit (Bleeding disorder - severe thrombocytopenia: platelets 45,000)
(6, 6, 10.5, 7500, 45, 8.5, 14.0, 14.5, 1.2, 42.0, 90.0, 90.0, 5.1, 24.0, 0.85, 0.8, 35.0, 30.0, 'A', 'Positive', 'NON-REACTIVE', 'NON-REACTIVE', 'NON-REACTIVE', NULL, 'Critical thrombocytopenia (platelets < 50,000).', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 7. Step 5: Medical History & Systemic Diseases
INSERT INTO medical_history (id, patient_id, hypertension, diabetes, heart_disease, kidney_disease, liver_disease, thyroid_disorder, asthma, epilepsy, blood_disorder, hepatitis, hiv_positive, pregnancy_status, pregnancy_trimester, other_conditions, current_medications, allergies, previous_surgeries, anaesthetic_complications, family_history, social_history, notes, created_at)
VALUES
-- Rajesh (Clean)
(1, 1, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL, 'None', 'None', 'None', 'None', 'None', 'None', 'Social alcohol user', 'Fit surgical candidate.', NOW()),
-- Priya (Hypertensive)
(2, 2, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL, 'None', 'Amlodipine 5mg OD', 'Penicillin', 'Appendectomy (2018)', 'None', 'Father Hypertensive', 'Non-smoker', 'Anxious about penicillin alternative.', NOW()),
-- Somnath (Diabetic)
(3, 3, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL, 'None', 'Metformin 500mg BD', 'Latex, Penicillin', 'None', 'None', 'Family history of diabetes', 'Non-smoker', 'Poor glucose tracking.', NOW()),
-- Lakshmi (Pregnant)
(4, 4, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, 'Second Trimester', 'Gestation 22 weeks', 'Iron and Calcium supplements', 'None', 'None', 'None', 'None', 'Non-smoker', 'OBG cleared for local dental procedures.', NOW()),
-- George (Cardiac / Anticoagulant)
(5, 5, TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, NULL, 'Atrial Fibrillation', 'Warfarin 5mg OD', 'None', 'Coronary Stent (2022)', 'None', 'None', 'Social drinker', 'On active anticoagulant therapy.', NOW()),
-- Amit (Thrombocytopenia)
(6, 6, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE, NULL, 'Idiopathic Thrombocytopenic Purpura (ITP)', 'None', 'None', 'None', 'None', 'None', 'Non-smoker', 'Prone to mucosal bruising.', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 8. Structured Medications (Required for medications check table logic)
INSERT INTO medications (id, patient_id, drug_name, dosage, frequency, route, indication, is_anticoagulant, is_immunosuppressant, created_at)
VALUES
(1, 2, 'Amlodipine', '5mg', 'Once Daily', 'Oral', 'Hypertension', FALSE, FALSE, NOW()),
(2, 3, 'Metformin', '500mg', 'Twice Daily', 'Oral', 'Diabetes mellitus', FALSE, FALSE, NOW()),
(3, 5, 'Warfarin', '5mg', 'Once Daily', 'Oral', 'Atrial Fibrillation', TRUE, FALSE, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 9. Step 6: Dental & Local Mandibular Examinations
INSERT INTO dental_examination (id, patient_id, asa_class, pell_gregory_class, winter_classification, upper_third_molar, difficulty_score, mouth_opening_mm, oral_hygiene_status, periodontal_status, active_infection, swelling, trismus, tooth_number, clinical_examination_notes, created_at)
VALUES
(1, 1, 'I', 'Class I', 'Mesioangular', 'None', 3, 42, 'Good', 'Healthy', FALSE, FALSE, FALSE, '38', 'Simple impaction; nerve not in contact.', NOW()),
(2, 2, 'II', 'Class II', 'Horizontal', 'None', 6, 38, 'Fair', 'Mild gingivitis', FALSE, TRUE, FALSE, '48', 'Operculum swelling present preoperatively.', NOW()),
(3, 3, 'III', 'Class III', 'Distoangular', 'None', 8, 32, 'Poor', 'Moderate periodontitis', TRUE, TRUE, TRUE, '38', 'Severe pericoronitis, active purulent discharge.', NOW()),
(4, 4, 'II', 'Class I', 'Vertical', 'None', 4, 40, 'Good', 'Pregnancy gingivitis', FALSE, FALSE, FALSE, '48', 'Local anesthetic safe (restrict epinephrine).', NOW()),
(5, 5, 'II', 'Class I', 'Mesioangular', 'None', 5, 45, 'Good', 'Healthy bone level', FALSE, FALSE, FALSE, '36', 'Implant planned; check localized bleeding.', NOW()),
(6, 6, 'III', 'Class II', 'Vertical', 'None', 7, 36, 'Fair', 'Healthy gums', FALSE, FALSE, FALSE, '46', 'Local extraction carrying high hemorrhage risk.', NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 10. Step 7: Clinical Decisions (Evaluations)
INSERT INTO clinical_decisions (id, patient_id, risk_level, fitness_decision, risk_score, clinical_alerts, recommendations, decision_notes, generated_by_user_id, generated_at, created_at)
VALUES
-- Rajesh (FIT)
(1, 1, 'LOW', 'FIT', 0, '[]', '["Proceed with standard surgical extraction under local anesthesia."]', 'Fit surgical candidate.', 1, NOW(), NOW()),
-- Priya (CRITICAL - Uncontrolled BP)
(2, 2, 'HIGH', 'CRITICAL', 35, '["Hypertension Alert: BP is 178/104 mmHg. Stage II Hypertension."]', '["Defer elective surgical extraction until BP is stabilized.", "Refer to physician for antihypertensive therapy optimization."]', 'Postponed due to dangerous hypertensive levels.', 1, NOW(), NOW()),
-- Somnath (REVIEW - High Sugar)
(3, 3, 'HIGH', 'REVIEW', 30, '["Hyperglycemia Alert: HbA1c is 9.2%. Uncontrolled Diabetes."]', '["Ensure preoperative antibiotic coverage (amoxicillin 2g) to prevent dry socket.", "Coordinate with diabetologist for insulin management."]', 'Surgical clearance pending blood glucose control.', 1, NOW(), NOW()),
-- Lakshmi (REVIEW - Pregnancy)
(4, 4, 'MODERATE', 'REVIEW', 20, '["Pregnancy Warning: Patient is in the second trimester."]', '["Obtain formal clearance from OBG before extraction.", "Avoid prescribing NSAIDs; use paracetamol for analgesia.", "Limit epinephrine in local anesthetic to max 2 carpules."]', 'Second trimester is safest for treatment, but requires OBG consent.', 1, NOW(), NOW()),
-- George (CRITICAL - Elevated INR)
(5, 5, 'HIGH', 'CRITICAL', 40, '["Anticoagulant Alert: INR is 3.2. Extreme hemorrhage risk."]', '["Consult cardiologist to bridge Warfarin with LMWH.", "Prepare local hemostatic agents (gelatin sponge, sutures)."]', 'High bleeding risk (INR > 2.5). Do not extract.', 1, NOW(), NOW()),
-- Amit (CRITICAL - Low Platelets)
(6, 6, 'HIGH', 'CRITICAL', 50, '["Thrombocytopenia Alert: Platelet count is 45,000 /uL."]', '["Postpone extraction due to critical platelet count.", "Obtain hematologist clearance; prepare for platelet transfusion if surgery is urgent."]', 'Platelet level below 50,000 is contraindicated for extraction.', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 11. Step 8: Assessment Reports & Audit Logs
INSERT INTO assessment_reports (id, patient_id, report_file_path, report_file_name, report_generated_at, report_version, generated_by_user_id, created_at)
VALUES
(1, 1, 'backend/reports/Report_Rajesh_Kumar_v1.pdf', 'Report_Rajesh_Kumar_v1.pdf', NOW(), 1, 1, NOW()),
(2, 2, 'backend/reports/Report_Priya_Patel_v1.pdf', 'Report_Priya_Patel_v1.pdf', NOW(), 1, 1, NOW()),
(3, 3, 'backend/reports/Report_Somnath_Sen_v1.pdf', 'Report_Somnath_Sen_v1.pdf', NOW(), 1, 1, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- Seed Audit logs
INSERT INTO audit_logs (id, user_id, username, action, entity_type, entity_id, description, ip_address, timestamp)
VALUES
(1, 1, 'aditi_omfs', 'LOGIN', 'User', 1, 'User logged in: aditi.sharma@simats.edu', '10.0.12.82', NOW()),
(2, 1, 'aditi_omfs', 'CREATE', 'Patient', 1, 'Created patient record for MRN: MRN2026001', '10.0.12.82', NOW()),
(3, 1, 'aditi_omfs', 'CREATE', 'Patient', 2, 'Created patient record for MRN: MRN2026002', '10.0.12.82', NOW())
ON DUPLICATE KEY UPDATE id=id;
