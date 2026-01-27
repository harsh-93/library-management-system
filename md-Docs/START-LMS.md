# 🚀 Library Management System - Startup Guide

## Overview

This guide explains how to start the Library Management System using **two different approaches**:

1. **Approach 1:** Complete E2E Dockerized Setup (Fully Automated) ✨
2. **Approach 2:** Docker for Infrastructure + Local Services (Development Mode) 🛠️

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Approach 1: Complete E2E Dockerized Setup](#approach-1-complete-e2e-dockerized-setup)
3. [Approach 2: Docker Infrastructure + Local Services](#approach-2-docker-infrastructure--local-services)
4. [Verification Steps](#verification-steps)
5. [Troubleshooting](#troubleshooting)
6. [Stopping the Application](#stopping-the-application)

---

## Prerequisites

### **Required for Both Approaches:**

#### **1. Docker & Docker Compose**
```bash
# Check Docker version
docker --version
# Required: Docker 20.10+

# Check Docker Compose version
docker-compose --version
# Required: Docker Compose 2.0+
```



#### **2. Java (for Approach 2 only)**
```bash
# Check Java version
java -version
# Required: Java 17+
```


---

#### **3. Maven (for Approach 2 only)**
```bash
# Check Maven version
mvn -version
# Required: Maven 3.6+
```


---

---

### **Required Ports:**

Make sure these ports are available (not used by other applications):

| Port | Service | Check Command |
|------|---------|---------------|
| 8761 | Service Discovery (Eureka) | `lsof -i :8761` (Mac/Linux) or `netstat -ano \| findstr :8761` (Windows) |
| 8080 | API Gateway | `lsof -i :8080` |
| 8081 | Signup Service | `lsof -i :8081` |
| 8082 | Login Service | `lsof -i :8082` |
| 8083 | Book Service | `lsof -i :8083` |
| 8084 | Notification Service | `lsof -i :8084` |
| 3306 | MySQL | `lsof -i :3306` |
| 9092 | Kafka | `lsof -i :9092` |
| 2181 | Zookeeper | `lsof -i :2181` |



---

## Approach 1: Complete E2E Dockerized Setup

### ✨ **Best For:**
- Production-like environment
- Testing the complete system
- Minimal local setup
- Consistent environment across teams

### 📦 **What Gets Deployed:**
```
┌─────────────────────────────────────────────────┐
│  All services running in Docker containers:     │
├─────────────────────────────────────────────────┤
│  • MySQL (Database)                             │
│  • Kafka + Zookeeper (Message Broker)           │
│  • Service Discovery (Eureka)                   │
│  • API Gateway                                  │
│  • Signup Service                               │
│  • Login Service                                │
│  • Book Service                                 │
│  • Notification Service                         │
└─────────────────────────────────────────────────┘
```

---

### 🚀 **Step-by-Step Instructions:**

#### **Step 1: Navigate to Project Directory**
```bash
cd library-management-system
```

---

#### **Step 2: Make Script Executable (First Time Only)**
```bash
chmod +x run-lms-end-to-end-with-docker.sh
```

---

#### **Step 3: Run the Script**
```bash
./run-lms-end-to-end-with-docker.sh
```

---

### 📝 **What the Script Does:**

The script performs the following steps automatically:

```bash
# 1. Package Service Discovery
mvn -f service-discovery clean install package -DskipTests

# 2. Package API Gateway
mvn -f api-gateway clean install package -DskipTests

# 3. Package Signup Service
mvn -f signup-service clean install package -DskipTests

# 4. Package Login Service
mvn -f login-service clean install package -DskipTests

# 5. Package Book Service
mvn -f book-service clean install package -DskipTests

# 6. Package Notification Service
mvn -f notification-service clean install package -DskipTests

# 7. Build and start all Docker containers
docker-compose -f docker-compose-lms-end-to-end.yaml up --build

# 8. Wait for services to stabilize
sleep 10
```

**Estimated time:** 5-10 minutes (first run), 2-3 minutes (subsequent runs)

---

### 📊 **Expected Output:**

You should see output similar to:

```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Building service-discovery 2.0.0
[INFO] Building api-gateway 2.0.0
[INFO] Building signup-service 2.0.0
[INFO] Building login-service 2.0.0
[INFO] Building book-service 2.0.0
[INFO] Building notification-service 2.0.0

Creating network "library-management-system_default" with the default driver
Creating library-mysql ... done
Creating library-zookeeper ... done
Creating library-kafka ... done
Creating service-discovery ... done
Creating api-gateway ... done
Creating signup-service ... done
Creating login-service ... done
Creating book-service ... done
Creating notification-service ... done

Attaching to library-mysql, library-zookeeper, library-kafka, 
service-discovery, api-gateway, signup-service, login-service, 
book-service, notification-service
```

---

### ✅ **Verification (See full section below)**

Once all containers are running, verify the services:

```bash
# Check all containers are running
docker ps

# You should see 9 containers running
```

---

### 🛑 **Stopping the Application:**

```bash
# Stop all containers (keeps data)
docker-compose -f docker-compose-lms-end-to-end.yaml down

# Stop and remove volumes (deletes data)
docker-compose -f docker-compose-lms-end-to-end.yaml down -v
```

---

## Approach 2: Docker Infrastructure + Local Services

### 🛠️ **Best For:**
- Development & debugging
- Code changes and hot reload
- IDE integration
- Faster iteration cycle

### 📦 **What Gets Deployed:**

```
┌─────────────────────────────────────────────────┐
│  Docker Containers:                             │
├─────────────────────────────────────────────────┤
│  • MySQL (Database)                             │
│  • Kafka + Zookeeper (Message Broker)           │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│  Local Processes (running via Maven):           │
├─────────────────────────────────────────────────┤
│  • Service Discovery (Eureka)      - Port 8761  │
│  • API Gateway                     - Port 8080  │
│  • Signup Service                  - Port 8081  │
│  • Login Service                   - Port 8082  │
│  • Book Service                    - Port 8083  │
│  • Notification Service            - Port 8084  │
└─────────────────────────────────────────────────┘
```

---

### 🚀 **Step-by-Step Instructions:**

#### **Step 1: Navigate to Project Directory**
```bash
cd library-management-system
```

---

#### **Step 2: Start Infrastructure (MySQL + Kafka)**
```bash
docker-compose -f docker-compose-mysql-kafka-only.yaml up -d
```

**Verify infrastructure is running:**
```bash
docker ps

# You should see 3 containers:
# - library-mysql
# - library-kafka
# - library-zookeeper
```

**Wait for services to be ready (~30 seconds):**
```bash
# Check MySQL is ready
docker logs library-mysql | grep "ready for connections"

# Check Kafka is ready
docker logs library-kafka | grep "started"
```

---

#### **Step 3: Start Services (in separate terminals)**

You need to open **6 separate terminal windows/tabs** and run each service:

##### **Terminal 1: Service Discovery**
```bash
cd library-management-system/service-discovery
mvn spring-boot:run
```

**Wait for:** `Tomcat started on port(s): 8761`

---

##### **Terminal 2: API Gateway**
```bash
cd library-management-system/api-gateway
mvn spring-boot:run
```

**Wait for:** `Tomcat started on port(s): 8080`

---

##### **Terminal 3: Signup Service**
```bash
cd library-management-system/signup-service
mvn spring-boot:run
```

**Wait for:** `Tomcat started on port(s): 8081`

---

##### **Terminal 4: Login Service**
```bash
cd library-management-system/login-service
mvn spring-boot:run
```

**Wait for:** `Tomcat started on port(s): 8082`

---

##### **Terminal 5: Book Service**
```bash
cd library-management-system/book-service
mvn spring-boot:run
```

**Wait for:** `Tomcat started on port(s): 8083`

---

##### **Terminal 6: Notification Service**
```bash
cd library-management-system/notification-service
mvn spring-boot:run
```

**Wait for:** `Tomcat started on port(s): 8084`

---

#### **Step 4: Verify All Services Started**

Check that all services are registered in Eureka:

```bash
# Open Eureka Dashboard
open http://localhost:8761

# Or using curl
curl http://localhost:8761/eureka/apps
```

You should see all 5 services registered:
- API-GATEWAY
- SIGNUP-SERVICE
- LOGIN-SERVICE
- BOOK-SERVICE
- NOTIFICATION-SERVICE

---



#### **Or create a startup script:**

Create `start-all-services.sh`:
```bash
#!/bin/bash

echo "Starting infrastructure..."
docker-compose -f docker-compose-mysql-kafka-only.yaml up -d

echo "Waiting for infrastructure to be ready..."
sleep 30

echo "Starting services..."
cd service-discovery && mvn spring-boot:run &
sleep 15

cd ../api-gateway && mvn spring-boot:run &
sleep 10

cd ../signup-service && mvn spring-boot:run &
cd ../login-service && mvn spring-boot:run &
cd ../book-service && mvn spring-boot:run &
cd ../notification-service && mvn spring-boot:run &

wait
```

**Note:** This runs all services in background. Use Approach 1 if you want this!

---

### 🛑 **Stopping the Application:**

#### **Stop Services (in each terminal):**
Press `Ctrl+C` in each terminal running a service.

#### **Stop Infrastructure:**
```bash
docker-compose -f docker-compose-mysql-kafka-only.yaml down

# Or to remove volumes (delete data)
docker-compose -f docker-compose-mysql-kafka-only.yaml down -v
```

---

## Verification Steps

### **Step 1: Check Docker Containers (Approach 1 only)**

```bash
docker ps
```

**Expected Output:**
```
CONTAINER ID   IMAGE                          STATUS         PORTS
abc123...      service-discovery:latest       Up 2 minutes   0.0.0.0:8761->8761/tcp
def456...      api-gateway:latest             Up 2 minutes   0.0.0.0:8080->8080/tcp
ghi789...      signup-service:latest          Up 2 minutes   0.0.0.0:8081->8081/tcp
jkl012...      login-service:latest           Up 2 minutes   0.0.0.0:8082->8082/tcp
mno345...      book-service:latest            Up 2 minutes   0.0.0.0:8083->8083/tcp
pqr678...      notification-service:latest    Up 2 minutes   0.0.0.0:8084->8084/tcp
stu901...      library-mysql:latest           Up 2 minutes   0.0.0.0:3306->3306/tcp
vwx234...      library-kafka:latest           Up 2 minutes   0.0.0.0:9092->9092/tcp
yza567...      library-zookeeper:latest       Up 2 minutes   0.0.0.0:2181->2181/tcp
```

**All containers should show `Up` status!**

---

### **Step 2: Check Eureka Dashboard**

**Open in browser:**
```
http://localhost:8761
```

**Expected:**
- Eureka Server homepage loads ✅
- All 5 services registered:
    - API-GATEWAY
    - SIGNUP-SERVICE
    - LOGIN-SERVICE
    - BOOK-SERVICE
    - NOTIFICATION-SERVICE

**Screenshot:**
```
Instances currently registered with Eureka:
┌──────────────────────────────────────────┐
│ Application         AMIs    Availability │
├──────────────────────────────────────────┤
│ API-GATEWAY         1       (1) (1) UP   │
│ SIGNUP-SERVICE      1       (1) (1) UP   │
│ LOGIN-SERVICE       1       (1) (1) UP   │
│ BOOK-SERVICE        1       (1) (1) UP   │
│ NOTIFICATION-SERVICE 1      (1) (1) UP   │
└──────────────────────────────────────────┘
```

---

### **Step 3: Test API Gateway**

```bash
# Test gateway health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

---

### **Step 4: Test Service Registration**

```bash
# Register a test user
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

**Expected Response:**
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "active": true,
  "message": "User registered successfully"
}
```

✅ If you get this response, the system is working!

---


---

### **Step 6: Check Database**

```bash
# Connect to MySQL
docker exec -it library-mysql mysql -uroot -proot

# Check databases
SHOW DATABASES;

# Expected:
# - library_signup_db
# - library_book_db

# Check users table
USE library_signup_db;
SELECT * FROM users;

# You should see the test user you created
```

---

### **Step 7: Check Kafka**

```bash
# List Kafka topics
docker exec -it library-kafka kafka-topics --list \
  --bootstrap-server localhost:9092

# Expected topics:
# - book-notification-topic
# - book-notification-topic-retry-0
# - book-notification-topic-retry-1
# - book-notification-topic-retry-2
# - book-notification-topic-dlt
```

---

### **Step 8: Access Swagger Documentation**

Open these URLs in your browser:

| Service | Swagger URL |
|---------|-------------|
| Signup Service | http://localhost:8081/swagger-ui.html |
| Login Service | http://localhost:8082/swagger-ui.html |
| Book Service | http://localhost:8083/swagger-ui.html |

**Expected:** Swagger UI loads with API documentation ✅

---


### **Issue 6: Build Failures (Approach 1)**

**Symptoms:**
```
[ERROR] Failed to execute goal...
```

**Solution:**
```bash
# Clean all projects
mvn clean

# Build each service individually to identify issue
mvn -f service-discovery clean install -DskipTests
mvn -f api-gateway clean install -DskipTests
# ... etc

# Check for:
# - Missing dependencies
# - Java version mismatch
# - Compilation errors
```

---

### **Issue 7: Out of Memory**

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# Increase Docker memory (Docker Desktop Settings)
# Recommended: 8GB minimum

# Or set JVM options (Approach 2)
export MAVEN_OPTS="-Xmx2g"
mvn spring-boot:run
```

---

### **Issue 8: Slow Startup**

**Symptoms:**
- Services take >5 minutes to start
- Timeouts during registration

**Solution:**
```bash
# Increase startup timeout
# In application.yml:
eureka:
  instance:
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

# Wait longer - first startup can take 5-10 minutes
```

---

## Stopping the Application

### **Approach 1: Dockerized Setup**

```bash
# Stop containers (keeps data)
docker-compose -f docker-compose-lms-end-to-end.yaml down

# Stop and remove volumes (DELETE ALL DATA!)
docker-compose -f docker-compose-lms-end-to-end.yaml down -v

# Stop and remove images (complete cleanup)
docker-compose -f docker-compose-lms-end-to-end.yaml down -v --rmi all
```

---

### **Approach 2: Local Services**

**Stop services:**
- Press `Ctrl+C` in each terminal running a service

**Stop infrastructure:**
```bash
# Stop containers (keeps data)
docker-compose -f docker-compose-mysql-kafka-only.yaml down

# Stop and remove volumes (DELETE ALL DATA!)
docker-compose -f docker-compose-mysql-kafka-only.yaml down -v
```

---

## Quick Command Reference

### **Approach 1: E2E Dockerized**

```bash
# Start everything
./run-lms-end-to-end-with-docker.sh

# Check status
docker ps

# View logs (all services)
docker-compose -f docker-compose-lms-end-to-end.yaml logs -f

# View logs (specific service)
docker logs -f book-service

# Stop everything
docker-compose -f docker-compose-lms-end-to-end.yaml down
```

---

### **Approach 2: Local Services**

```bash
# Start infrastructure
docker-compose -f docker-compose-mysql-kafka-only.yaml up -d

# Start a service (in separate terminal)
cd <service-name>
mvn spring-boot:run

# Check service status
curl http://localhost:<port>/actuator/health

# Stop infrastructure
docker-compose -f docker-compose-mysql-kafka-only.yaml down
```

---







