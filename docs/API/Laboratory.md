# REST API Contract: Laboratory Parameters

Exposes endpoints for submitting and verifying laboratory indices separately if needed.

## 1. Verify Laboratory Boundaries
- **Endpoint**: `POST /api/v1/labs/verify`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Payload**:
```json
{
  "glucoseRbs": 190,
  "platelets": 95000,
  "inr": 1.6
}
```
- **Success Response (200 OK)**:
```json
{
  "isValid": false,
  "warnings": [
    "Uncontrolled Hyperglycemia: RBS is 190 mg/dL.",
    "Thrombocytopenia Alert: Platelets are low at 95000 /µL.",
    "Elevated INR Alert: INR is high at 1.6."
  ]
}
```
