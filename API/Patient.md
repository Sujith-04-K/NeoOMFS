# REST API Contract: Patient Management

Exposes endpoints for patient registration, directory search, and retrieving patient records.

## 1. Register Patient
- **Endpoint**: `POST /api/v1/patients`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Payload**:
```json
{
  "name": "Rajesh Kumar",
  "age": 24,
  "gender": "Male",
  "proposedProcedure": "Surgical Extraction of #38",
  "asaClassification": 1,
  "allergies": ["None"]
}
```
- **Success Response (201 Created)**:
```json
{
  "status": "Success",
  "message": "Patient registered successfully",
  "patientId": 12
}
```

## 2. Search Patients
- **Endpoint**: `GET /api/v1/patients/search`
- **Headers**: `Authorization: Bearer <token>`
- **Query Parameters**: `query=Rajesh`
- **Success Response (200 OK)**:
```json
[
  {
    "id": 12,
    "name": "Rajesh Kumar",
    "age": 24,
    "gender": "Male",
    "proposedProcedure": "Surgical Extraction of #38",
    "asaClassification": 1,
    "allergies": ["None"]
  }
]
```
