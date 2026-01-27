# Swagger/OpenAPI Documentation Guide

## Overview

All microservices in the Library Management System are documented using **Swagger/OpenAPI 3.0** via the `springdoc-openapi` library. Each service exposes interactive API documentation through Swagger UI.

---

## Swagger UI URLs

Once all services are running, you can access their Swagger documentation at the following URLs:

### 1. **Signup Service**
- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- **Port**: 8081
- **Description**: User registration and signup APIs

### 2. **Login Service**
- **Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)
- **Port**: 8082
- **Description**: Authentication and JWT token management APIs
- **Security**: Supports JWT Bearer token authentication

### 3. **Book Service**
- **Swagger UI**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs)
- **Port**: 8083
- **Description**: Complete book inventory management with CRUD, search, pagination, and wishlist
- **Security**: Requires JWT Bearer token for all endpoints (except health check)

### 4. **Notification Service**
- **Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8084/v3/api-docs](http://localhost:8084/v3/api-docs)
- **Port**: 8084
- **Description**: Asynchronous notification service (Kafka consumer)
- **Note**: Primarily a background service with health check endpoint

### 5. **API Gateway** (Access all services through gateway)
- **Gateway URL**: [http://localhost:8080](http://localhost:8080)
- **Description**: Routes requests to all backend services
- **Routes**:
  - `/signup/**` → Signup Service (port 8081)
  - `/auth/**` → Login Service (port 8082)
  - `/books/**` → Book Service (port 8083)
  - `/notification/**` → Notification Service (port 8084)

---

## How to Use Swagger UI

### Step 1:  Bring up the application 
- Method-1 : Without using dockerised microservices
#### Start kafka and mysql using docker-compose
  - Ensure you are in the root directory of the project
  - You have docker and compose installed

Run below command in a terminal:
```bash
docker-compose -f docker-compose-lms-end-to-end.yaml up
```

Then services individually:
```bash
# Terminal 1 - Service Discovery
cd service-discovery && mvn spring-boot:run

# Terminal 2 - API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 3 - Signup Service
cd signup-service && mvn spring-boot:run

# Terminal 4 - Login Service
cd login-service && mvn spring-boot:run

# Terminal 5 - Book Service
cd book-service && mvn spring-boot:run

# Terminal 6 - Notification Service
cd notification-service && mvn spring-boot:run
```
- Method-2 : Without using dockerised microservices

Run below command
```bash
sh run-lms-end-to-end-with-docker.sh
```
### Step 2: Access Swagger UI

Navigate to any of the Swagger UI URLs listed above. For example:
- [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) for Signup Service
- [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) for Book Service

### Step 3: Test APIs Using Swagger UI

#### For Public Endpoints (No Authentication Required):
1. **Signup Service**: Create a new user
   - Expand `POST /api/signup/register`
   - Click "Try it out"
   - Fill in the request body with user details
   - Click "Execute"

2. **Login Service**: Authenticate and get JWT token
   - Expand `POST /api/auth/login`
   - Click "Try it out"
   - Enter username and password
   - Click "Execute"
   - **Copy the JWT token from the response** (you'll need this for protected endpoints)

#### For Protected Endpoints (Requires JWT):
1. Click the **"Authorize"** button at the top right of Swagger UI
2. In the "Available authorizations" dialog:
   - Enter: `Bearer <your-jwt-token>` (replace `<your-jwt-token>` with actual token from login)
   - Click "Authorize"
   - Click "Close"

3. Now you can test protected endpoints:
   - **Book Service**: Create, read, update, delete books
   - **Book Service**: Search books, add to wishlist
   - All requests will automatically include the JWT token in the `Authorization` header

---


## API Documentation Summary

### Signup Service (Port 8081)
| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api//signup/register` | POST | Register new user | No |
| `/api/signup/health` | GET | Health check | No |

### Login Service (Port 8082)
| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/auth/login` | POST | User login, get JWT | No |
| `/api/auth/health` | GET | Health check | No |

### Book Service (Port 8083)
| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/books` | POST | Create new book | Yes |
| `/api/books` | GET | Get all books (paginated, filterable, optional search params - Author/PusblishedYear) | Yes |
| `/api/books/{id}` | GET | Get book by ID | Yes |
| `/api/books/{id}` | PUT | Update book . This api will also trigger notificationwhen moved rom borrowed to AVAILABLE for wishlisted book | Yes |
| `/api/books/{id}` | DELETE | Soft delete book | Yes |
| `/api/books/search` | GET | Search books by partial text match on either title/author | Yes |
| `/api/books/wishlist` | POST | Add book to wishlist | Yes |
| `/api/books/health` | GET | Health check | No |

### Notification Service (Port 8084)
| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/notification/health` | GET | Health check | No |

---

## Testing Workflow Example

### Complete End-to-End Test via Swagger:

1. **Register a User** (Signup Service):
   ```
   POST http://localhost:8081/api/signup/register
   {
      "username": "test-username",
      "email": "test-user@spry.com",
      "password": "ProtectedPass123!#",
      "first-name": "Mohit",
      "last-name": "Yadav"
   }
   ```

2. **Login** (Login Service):
   ```
   POST http://localhost:8082/api/auth/login
   {
     "username": "test-username",
     "password": "ProtectedPass123!#"
   }
   ```
   **Copy the JWT token from response**

3. **Authorize** in Book Service Swagger UI:
   - Click "Authorize" button
   - Enter: `Bearer <jwt-token>`
   - Click "Authorize"

4. **Create a Book** (Book Service):
   ```
   POST http://localhost:8083/api/books
   {
     "title": "new Book - romeo4555",
      "author": "Mohit Kumar Yadav docker",
      "isbn": "isban_4455",
      "published-year": 2011,
      "availability-status": "AVAILABLE"
   }
   ```

5. **Search Books**:
   ```
   GET http://localhost:8083/api/books/search?query=Clean
   ```

6. **Add to Wishlist**:
   ```
   POST http://localhost:8083/api/books/wishlist
   {
     "userId": "6",
     "bookId": 1
   }
   ```

7. **Update Book Status** (triggers Kafka notification):
   ```
   PUT http://localhost:8083/api/books/1
   {
     "title": "new Book - romeo4555",
      "author": "Mohit Kumar Yadav docker",
      "isbn": "isban_4455",
      "published-year": 2011,
      "availability-status": "AVAILABLE"
   }
   ```

---

## Accessing via API Gateway

You can also access all services through the API Gateway:

- **Signup**: `http://localhost:8080/signup/register`
- **Login**: `http://localhost:8080/auth/login`
- **Books**: `http://localhost:8080/books`

The gateway automatically routes requests to the appropriate service.

---

## OpenAPI JSON Export

Each service provides a machine-readable OpenAPI specification:

- Signup: `http://localhost:8081/v3/api-docs`
- Login: `http://localhost:8082/v3/api-docs`
- Book: `http://localhost:8083/v3/api-docs`
- Notification: `http://localhost:8084/v3/api-docs`


---



## Summary

✅ **All services have Swagger UI enabled**
✅ **Interactive API documentation available**
✅ **JWT authentication support in Swagger**
✅ **Complete request/response schemas documented**
✅ **Easy testing without Postman (though Postman collection is also provided)**

**Quick Access URLs:**
- Signup: http://localhost:8081/swagger-ui.html
- Login: http://localhost:8082/swagger-ui.html
- Book: http://localhost:8083/swagger-ui.html
- Notification: http://localhost:8084/swagger-ui.html
