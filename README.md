# NeoOMFS Preoperative Surgical Fitness Assessment System

NeoOMFS is a Clinical Decision Support System (CDSS) designed for oral and maxillofacial surgeons to execute preoperative surgical fitness triage. The system evaluates vital signs, laboratory boundaries, radiological uploads, and systemic medical histories, generating rule-based risk classifications (Low, Medium, High) and surgical recommendations.

---

## Repository Structure

```text
NeoOMFS/
├── frontend/                     # Android Jetpack Compose + XML Views Client App
├── backend/                      # Spring Boot REST API + CDSS Decision Engine
├── database/                     # MySQL Relational Schema and Seed Datasets
├── docs/                         # Specifications, Database designs, and Manuals
├── assets/                       # Static media elements
├── README.md                     # Root project documentation
├── LICENSE                       # Project licensing (MIT)
└── .gitignore                    # Git file exclusions configuration
```

---

## Key Features

1. **Android Guided Triage Wizard (8 Steps)**:
   - Step 1: Patient Demographics & ASA Selection
   - Step 2: Vital Signs Check (Real-time bounds validators)
   - Step 3: Radiology upload checklists (IOPA, OPG, CBCT)
   - Step 4: Laboratory investigations input (Sugar, blood counts, PT/INR)
   - Step 5: Medical History & Outpatient Medication log
   - Step 6: Dental Impaction Classifications (Pell & Gregory, Winter)
   - Step 7: Clinical Decision Support alerts (Red warnings, Yellow cautions)
   - Step 8: Preoperative Report summary, fitness decision, and PDF compile
2. **Spring Boot Clinical Decision Support Engine**:
   - Automated rules checking vital ranges, thrombocytopenia risk, hyperglycemia risk, and coagulation anomalies.
   - Recommends specialized clearances and procedural modifications (e.g. antibiotic coverage, epinephrine limits).
3. **Relational MySQL Database**:
   - Normalized database tables modeling patient clinical timelines (vitals, labs, dental metrics, and assessment logs).

---

## Technical Setup & Execution

For step-by-step setup guides, refer to:
- [Installation and Setup Guide](docs/Installation_Guide.md)
- [Database Schema Dictionary](docs/Database_Design.md)
- [REST API Endpoint specifications](docs/API/)
- [System Architecture Specification](docs/Architecture.md)
- [Operator User Manual](docs/User_Manual.md)

---

## Licensing
This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
