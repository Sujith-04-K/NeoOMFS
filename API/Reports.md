# REST API Contract: Preoperative Reports

Exposes endpoints for generating and downloading patient PDF reports.

## 1. Retrieve Assessment PDF Report
- **Endpoint**: `GET /api/v1/reports/pdf/{assessmentId}`
- **Headers**: `Authorization: Bearer <token>`
- **Response Format**: `application/pdf` binary stream
- **Success Response (200 OK)**: Downloads `Pre-Op_Fitness_Report_<Patient_Name>.pdf`.
