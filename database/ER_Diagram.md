# NeoOMFS Entity Relationship (ER) Diagram
This document models the production database entity relationships of the NeoOMFS preoperative triage and decision support system.

```mermaid
erDiagram
    users ||--o{ user_roles : "has"
    roles ||--o{ user_roles : "defines"
    users ||--|| refresh_tokens : "generates"
    users ||--o{ patients : "creates"
    users ||--o{ notifications : "receives"
    users ||--o{ clinical_decisions : "evaluates"
    users ||--o{ assessment_reports : "signs"
    users ||--o{ uploaded_files : "uploads"

    patients ||--|| patient_vitals : "possesses"
    patients ||--|| radiology : "captures"
    patients ||--|| laboratory_investigations : "tests"
    patients ||--|| medical_history : "records"
    patients ||--o{ medications : "takes"
    patients ||--|| dental_examination : "undergoes"
    patients ||--|| clinical_decisions : "evaluates"
    patients ||--o{ assessment_reports : "generates"
    patients ||--o{ uploaded_files : "contains"

    users {
        bigint id PK
        string username UNIQUE
        string full_name
        string email UNIQUE
        string password
        string license_number
        string department
        string institution
        string phone_number
        boolean is_active
        datetime last_login
        string password_reset_token
        datetime password_reset_expiry
        datetime created_at
        datetime updated_at
    }

    roles {
        int id PK
        string name UNIQUE
    }

    user_roles {
        bigint user_id PK, FK
        int role_id PK, FK
    }

    refresh_tokens {
        bigint id PK
        string token UNIQUE
        datetime expiry_date
        bigint user_id FK, UNIQUE
    }

    patients {
        bigint id PK
        string mrn UNIQUE
        string full_name
        int age
        date date_of_birth
        string gender
        string blood_group
        string phone_number
        string address
        string emergency_contact
        string emergency_phone
        string procedure_type
        string referring_doctor
        string assessment_status
        boolean is_deleted
        bigint created_by_user_id FK
        datetime created_at
        datetime updated_at
    }

    patient_vitals {
        bigint id PK
        bigint patient_id FK, UNIQUE
        int bp_systolic
        int bp_diastolic
        decimal temperature
        int pulse_rate
        decimal spo2
        int respiratory_rate
        decimal height_cm
        decimal weight_kg
        decimal bmi
        decimal random_blood_sugar
        string notes
        datetime created_at
        datetime updated_at
    }

    radiology {
        bigint id PK
        bigint patient_id FK, UNIQUE
        boolean iopa_taken
        string iopa_file_url
        string iopa_findings
        boolean opg_taken
        string opg_file_url
        string opg_findings
        boolean cbct_taken
        string cbct_file_url
        string cbct_findings
        double bone_density_hu
        string general_radiology_notes
        datetime created_at
        datetime updated_at
    }

    laboratory_investigations {
        bigint id PK
        bigint patient_id FK, UNIQUE
        decimal hemoglobin
        int total_wbc_count
        int platelet_count
        decimal bleeding_time
        decimal clotting_time
        decimal pt
        decimal inr
        decimal aptt
        decimal fasting_blood_sugar
        decimal random_blood_sugar
        decimal hba1c
        decimal blood_urea
        decimal serum_creatinine
        decimal serum_bilirubin_total
        decimal sgot
        decimal sgpt
        string blood_group
        string rh_factor
        string hiv_status
        string hbsag_status
        string hcv_status
        string lab_report_file_url
        string notes
        datetime created_at
        datetime updated_at
    }

    medical_history {
        bigint id PK
        bigint patient_id FK, UNIQUE
        boolean hypertension
        boolean diabetes
        boolean heart_disease
        boolean kidney_disease
        boolean liver_disease
        boolean thyroid_disorder
        boolean asthma
        boolean epilepsy
        boolean blood_disorder
        boolean hepatitis
        boolean hiv_positive
        boolean pregnancy_status
        string pregnancy_trimester
        string other_conditions
        text current_medications
        text allergies
        text previous_surgeries
        string anaesthetic_complications
        string family_history
        string social_history
        string notes
        datetime created_at
        datetime updated_at
    }

    medications {
        bigint id PK
        bigint patient_id FK
        string drug_name
        string dosage
        string frequency
        string route
        string indication
        boolean is_anticoagulant
        boolean is_immunosuppressant
        datetime created_at
        datetime updated_at
    }

    dental_examination {
        bigint id PK
        bigint patient_id FK, UNIQUE
        string asa_class
        string pell_gregory_class
        string winter_classification
        string upper_third_molar
        int difficulty_score
        int mouth_opening_mm
        string oral_hygiene_status
        string periodontal_status
        boolean active_infection
        boolean swelling
        boolean trismus
        string tooth_number
        text clinical_examination_notes
        datetime created_at
        datetime updated_at
    }

    clinical_decisions {
        bigint id PK
        bigint patient_id FK, UNIQUE
        string risk_level
        string fitness_decision
        int risk_score
        text clinical_alerts
        text recommendations
        text decision_notes
        bigint generated_by_user_id FK
        datetime generated_at
        datetime created_at
        datetime updated_at
    }

    assessment_reports {
        bigint id PK
        bigint patient_id FK
        string report_file_path
        string report_file_name
        datetime report_generated_at
        int report_version
        bigint generated_by_user_id FK
        datetime created_at
        datetime updated_at
    }

    notifications {
        bigint id PK
        bigint recipient_user_id FK
        string title
        text message
        string type
        boolean is_read
        bigint related_patient_id
        datetime created_at
        datetime updated_at
    }

    audit_logs {
        bigint id PK
        bigint user_id
        string username
        string action
        string entity_type
        bigint entity_id
        string description
        string ip_address
        datetime timestamp
    }

    uploaded_files {
        bigint id PK
        string original_file_name
        string stored_file_name
        string file_type
        string file_category
        bigint file_size
        bigint patient_id FK
        bigint uploaded_by_user_id FK
        datetime upload_date
    }
```

## Relational Explanations

1. **USERS to ROLES (M:N via user_roles)**: Enables flexible, clean Role-Based Access Control mapping. A user can possess multiple credentials.
2. **PATIENTS to Wizard Steps (1:1)**: Ensures a unified single source of truth for the current clinical surgical parameters (`patient_vitals`, `radiology`, `laboratory_investigations`, `medical_history`, `dental_examination`, `clinical_decisions`).
3. **PATIENTS to medications, assessment_reports, uploaded_files (1:N)**: A patient can take multiple medications, undergo multiple assessments over time (saving distinct reports), and store several diagnostic file uploads.
