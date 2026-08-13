# NeoOMFS Database Design Specification

This document details the relational database tables, columns, indexes, and integrity constraints for the NeoOMFS Preoperative Assessment system.

---

## 1. Table: `users`
Represents registered clinical operators / oral surgeons.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique surgeon identifier |
| `name` | VARCHAR(100) | NOT NULL | Full name of clinician |
| `license_number`| VARCHAR(50) | NOT NULL, UNIQUE | Medical/Dental council license number |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | Login credential email address |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt encrypted credential password |
| `institution` | VARCHAR(150) | NULL | Affiliated hospital / university |
| `role` | VARCHAR(30) | DEFAULT 'SURGEON' | System access control privileges |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Date user registered |

---

## 2. Table: `patients`
Stores demographics of patients undergoing assessment.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique patient identifier |
| `user_id` | INT | FOREIGN KEY REFERENCES `users(id)` | Surgeon managing the record |
| `name` | VARCHAR(100) | NOT NULL | Patient name |
| `age` | INT | NOT NULL | Patient age |
| `gender` | VARCHAR(20) | NOT NULL | Male / Female / Other |
| `proposed_procedure`| VARCHAR(150)| NOT NULL | E.g. "Surgical Extraction of #38" |
| `asa_classification`| INT | DEFAULT 1 | ASA physical status class (I - VI) |
| `allergies` | TEXT | NULL | JSON or comma-separated list of allergies |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration date |

---

## 3. Table: `patient_vitals`
Stores physiological vital signs (1:1 with `patients`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | INT | PRIMARY KEY, FK REFERENCES `patients(id)` | Parent patient ID |
| `bp_sys` | INT | NOT NULL | Systolic Blood Pressure (mmHg) |
| `bp_dia` | INT | NOT NULL | Diastolic Blood Pressure (mmHg) |
| `pulse_rate` | INT | NOT NULL | Heart rate (BPM) |
| `temperature` | DECIMAL(4,1) | NOT NULL | Body temperature (°F) |
| `respiratory_rate`| INT | NOT NULL | Breathing rate (BPM) |
| `spo2` | INT | NOT NULL | Blood oxygen saturation (%) |
| `height` | DECIMAL(5,1) | NULL | Height (cm) |
| `weight` | DECIMAL(5,1) | NULL | Weight (kg) |
| `bmi` | DECIMAL(4,1) | NULL | Body Mass Index (kg/m²) |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last vitals update |

---

## 4. Table: `patient_labs`
Stores clinical laboratory investigation metrics (1:1 with `patients`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | INT | PRIMARY KEY, FK REFERENCES `patients(id)` | Parent patient ID |
| `blood_group` | VARCHAR(10) | NOT NULL | E.g. "O Positive", "AB Negative" |
| `glucose_rbs` | INT | NOT NULL | Random Blood Sugar (mg/dL) |
| `glucose_fbs` | INT | NOT NULL | Fasting Blood Sugar (mg/dL) |
| `bleeding_time_mins`| DECIMAL(4,2)| NOT NULL | Hemostasis Bleeding Time |
| `clotting_time_mins`| DECIMAL(4,2)| NOT NULL | Hemostasis Clotting Time |
| `hb_g_dl` | DECIMAL(4,1) | NOT NULL | Hemoglobin concentration (g/dL) |
| `wbc_per_ul` | INT | NOT NULL | White Blood Cell count (/µL) |
| `platelets_per_ul` | INT | NOT NULL | Platelet count (/µL) |
| `pt_seconds` | DECIMAL(4,1) | NOT NULL | Prothrombin Time (seconds) |
| `inr_ratio` | DECIMAL(3,2) | NOT NULL | International Normalized Ratio |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last labs update |

---

## 5. Table: `patient_medical_history`
Stores systemic medical history and drug schedules (1:1 with `patients`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | INT | PRIMARY KEY, FK REFERENCES `patients(id)` | Parent patient ID |
| `smoking` | BOOLEAN | DEFAULT FALSE | Active smoker status |
| `alcohol` | BOOLEAN | DEFAULT FALSE | Alcohol consumption status |
| `diet` | VARCHAR(50) | DEFAULT 'Normal Mixed Diet' | Diet plans |
| `systemic_diseases`| TEXT | NULL | Checked chronic conditions list |
| `active_medications`| TEXT | NULL | JSON drugs (name, dosage, frequency) |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last history update |

---

## 6. Table: `patient_dental_exam`
Stores localized intraoral and dental classifications (1:1 with `patients`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `patient_id` | INT | PRIMARY KEY, FK REFERENCES `patients(id)` | Parent patient ID |
| `mouth_opening_mm`| INT | NOT NULL | Interincisal opening distance |
| `tooth_number` | INT | NOT NULL | Target tooth index (e.g. 38, 48) |
| `impaction_type` | VARCHAR(50) | NOT NULL | Soft Tissue / Partial / Complete Bony |
| `pell_gregory_class`| VARCHAR(50)| NOT NULL | Ramus Space & Occlusal Depth class |
| `winter_class` | VARCHAR(50) | NOT NULL | Third molar angulation class |
| `upper_third_class`| VARCHAR(50) | NOT NULL | Upper molar height class |
| `swelling` | BOOLEAN | DEFAULT FALSE | Active localized swelling |
| `infection` | BOOLEAN | DEFAULT FALSE | Active localized pericoronal infection |
| `surgical_difficulty`| VARCHAR(30)| DEFAULT 'Moderate' | Difficulty rating (Easy/Moderate/Difficult)|
| `clinical_notes` | TEXT | NULL | Custom operator descriptions |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last dental exam update |

---

## 7. Table: `preoperative_assessments`
Tracks compiled risk scores and final fitness clearances (1:M with `patients`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INT | PRIMARY KEY, AUTO_INCREMENT | Assessment instance ID |
| `patient_id` | INT | FOREIGN KEY REFERENCES `patients(id)` | Associated patient profile |
| `risk_level` | VARCHAR(30) | NOT NULL | LOW, MEDIUM, or HIGH RISK |
| `critical_alerts` | TEXT | NULL | JSON string array of red alerts |
| `fitness_decision` | VARCHAR(50) | NOT NULL | Fit / Fit with Modification / Not Fit |
| `fitness_remarks` | TEXT | NULL | Clinical clinician summaries |
| `treatment_recommendations`| TEXT | NULL | Custom rule-based action instructions |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Generation date |

---

## Indexes & Performance Tuning

1. **`idx_patients_name`**: Indexes patient name strings to speed up dashboard case searches.
2. **`idx_assessments_risk`**: Indexes assessment risk level fields to query high-risk patients efficiently for clinical dashboard graphs.
3. **`ON DELETE CASCADE`**: Restricts orphan records; deleting a patient automatically purges vital metrics, lab metrics, history metrics, exams, and generated fitness assessments.
