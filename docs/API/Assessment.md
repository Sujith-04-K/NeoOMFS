# REST API Contract: Triage Assessment

Exposes endpoints for submitting vital signs, laboratory metrics, medical history, local examinations, and trigger decision support calculations.

## 1. Submit Triage Parameters
- **Endpoint**: `POST /api/v1/assessments/triage`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Payload**:
```json
{
  "patientId": 12,
  "vitals": {
    "bpSys": 120,
    "bpDia": 80,
    "pulseRate": 72,
    "temperature": 98.4,
    "respiratoryRate": 16,
    "spo2": 98
  },
  "labs": {
    "bloodGroup": "O Positive",
    "glucoseRbs": 104,
    "glucoseFbs": 88,
    "bleedingTime": 3.5,
    "clottingTime": 7.5,
    "hb": 14.8,
    "wbc": 7200,
    "platelets": 260000,
    "pt": 12.5,
    "inr": 1.01
  },
  "history": {
    "smoking": false,
    "alcohol": false,
    "diet": "Normal Mixed Diet",
    "systemicDiseases": ["None"],
    "medications": []
  },
  "dentalExam": {
    "mouthOpening": 42,
    "toothNumber": 38,
    "impactionType": "Soft Tissue Impaction",
    "pellGregory": "Class I, Position A",
    "winterClass": "Mesioangular",
    "upperThird": "Class A",
    "swelling": false,
    "infection": false,
    "difficulty": "Easy",
    "notes": "No nerve proximity"
  }
}
```
- **Success Response (200 OK)**:
```json
{
  "status": "Success",
  "assessmentId": 45,
  "riskLevel": "LOW RISK",
  "fitnessDecision": "Fit for Surgery",
  "criticalAlerts": [],
  "yellowAlerts": [],
  "clearances": [],
  "recommendations": [
    "Proceed with standard aseptic surgical extraction protocol under local anesthesia."
  ]
}
```
