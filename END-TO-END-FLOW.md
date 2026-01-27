# 🎯 Library Management System - Complete End-to-End Flow

## Overview

This guide provides a complete walkthrough of the Library Management System, from user registration to receiving notifications. Follow this step-by-step guide to understand how data flows through all microservices.

---

## 📋 Table of Contents

1. [System Architecture](#system-architecture)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Flow](#step-by-step-flow)
4. [Complete API Workflow Script](#complete-api-workflow-script)
5. [Data Flow Diagrams](#data-flow-diagrams)
6. [Testing Scenarios](#testing-scenarios)
7. [Troubleshooting](#troubleshooting)

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Library Management System                         │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│   ┌──────────────┐      ┌──────────────┐      ┌──────────────┐     │
│   │   Service    │      │  API Gateway │      │    Kafka     │     │
│   │  Discovery   │◄─────┤   (Port      │◄────►│  Message     │     │
│   │  (Eureka)    │      │    8080)     │      │   Broker     │     │
│   │  Port 8761   │      └──────────────┘      └──────────────┘     │
│   └──────────────┘              │                                    │
│          │                      │                                    │
│          └──────────┬───────────┴───────────┬──────────────┐        │
│                     │                       │              │        │
│              ┌──────▼──────┐        ┌──────▼──────┐  ┌───▼──────┐ │
│              │   Signup    │        │    Login    │  │   Book   │ │
│              │   Service   │        │   Service   │  │ Service  │ │
│              │  Port 8081  │        │  Port 8082  │  │Port 8083 │ │
│              └─────────────┘        └─────────────┘  └──────────┘ │
│                     │                      │               │        │
│                     │                      │               │        │
│              ┌──────▼──────────────────────▼───────────────▼─────┐ │
│              │              MySQL Database                        │ │
│              │  - library_signup_db (users)                       │ │
│              │  - library_book_db (books, wishlists)              │ │
│              └────────────────────────────────────────────────────┘ │
│                                                                       │
│                          ┌──────────────┐                            │
│                          │Notification  │                            │
│                          │  Service     │                            │
│                          │ Port 8084    │                            │
│                          └──────────────┘                            │
└─────────────────────────────────────────────────────────────────────┘
```

### Microservices:

| Service | Port | Purpose |
|---------|------|---------|
| **Service Discovery** | 8761 | Eureka - Service registry |
| **API Gateway** | 8080 | **Single entry point for all requests** |
| **Signup Service** | 8081 | User registration |
| **Login Service** | 8082 | Authentication & JWT generation |
| **Book Service** | 8083 | Book CRUD, wishlist, Kafka publisher |
| **Notification Service** | 8084 | Kafka consumer, sends notifications |

### Infrastructure:

| Component | Port | Purpose |
|-----------|------|---------|
| **MySQL** | 3306 | Persistent data storage |
| **Kafka** | 9092 | Message broker for async notifications |
| **Zookeeper** | 2181 | Kafka coordination |

---

## Prerequisites

Before starting, ensure you have:

### Running the Application:

**To Start the appliction See [START-LMS.md](START-LMS.md)**

To SEE SWagger GUIDES and URLS of the appliction See [SWAGGER-GUIDE.md](SWAGGER-GUIDE.md) and [SWAGGER-URLs.md](SWAGGER-URLs.md)

You can find the Postman collection here -> [lms-API.postman_collection.json](lms-API.postman_collection.json)


### Verify Services are Running:

```bash
# Check Eureka Dashboard
curl http://localhost:8761

# Check all services registered
# You should see: SIGNUP-SERVICE, LOGIN-SERVICE, BOOK-SERVICE, NOTIFICATION-SERVICE, API-GATEWAY
```

---

## Step-by-Step Flow

### **Phase 1: User Registration & Authentication**

---

#### **Step 1️⃣: Register a New User**

**Endpoint:** `POST /api/signup/register`

**Via API Gateway:**
```bash
curl -X POST http://localhost:8080/api/signup/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "lms_new_user_final_with Docker-new",
  "email": "new_user_final_dockernew@spry.com",
  "password": "ProtectedPass123!#",
  "first-name": "Mohit",
  "last-name": "Yadav"
  }'
```

**What Happens Internally:**

1. Request → **API Gateway** (port 8080)
2. Gateway routes to → **Signup Service** (port 8081)
   - Path: `/api/signup/register` → forwards to `/api/signup/register`
3. Signup Service validates:
   - ✅ Username is unique
   - ✅ Email format is valid
   - ✅ Password meets requirements
4. Password hashed using **BCrypt**
5. User saved to `library_signup_db.users` table
6. Returns success response

**Expected Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true,
  "createdAt": "2026-01-26T10:30:00",
  "message": "User registered successfully"
}
```

**Data Flow:**
```
Client → API Gateway → Signup Service → MySQL (INSERT user) → ✅ User Created
```

**Database State:**
```sql
-- library_signup_db.users table
INSERT INTO users (username, email, password, first_name, last_name, active)
VALUES ('john_doe', 'john@example.com', '$2a$10$...', 'John', 'Doe', true);
```

---

#### **Step 2️⃣: Login to Get JWT Token**

**Endpoint:** `POST /api/auth/login`

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

**What Happens Internally:**

1. Request → **API Gateway** → **Login Service** (port 8082)
2. Login Service:
   - Queries `library_signup_db.users` for username
   - **Optimized:** Only 1 query (CustomUserDetails pattern)
   - Validates password using BCrypt
   - Generates JWT token:
     ```
     Header: {"alg": "HS256", "typ": "JWT"}
     Payload: {"userId": 1, "username": "john_doe", "iat": ..., "exp": ...}
     Signature: HMACSHA256(header + payload, secret)
     ```
   - Token expires in 24 hours
3. Returns JWT token and user details

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9obl9kb2UiLCJpYXQiOjE3MDY4MjAwMDAsImV4cCI6MTcwNjkwNjQwMH0.abc123xyz",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "message": "Login successful"
}
```

**Data Flow:**
```
Client → API Gateway → Login Service → MySQL (SELECT user) 
                                     → Validate Password
                                     → Generate JWT
                                     → ✅ Return Token
```

**⚠️ IMPORTANT:** Save this token! You'll need it for all subsequent requests.

**Store the token:**
```bash
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### **Phase 2: Book Management**

---

#### **Step 3️⃣: Create a Book**

**Endpoint:** `POST /api/books`

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "new Book - romeo4555",
  "author": "Mohit Kumar Yadav docker",
  "isbn": "isban_4455",
  "published-year": 2011,
  "availability-status": "AVAILABLE"
  }'
```

**What Happens Internally:**

1. Request → **API Gateway** → **Book Service** (port 8083)
2. **JWT Authentication Filter** (runs before controller):
   - Extracts token from `Authorization: Bearer <token>` header
   - Validates token signature using secret key
   - Checks token expiration
   - Extracts username from token
   - Sets Spring Security context
   - ✅ Proceeds to controller if valid
   - ❌ Returns 401 if invalid/expired
3. Book Controller → Book Service:
   - Checks ISBN uniqueness: `SELECT COUNT(*) FROM books WHERE isbn = ? AND deleted = false`
   - ❌ Returns 409 Conflict if ISBN exists
   - ✅ Creates new book if unique
4. Saves to `library_book_db.books` table
5. Sets timestamps: `createdAt` and `updatedAt`
6. Returns created book

**Expected Response:**
```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "publishedYear": 2008,
  "availabilityStatus": "AVAILABLE",
  "deleted": false,
  "createdAt": "2026-01-26T10:35:00",
  "updatedAt": "2026-01-26T10:35:00"
}
```

**Data Flow:**
```
Client → API Gateway → Book Service → JWT Validation ✅
                                   → Check ISBN Uniqueness
                                   → MySQL (INSERT book)
                                   → ✅ Book Created
```

**Security:**
- ✅ JWT token required
- ✅ Token validated before processing
- ✅ Only authenticated users can create books

---

#### **Step 4️⃣: Get All Books (with Pagination & Filtering)**

**Endpoint:** `GET /api/books`

**Basic Request:**
```bash
curl -X GET http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN"
```

**Advanced Requests:**

```bash
# Pagination
curl -X GET "http://localhost:8080/api/books?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Filter by author
curl -X GET "http://localhost:8080/api/books?author=Martin" \
  -H "Authorization: Bearer $TOKEN"

# Filter by availability
curl -X GET "http://localhost:8080/api/books?availabilityStatus=AVAILABLE" \
  -H "Authorization: Bearer $TOKEN"

# Combined filters
curl -X GET "http://localhost:8080/api/books?author=Martin&availabilityStatus=AVAILABLE&page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"

# Sorting
curl -X GET "http://localhost:8080/api/books?sort=title,asc" \
  -H "Authorization: Bearer $TOKEN"
```

**What Happens Internally:**

1. Request → **API Gateway** → **Book Service**
2. JWT validated
3. **JPA Specifications** build dynamic query:
   ```java
   Specification<Book> spec = Specification.where(null);
   if (author != null) spec = spec.and(authorContains(author));
   if (status != null) spec = spec.and(statusEquals(status));
   spec = spec.and(notDeleted());  // Always exclude soft-deleted books
   ```
4. Query executed:
   ```sql
   SELECT * FROM books 
   WHERE deleted = false 
     AND author LIKE '%Martin%' 
     AND availability_status = 'AVAILABLE'
   ORDER BY title ASC
   LIMIT 10 OFFSET 0;
   ```
5. Returns paginated results

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "9780132350884",
      "publishedYear": 2008,
      "availabilityStatus": "AVAILABLE",
      "deleted": false,
      "createdAt": "2026-01-26T10:35:00",
      "updatedAt": "2026-01-26T10:35:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true
}
```

**Features:**
- ✅ Dynamic filtering (any combination of fields)
- ✅ Pagination (page, size)
- ✅ Sorting (field, direction)
- ✅ Soft deletes (only active books returned)

---

### **Phase 3: Wishlist & Notifications** 

---

#### **Step 5️⃣: Add Book to Wishlist**

**Endpoint:** `POST /api/books/wishlist`

```bash
curl -X POST http://localhost:8080/api/books/wishlist \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "bookId": 1
  }'
```

**What Happens Internally:**

1. Request → **API Gateway** → **Book Service**
2. JWT validated
3. **Security Feature:**
   - Username extracted from JWT token (e.g., "john_doe")
   - Query: `SELECT * FROM users WHERE username = 'john_doe'`
   - Get userId from database (e.g., userId = 1)
   - ✅ **Important:** userId NOT accepted from request body!
   - ❌ Prevents users from adding to other users' wishlists
4. Validates book exists:
   - Query: `SELECT * FROM books WHERE id = 1 AND deleted = false`
   - ❌ Returns 404 if not found
5. Checks if already in wishlist:
   - Query: `SELECT COUNT(*) FROM wishlists WHERE user_id = 1 AND book_id = 1`
   - Silently returns if already exists (idempotent)
6. Inserts into wishlist:
   - Query: `INSERT INTO wishlists (user_id, book_id, created_at) VALUES (1, 1, NOW())`

**Expected Response:**
```json
HTTP 200 OK
```

**Data Flow:**
```
Client → API Gateway → Book Service → JWT Validation ✅
                                   → Extract Username from JWT
                                   → MySQL (SELECT user by username)
                                   → Get User ID (Secure! 🔒)
                                   → Validate Book Exists
                                   → Check if Already in Wishlist
                                   → MySQL (INSERT into wishlists)
                                   → ✅ Wishlist Entry Created
```

**Security Highlight:**
```bash
# OLD (Insecure - what we DON'T do):
{
  "userId": 2,    # User could specify ANY userId!
  "bookId": 1
}

# NEW (Secure - what we DO):
{
  "bookId": 1     # userId extracted from JWT ✅
}
```

**Database State:**
```sql
-- library_book_db.wishlists table
INSERT INTO wishlists (user_id, book_id, created_at)
VALUES (1, 1, '2026-01-26 10:40:00');
```

---

#### **Step 6️⃣: Update Book Status (Triggers Notification) 🚀**

**This is where the magic happens!**

**Scenario:** Someone borrowed the book. Now they're returning it.

**Endpoint:** `PUT /api/books/{id}`

```bash
# Book was BORROWED, now returning it to AVAILABLE
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "publishedYear": 2008,
    "availabilityStatus": "AVAILABLE"
  }'
```

**What Happens Internally (The Full Journey):**

1. Request → **API Gateway** → **Book Service**
2. JWT validated
3. Book Service updates book:
   ```java
   // Get existing book
   Book existingBook = findById(1);  // old status: BORROWED
   
   // Update fields
   existingBook.setAvailabilityStatus(AVAILABLE);  // new status
   
   // Detect status change
   boolean statusChanged = !oldStatus.equals(newStatus);
   boolean nowAvailable = newStatus == AVAILABLE;
   
   if (statusChanged && nowAvailable) {
       // 🔔 TRIGGER NOTIFICATIONS!
       notifyWishlistUsers(bookId);
   }
   ```

4. Query wishlist:
   ```sql
   SELECT user_id FROM wishlists 
   WHERE book_id = 1;
   
   -- Results: [1, 5, 12]  (3 users have this book in wishlist)
   ```

5. **For each userId in wishlist:**
   ```java
   BookNotificationEvent event = BookNotificationEvent.builder()
       .bookId(1)
       .bookTitle("Clean Code")
       .userId(1)  // First user
       .eventType(EventType.BOOK_AVAILABLE)
       .message("Book 'Clean Code' is now available")
       .build();
   
   // Publish to Kafka
   kafkaTemplate.send("book-notification-topic", event);
   ```

6. **Kafka Producer:**
   - Serializes event to JSON
   - Publishes to `book-notification-topic`
   - **Async:** Doesn't block the response
   - Returns immediately

**Expected Response:**
```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "publishedYear": 2008,
  "availabilityStatus": "AVAILABLE",
  "deleted": false,
  "updatedAt": "2026-01-26T11:00:00"
}
```

**Data Flow:**
```
Client → API Gateway → Book Service → JWT Validation ✅
                                   → MySQL (UPDATE book)
                                   → Detect Status Change: BORROWED → AVAILABLE ✅
                                   → MySQL (SELECT from wishlists)
                                   → For Each User in Wishlist:
                                       Create BookNotificationEvent
                                   → Kafka Producer
                                   → Publish to 'book-notification-topic' ✅
                                   → Return Updated Book (async, doesn't wait)
```

**Kafka Message (JSON):**
```json
{
  "bookId": 1,
  "bookTitle": "Clean Code",
  "userId": 1,
  "eventType": "BOOK_AVAILABLE",
  "message": "Book 'Clean Code' is now available",
  "timestamp": "2026-01-26T11:00:00"
}
```

**Important Notes:**
- ✅ Notifications sent only when status changes to AVAILABLE
- ✅ Only users with book in wishlist are notified
- ✅ Async processing (doesn't slow down the API response)
- ✅ Multiple users can be notified for same book

---

#### **Step 7️⃣: Notification Service Consumes Message (Background Process)**

**This happens automatically in the background!**

**What Happens Internally:**

1. **Kafka Consumer** polls from `book-notification-topic`
   ```java
   @KafkaListener(topics = "book-notification-topic")
   public void consume(BookNotificationEvent event) {
       // Process notification
   }
   ```

2. **Message Deserialization:**
   - JSON → `BookNotificationEvent` object
   - Uses `JsonDeserializer`

3. **@RetryableTopic Mechanism Active:**
   ```
   Attempt 1: Process message
       ├─→ Success? → Commit offset ✅ (done!)
       └─→ Failure? → Publish to retry-0 topic
                       Wait 2 seconds...
   
   Attempt 2: Process from retry-0
       ├─→ Success? → Commit offset ✅
       └─→ Failure? → Publish to retry-1 topic
                       Wait 4 seconds...
   
   Attempt 3: Process from retry-1
       ├─→ Success? → Commit offset ✅
       └─→ Failure? → Publish to retry-2 topic
                       Wait 8 seconds...
   
   Attempt 4: Process from retry-2
       ├─→ Success? → Commit offset ✅
       └─→ Failure? → Publish to DLT topic
                       @DltHandler called
                       Manual intervention required ⚠️
   ```

4. **Process Notification:**
   ```java
   public void processNotification(BookNotificationEvent event) {
       log.info("Notification prepared for user_id: {}", event.getUserId());
       log.info("Book [{}] is now available", event.getBookTitle());
       
       // In real implementation:
       // - Send email via SendGrid/AWS SES
       // - Send SMS via Twilio
       // - Send push notification via Firebase
       // - Store in notification database
       
       // For now: log to console (simulated)
   }
   ```

5. **Commit Offset:**
   - After successful processing
   - ACK mode: `record`
   - Spring manages automatically

**Console Output:**
```
┌─────────────────────────────────────────────────────────────
│ Consuming message from Kafka
│ Topic: book-notification-topic, Partition: 0, Offset: 5
│ Event: BookNotificationEvent(bookId=1, userId=1, ...)
└─────────────────────────────────────────────────────────────
==============================================
NOTIFICATION PREPARED
==============================================
Notification prepared for user_id: 1
Book [Clean Code] is now available
Event Type: BOOK_AVAILABLE
Message: Book 'Clean Code' is now available
==============================================
Notification successfully sent to user: 1
✅ Successfully processed notification (offset: 5)
```

**Data Flow:**
```
Kafka Topic → Notification Service → Deserialize JSON
                                  → Process Notification
                                  → Send Email/SMS (Simulated)
                                  → Log to Console
                                  → Commit Offset ✅
                                  → User Notified!
```

**Retry Flow (If Processing Fails):**
```
Attempt 1: book-notification-topic (offset: 5) → FAIL
              ↓ (2 seconds delay)
Attempt 2: book-notification-topic-retry-0 → FAIL
              ↓ (4 seconds delay)
Attempt 3: book-notification-topic-retry-1 → FAIL
              ↓ (8 seconds delay)
Attempt 4: book-notification-topic-retry-2 → FAIL
              ↓
         book-notification-topic-dlt (Dead Letter Topic)
              ↓
         @DltHandler called:
         
╔═════════════════════════════════════════
║ MESSAGE SENT TO DEAD LETTER QUEUE
║ All retry attempts exhausted!
╠═════════════════════════════════════════
║ Event: BookNotificationEvent(...)
║ Error: Connection timeout
╚═════════════════════════════════════════
⚠️ Manual intervention required
```

---


---

## Data Flow Diagrams

### **High-Level Architecture Flow**

```
┌──────────┐     ┌─────────┐     ┌────────┐     ┌────────┐     ┌──────────┐
│  Client  │────→│ Gateway │────→│ Signup │────→│ MySQL  │     │  Kafka   │
│          │     │ :8080   │     │ :8081  │     │        │     │          │
└──────────┘     └─────────┘     └────────┘     └────────┘     └──────────┘
      │               │               │              │               │
      │               │          ┌────▼────┐         │               │
      │               │          │  Login  │         │               │
      │               │          │  :8082  │         │               │
      │               │          └────┬────┘         │               │
      │               │               │              │               │
      │               │               │ Generate JWT │               │
      │               │               │◄─────────────┘               │
      │               │               │                              │
      │◄──────────────┴───────────────┤ Return Token                │
      │  JWT Token                    │                              │
      │                               │                              │
      ├───────────────────────────────┤ Create Book (with JWT)       │
      │               │          ┌────▼────┐                         │
      │               │          │  Book   │                         │
      │               │          │ Service │                         │
      │               │          │ :8083   │                         │
      │               │          └────┬────┘                         │
      │               │               │ Save Book                    │
      │               │               ├──────────────►MySQL          │
      │               │               │                              │
      │               │               │ Update Book (Status Change)  │
      │               │               ├──────────────►MySQL          │
      │               │               │                              │
      │               │               │ Publish Event                │
      │               │               ├──────────────────────────────►Kafka
      │               │               │                                 │
      │               │               │                                 │
      │               │          ┌────▼────────┐                       │
      │               │          │Notification │◄──────────────────────┘
      │               │          │  Service    │ Consume Event
      │               │          │  :8084      │
      │               │          └─────────────┘
      │               │                 │
      │               │                 │ Send Notification
      │               │                 ▼
      │               │          ✉️  Email/SMS/Push
```

### **Detailed Book Update → Notification Flow**

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1. Client Updates Book Status                                        │
│    PUT /api/books/1                                                   │
│    { "availabilityStatus": "AVAILABLE" }                              │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2. API Gateway Routes Request                                         │
│    → Book Service :8083                                               │
│    Path transformation: /api/books/1 → /books/1                       │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3. JWT Authentication Filter                                          │
│    ✅ Extract token from Authorization header                         │
│    ✅ Validate JWT signature                                          │
│    ✅ Check expiration                                                │
│    ✅ Extract username: "alice"                                       │
│    ✅ Set SecurityContextHolder                                       │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4. Book Controller                                                    │
│    @PutMapping("/{id}")                                               │
│    public ResponseEntity<Book> updateBook(                            │
│        @PathVariable Long id,                                         │
│        @RequestBody UpdateBookRequest request)                        │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 5. Book Service - Update Logic                                        │
│                                                                        │
│    a) Find existing book:                                             │
│       SELECT * FROM books WHERE id = 1 AND deleted = false            │
│       Result: {id: 1, status: "BORROWED", ...}                        │
│                                                                        │
│    b) Check ISBN uniqueness (excluding current book):                 │
│       SELECT COUNT(*) FROM books                                      │
│       WHERE isbn = '9780135957059'                                    │
│         AND id != 1 AND deleted = false                               │
│       Result: 0 (unique ✅)                                           │
│                                                                        │
│    c) Detect status change:                                           │
│       oldStatus = "BORROWED"                                          │
│       newStatus = "AVAILABLE"                                         │
│       statusChanged = true ✅                                         │
│       nowAvailable = true ✅                                          │
│                                                                        │
│    d) Update book:                                                    │
│       UPDATE books                                                    │
│       SET availability_status = 'AVAILABLE',                          │
│           updated_at = NOW()                                          │
│       WHERE id = 1                                                    │
│                                                                        │
│    e) Trigger notifications (status changed to AVAILABLE):            │
│       notifyWishlistUsers(bookId=1)                                   │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 6. Wishlist Query & Event Creation                                    │
│                                                                        │
│    a) Query wishlist:                                                 │
│       SELECT user_id FROM wishlists WHERE book_id = 1                 │
│       Result: [1, 5, 12] (3 users)                                    │
│                                                                        │
│    b) For each userId, create event:                                  │
│       BookNotificationEvent {                                         │
│         bookId: 1,                                                    │
│         bookTitle: "The Pragmatic Programmer",                        │
│         userId: 1,                                                    │
│         eventType: BOOK_AVAILABLE,                                    │
│         message: "Book ... is now available"                          │
│       }                                                               │
│                                                                        │
│    c) Publish to Kafka (async):                                       │
│       kafkaTemplate.send("book-notification-topic", event)            │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 7. Kafka Producer (Book Service)                                      │
│    - Serialize to JSON using JsonSerializer                           │
│    - Publish to topic: "book-notification-topic"                      │
│    - Partition: determined by key (or round-robin)                    │
│    - Does NOT wait for consumer (async)                               │
│    - Returns immediately                                              │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 8. Kafka Broker                                                       │
│    - Store message in partition                                       │
│    - Offset: 5 (example)                                              │
│    - Message persisted to disk                                        │
│    - Available for consumption                                        │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 9. Notification Service - Kafka Consumer                              │
│                                                                        │
│    @RetryableTopic(attempts = 4)                                      │
│    @KafkaListener(topics = "book-notification-topic")                 │
│    public void consume(BookNotificationEvent event) {                 │
│                                                                        │
│        a) Poll from Kafka                                             │
│        b) Deserialize JSON → BookNotificationEvent                    │
│        c) Process notification:                                       │
│           - Log event details                                         │
│           - Send email (simulated)                                    │
│           - Send SMS (simulated)                                      │
│        d) If success → Commit offset                                  │
│        e) If failure → Retry with backoff                             │
│    }                                                                  │
└──────────────────────┬────────────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 10. User Notified! ✅                                                 │
│     "Book 'The Pragmatic Programmer' is now available"                │
│                                                                        │
│     Notification sent via:                                            │
│     - Email (simulated)                                               │
│     - SMS (simulated)                                                 │
│     - Push notification (simulated)                                   │
│     - Console log (actual)                                            │
└───────────────────────────────────────────────────────────────────────┘
```

---

## Testing Scenarios

### **Scenario 1: Happy Path ✅**

**Description:** Everything works perfectly

**Steps:**
1. Register user
2. Login
3. Create book
4. Add to wishlist
5. Update book status → AVAILABLE
6. Notification sent

**Expected Result:** ✅ User gets notification

---



## Summary

### **Complete Data Flow:**

```
1. User Registration  → Signup Service → MySQL (users)
2. User Login         → Login Service → Generate JWT
3. Create Book        → JWT Validation → Book Service → MySQL (books)
4. Add to Wishlist    → JWT → Extract User ID → MySQL (wishlists)
5. Update Book Status → Detect Change → Query Wishlist → Publish to Kafka
6. Consume Message    → Deserialize → Process → Commit Offset
7. User Notified      ✅
```

### **Key Features:**

| Feature | Implementation |
|---------|----------------|
| **Security** | JWT authentication on all protected endpoints |
| **Data Integrity** | ISBN uniqueness, soft deletes, transaction management |
| **Scalability** | Async notifications via Kafka, microservices architecture |
| **Reliability** | Retry mechanism with exponential backoff, DLQ for failures |
| **Performance** | Single query for auth, pagination, JPA Specifications |
| **User Privacy** | userId from JWT (not request body) |
| **Observability** | Detailed logging, offset tracking, DLT monitoring |

### **Technologies:**

- **Backend:** Spring Boot 3.1.5, Spring Cloud 2022.0.4
- **Database:** MySQL 8.0
- **Message Broker:** Apache Kafka
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Authentication:** JWT with BCrypt
- **Documentation:** Swagger/OpenAPI 3.0
- **Containerization:** Docker, Docker Compose

---

## Next Steps


2. **Monitor services:**
   ```bash
   # Eureka dashboard
   open http://localhost:8761
   
   # Swagger UI
   open http://localhost:8081/swagger-ui.html  # Signup
   open http://localhost:8082/swagger-ui.html  # Login
   open http://localhost:8083/swagger-ui.html  # Books
   ```

3. **Check logs:**
   ```bash
   # All services
   docker compose logs -f
   
   # Specific service
   docker compose logs -f notification-service
   ```

4. **Experiment:**
   - Create multiple users
   - Add same book to multiple wishlists
   - Simulate notification failures
   - Test retry mechanism

---
