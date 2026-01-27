# 🚀 Swagger UI Quick Reference

## Direct Access Links (After Starting Services)

### Service Discovery (Eureka)
- **Eureka Dashboard**: [http://localhost:8761](http://localhost:8761)
- **Port**: 8761

---

### API Gateway
- **Gateway Base URL**: [http://localhost:8080](http://localhost:8080)
- **Port**: 8080
- **Routes**:
  - `/api/signup/**` → Signup Service
  - `/api/auth/**` → Login Service
  - `/api/books/**` → Book Service
  - `/notification/**` → Notification Service

---

### Signup Service
- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- **Health Check**: [http://localhost:8081/signup/health](http://localhost:8081/signup/health)
- **Port**: 8081

---

### Login Service
- **Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)
- **Health Check**: [http://localhost:8082/auth/health](http://localhost:8082/auth/health)
- **Port**: 8082

---

### Book Service ⭐
- **Swagger UI**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8083/v3/api-docs](http://localhost:8083/v3/api-docs)
- **Health Check**: [http://localhost:8083/books/health](http://localhost:8083/books/health)
- **Port**: 8083

---

### Notification Service
- **Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8084/v3/api-docs](http://localhost:8084/v3/api-docs)
- **Health Check**: [http://localhost:8084/notification/health](http://localhost:8084/notification/health)
- **Port**: 8084

---

## 📋 Testing Workflow

**To Start the appliction See [START-LMS.md](START-LMS.md)** 

### 1️⃣ Register User
**URL**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- Navigate to `POST /api/signup/register`
- Click "Try it out"
- Fill in user details
- Execute

### 2️⃣ Login & Get JWT Token
**URL**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- Navigate to `POST /api/auth/login`
- Click "Try it out"
- Enter username/password
- Execute
- **Copy the `token` from response**

### 3️⃣ Authorize Book Service
**URL**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
- Click **"Authorize"** button (top right)
- Enter: `Bearer YOUR_JWT_TOKEN_HERE`
- Click "Authorize"
- Click "Close"

### 4️⃣ Test Book APIs
Now you can test all book endpoints:
- `POST /books` - Create book
- `GET /books` - List books (with pagination & filters)
- `GET /books/{id}` - Get specific book
- `PUT /books/{id}` - Update book
- `DELETE /books/{id}` - Delete book
- `GET /books/search` - Search books
- `POST /books/wishlist` - Add to wishlist

---

## 📱 Via API Gateway

You can also access services through the gateway:

- Signup: `http://localhost:8080/api/signup/register`
- Login: `http://localhost:8080/api/auth/login`
- Books: `http://localhost:8080/api/books`

---




## 📚 Full Documentation

See [SWAGGER-GUIDE.md](SWAGGER-GUIDE.md) for complete details.
