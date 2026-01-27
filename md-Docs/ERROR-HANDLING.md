# API Error HAndled Scenarios

## Overview

This document lists all possible error scenarios in the Library Management System with their corresponding HTTP status codes and error responses.

---

## 📋 Error Scenarios

### **1. Duplicate User Registration**

**Scenario:** User tries to register with an existing username or email

**Endpoint:** `POST /api/signup/register`

**HTTP Status:** `409 Conflict`

**Response:**
```json
{
    "timestamp": "2026-01-27T08:14:04.643449501",
    "status": 409,
    "error": "Conflict",
    "message": "User with the Username or Email already Exists"
}
```

---

### **2. Missing Required Fields**

**Scenario:** Registration request missing required fields

**Endpoint:** `POST /api/signup/register`

**HTTP Status:** `400 Bad Request`

**Response:**
```json
{
    "validationErrors": {
        "password": "Invalid password",
        "email": "Email is required"
    },
    "error": "Validation Failed",
    "timestamp": "2026-01-27T08:16:37.115846544",
    "status": 400
}
```

---

### **3. Custom Validation Failures**

**Scenario:** Password doesn't meet complexity requirements

**Endpoint:** `POST /api/signup/register`

**HTTP Status:** `400 Bad Request`

**Response:**
```json
{
    "validationErrors": {
        "password": "Password must contain 1 or more digit characters., Password must contain 1 or more special characters."
    },
    "error": "Validation Failed",
    "timestamp": "2026-01-27T08:16:53.186442052",
    "status": 400
}
```

---

### **4. Invalid Login Credentials**

**Scenario:** Wrong username or password during login

**Endpoint:** `POST /api/auth/login`

**HTTP Status:** `401 Unauthorized`

**Response:**
```json
{
    "timestamp": "2026-01-27T08:17:39.530661129",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid username or password"
}
```

---

### **5. Invalid JWT Token**

**Scenario:** Tampered or invalid JWT token in request

**Endpoint:** `GET /api/books`

**HTTP Status:** `401 Unauthorized`

**Response:**
```json
{
    "timestamp": "2026-01-27T08:18:37.665793586",
    "status": 401,
    "error": "UNAUTHORIZED",
    "message": "Token signature verification failed - token may have been tampered with"
}
```

---

### **6. Duplicate ISBN**

**Scenario:** Book with same ISBN already exists

**Endpoint:** `POST /api/books`

**HTTP Status:** `409 Conflict`

**Response:**
```json
{
    "timestamp": "2026-01-27T08:18:12.880036922",
    "status": 409,
    "error": "Conflict",
    "message": "Book with ISBN 'isban_4455' already exists"
}
```

---

### **7. Future Publication Year**

**Scenario:** Publication year is set to a future date

**Endpoint:** `POST /api/books`

**HTTP Status:** `400 Bad Request`

**Response:**
```json
{
    "validationErrors": {
        "publishedYear": "Publication year cannot be in the future"
    },
    "error": "Validation Failed",
    "timestamp": "2026-01-27T08:19:12.017235172",
    "status": 400
}
```

---

### **8. Service Unavailable**

**Scenario:** Microservice is down or not yet started

**Endpoint:** `POST /api/signup/register`

**HTTP Status:** `503 Service Unavailable`

**Response:**
```json
{
    "error": "Service Unavailable",
    "message": "signup-service is currently unavailable. Please try again later.",
    "timestamp": "2026-01-27T08:22:36.028373835",
    "status": 503
}
```

---

### **9. Invalid Endpoint**

**Scenario:** Non-existent API endpoint accessed

**Endpoint:** `GET /apsi/books/search?query=romeo` (typo in URL)

**HTTP Status:** `404 Not Found`

**Response:**
```json
{
    "error": "Not Found",
    "message": "The requested service is currently unavailable. Please try again later.404 NOT_FOUND \"No static resource apsi/books/search.\"",
    "timestamp": "2026-01-27T08:26:54.722980261",
    "status": 404
}
```

---

## Error Summary Table

| Error Type | HTTP Status | Endpoint | Trigger |
|------------|-------------|----------|---------|
| Duplicate User | 409 | POST /api/signup/register | Existing username/email |
| Missing Fields | 400 | POST /api/signup/register | Required fields missing |
| Validation Failed | 400 | POST /api/signup/register | Password complexity |
| Invalid Credentials | 401 | POST /api/auth/login | Wrong username/password |
| Invalid Token | 401 | GET /api/books | Tampered JWT token |
| Duplicate ISBN | 409 | POST /api/books | Existing ISBN |
| Invalid Year | 400 | POST /api/books | Future publication year |
| Service Down | 503 | Any endpoint | Microservice unavailable |
| Invalid Endpoint | 404 | Any endpoint | Wrong URL path |

---

## Error Response Structure

All error responses follow a consistent structure:

```json
{
    "timestamp": "<ISO-8601 timestamp>",
    "status": <HTTP status code>,
    "error": "<Error type>",
    "message": "<Detailed error message>",
    "validationErrors": {  // Only for validation errors
        "<field>": "<validation message>"
    }
}
```

---

##  Error Categories

### **Client Errors (4xx)**

| Status | Category | Description |
|--------|----------|-------------|
| 400 | Bad Request | Validation failures, missing fields |
| 401 | Unauthorized | Invalid credentials, expired/invalid token |
| 404 | Not Found | Invalid endpoint or resource not found |
| 409 | Conflict | Duplicate data (username, email, ISBN) |

### **Server Errors (5xx)**

| Status | Category | Description |
|--------|----------|-------------|
| 503 | Service Unavailable | Microservice down or not responding |

---

## Testing Error Scenarios

### **Using cURL:**

```bash
# Test duplicate user
curl -X POST http://localhost:8080/api/signup/register \
  -H "Content-Type: application/json" \
  -d '{"username":"existing_user",...}'

# Test invalid login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wrong","password":"wrong"}'

# Test invalid token
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer invalid_token"

# Test duplicate ISBN
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"isbn":"existing_isbn",...}'
```

---


