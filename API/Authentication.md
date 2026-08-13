# REST API Contract: Authentication

Exposes endpoints for user registration, user login, and license verification.

## 1. Clinician Sign-up
- **Endpoint**: `POST /api/v1/auth/signup`
- **Headers**: `Content-Type: application/json`
- **Request Payload**:
```json
{
  "name": "Dr. Aditi Sharma",
  "licenseNumber": "DCI-OMFS-7729",
  "email": "aditi.sharma@simats.edu",
  "password": "SecurePassword123",
  "institution": "Saveetha Dental College"
}
```
- **Success Response (201 Created)**:
```json
{
  "status": "Success",
  "message": "User registered successfully",
  "userId": 1
}
```

## 2. Clinician Login
- **Endpoint**: `POST /api/v1/auth/login`
- **Headers**: `Content-Type: application/json`
- **Request Payload**:
```json
{
  "email": "aditi.sharma@simats.edu",
  "password": "SecurePassword123"
}
```
- **Success Response (200 OK)**:
```json
{
  "status": "Success",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "SURGEON"
}
```
