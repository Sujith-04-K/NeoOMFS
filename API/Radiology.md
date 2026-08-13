# REST API Contract: Radiology Metadata

Exposes endpoints for posting and retrieving patient radiological scan metadata.

## 1. Register Scan Upload Metadata
- **Endpoint**: `POST /api/v1/radiology/metadata`
- **Headers**: `Authorization: Bearer <token>`, `Content-Type: application/json`
- **Request Payload**:
```json
{
  "patientId": 12,
  "scanType": "OPG",
  "fileName": "Patient_OPG_OPG-902.png",
  "fileSize": "2.1 MB"
}
```
- **Success Response (200 OK)**:
```json
{
  "status": "Success",
  "message": "OPG scan metadata registered successfully"
}
```
