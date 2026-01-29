# 🎯 Library Management System - Interview Preparation Guide

## Overview

This guide covers all potential questions for your coding assessment round on the Library Management System, organized by topic area.

---

## 📋 Table of Contents

1. [System Architecture & Design](#system-architecture--design)
2. [Microservices](#microservices)
3. [Spring Boot & Spring Cloud](#spring-boot--spring-cloud)
4. [Database Design & JPA](#database-design--jpa)
5. [Kafka & Messaging](#kafka--messaging)
6. [Security & JWT](#security--jwt)
7. [Resilience & Fault Tolerance](#resilience--fault-tolerance)
8. [Docker & Deployment](#docker--deployment)
9. [API Design & REST](#api-design--rest)
10. [Performance & Optimization](#performance--optimization)
11. [Error Handling & Logging](#error-handling--logging)
12. [Testing & Quality](#testing--quality)
13. [Scenario-Based Questions](#scenario-based-questions)

---

## System Architecture & Design

### **Q1: Explain the overall architecture of your Library Management System.**

**Answer:**
- Microservices architecture with 6 services (Service Discovery, API Gateway, Signup, Login, Book, Notification)
- Event-driven communication using Kafka
- Service discovery with Eureka
- Centralized API Gateway for routing
- MySQL databases (library_signup_db, library_book_db)
- Containerized with Docker

---

### **Q2: Why did you choose microservices over monolithic architecture?**

**Answer:**
- **Independent deployment**: Services can be deployed separately
- **Scalability**: Scale notification service independently during high traffic
- **Technology flexibility**: Can use different tech stacks per service
- **Fault isolation**: One service failure doesn't bring down entire system
- **Team autonomy**: Different teams can own different services

---

### **Q3: What are the disadvantages of your microservices approach?**

**Answer:**
- **Complexity**: More moving parts to manage
- **Network latency**: Inter-service communication overhead
- **Distributed transactions**: Harder to maintain data consistency
- **Debugging**: Tracking requests across services is complex
- **Infrastructure overhead**: More resources needed (6 services vs 1 monolith)

---

### **Q4: How do services communicate with each other?**

**Answer:**
- **Synchronous**: REST APIs through API Gateway (Service-to-service via Eureka)
- **Asynchronous**: Kafka for event-driven communication (book availability notifications)
- **Service Discovery**: Eureka for dynamic service location

---

### **Q5: What happens if one service goes down?**

**Answer:**
- **Circuit breaker** (Resilience4j) prevents cascading failures
- **Fallback methods** provide degraded functionality
- **Eureka** marks service as DOWN, Gateway routes to healthy instances
- **Kafka retry mechanism** ensures message delivery when notification service recovers
- **Service isolation** prevents failure propagation

---

## Microservices

### **Q6: Why did you separate Signup and Login into different services?**

**Answer:**
- **Single Responsibility**: Signup handles user registration, Login handles authentication
- **Independent scaling**: Login might need more instances during peak hours
- **Security separation**: JWT generation isolated from user creation
- **Different databases**: Can evolve independently
- Though could be argued they should be one "User Service" - trade-off decision

---

### **Q7: Explain the role of each microservice.**

**Answer:**
- **Service Discovery (Eureka)**: Service registration and discovery
- **API Gateway**: Single entry point, routing, load balancing
- **Signup Service**: User registration, stores in library_signup_db
- **Login Service**: Authentication, JWT token generation
- **Book Service**: Book CRUD, wishlist, Kafka message publishing
- **Notification Service**: Kafka consumer, sends notifications to users

---

### **Q8: How does API Gateway route requests to services?**

**Answer:**
- Gateway uses path-based routing configured in application.yml
- `/api/signup/**` → signup-service (strips `/api` prefix)
- `/api/auth/**` → login-service
- `/api/books/**` → book-service
- Uses Eureka for service discovery (no hardcoded URLs)
- Client-side load balancing if multiple instances exist

---

### **Q9: What if API Gateway goes down?**

**Answer:**
- **Single point of failure** - critical issue
- **Solution options**:
  - Run multiple Gateway instances behind a load balancer
  - Use Kubernetes with auto-scaling
  - Health checks and auto-restart
  - Circuit breaker for Gateway dependencies
- Currently: Not fully addressed (improvement area)

---

### **Q10: How do you handle distributed transactions?**

**Answer:**
- **Eventual consistency** approach using Kafka
- Example: Book status update → Kafka event → Notification sent
- **Saga pattern** (implicit): Each service maintains its own database
- **@Transactional** within service boundaries
- **No 2-phase commit** due to microservices nature
- **Retry mechanism** ensures eventual consistency

---

## Spring Boot & Spring Cloud

### **Q11: What Spring Boot features did you use?**

**Answer:**
- **Spring Data JPA**: Database operations
- **Spring Security**: JWT authentication
- **Spring Kafka**: Message production/consumption
- **Spring Cloud Netflix Eureka**: Service discovery
- **Spring Cloud Gateway**: API Gateway
- **Spring Validation**: Request validation
- **Spring Boot Actuator**: Health checks (if enabled)

---

### **Q12: Explain how Eureka service discovery works.**

**Answer:**
1. **Registration**: Services register with Eureka on startup
2. **Heartbeat**: Services send heartbeat every 30 seconds
3. **Discovery**: Clients fetch service registry from Eureka
4. **Load balancing**: Client-side load balancing using Ribbon
5. **Health checks**: Eureka marks unhealthy services as DOWN
6. **De-registration**: Services unregister on shutdown or after missed heartbeats

---

### **Q13: What is the difference between @Component, @Service, and @Repository?**

**Answer:**
- **@Component**: Generic stereotype for any Spring-managed bean
- **@Service**: Semantic annotation for business logic layer
- **@Repository**: Data access layer, enables exception translation
- All are functionally the same, but provide semantic meaning
- **Used in project**: @Service for business logic, @Repository for data access

---

### **Q14: Explain @Transactional and where you used it.**

**Answer:**
- Manages database transactions automatically
- **ACID properties**: Ensures atomicity, consistency, isolation, durability
- **Used in**:
  - BookService.createBook() - ensures book creation is atomic
  - BookService.updateBook() - rollback if notification fails (wait, no - it's async!)
  - WishlistService operations
- **Read-only transactions**: @Transactional(readOnly = true) for queries - optimization
- **Propagation**: Default is REQUIRED (joins existing or creates new)

---

### **Q15: What is the purpose of application.yml vs application.properties?**

**Answer:**
- Both configure Spring Boot applications
- **YAML advantages**: Hierarchical, more readable, less repetitive
- **Used in project**: application.yml for better structure
- **Environment-specific**: Can have application-dev.yml, application-prod.yml
- **Externalization**: Can override with environment variables

---

### **Q16: How do you handle different environments (dev, staging, prod)?**

**Answer:**
- **Spring Profiles**: application-dev.yml, application-prod.yml
- **Environment variables**: Override properties via ENV vars in Docker
- **In project**:
  - `${DB_HOST:localhost}` - uses localhost if DB_HOST not set
  - Docker Compose sets environment variables
  - Can activate profiles with `spring.profiles.active=prod`

---

### **Q17: Explain dependency injection in your project.**

**Answer:**
- **Constructor injection** (recommended): `@RequiredArgsConstructor` with Lombok
- Example: BookService injects BookRepository, WishlistRepository, KafkaProducer
- **Benefits**: Immutability, easier testing, clear dependencies
- **Spring manages lifecycle**: Creates beans, injects dependencies
- **No @Autowired needed**: Constructor injection with Lombok

---

## Database Design & JPA

### **Q18: Explain your database schema design.**

**Answer:**
- **Two databases**: library_signup_db, library_book_db (database per service)
- **Users table**: username, email (unique), password (BCrypt), timestamps
- **Books table**: ISBN (unique), soft delete flag, availability status
- **Wishlists table**: Composite unique key (user_id, book_id), prevents duplicates
- **Normalization**: 3rd Normal Form
- **Indexes**: Added for performance (username, ISBN, book_id in wishlist)

---

### **Q19: Why did you use soft delete instead of hard delete?**

**Answer:**
- **Data recovery**: Can restore accidentally deleted books
- **Audit trail**: Maintain history of deleted items
- **Referential integrity**: Avoid orphaned wishlist entries
- **Analytics**: Can analyze deleted books
- **Implementation**: `deleted` boolean flag + `deleted_at` timestamp
- **Query impact**: All queries filter `WHERE deleted = false`

---

### **Q20: What is the N+1 query problem and how did you avoid it?**

**Answer:**
- **N+1 Problem**: Fetching N items, then N additional queries for related data
- **In project**: Initially NO N+1 issue because:
  - Wishlist uses primitive Long (user_id, book_id) not entities
  - No lazy loading relationships in notification query
- **If using @ManyToOne**: Would use `@EntityGraph` or JOIN FETCH
- **Prevention**: Pagination, fetch joins, batch loading

---

### **Q21: Explain JPA Specifications and why you used them.**

**Answer:**
- **Dynamic query building** without writing JPQL for every combination
- **In Book search**: Filter by author, year, status - any combination
- **Example**: `WHERE deleted = false AND author LIKE ? AND status = ?`
- **Benefits**:
  - Type-safe queries
  - Reusable criteria
  - Composable (combine multiple specs)
  - No SQL injection risk

---

### **Q22: What is the difference between CrudRepository and JpaRepository?**

**Answer:**
- **CrudRepository**: Basic CRUD operations (save, findById, delete)
- **JpaRepository**: Extends CrudRepository + PagingAndSortingRepository
- **Additional features in JPA**:
  - Batch operations (deleteAllInBatch)
  - Flush operations
  - Pagination and sorting built-in
- **Used in project**: JpaRepository for pagination support

---

### **Q23: How do you handle database migrations?**

**Answer:**
- **Currently**: Hibernate's `ddl-auto: update` creates/updates schema automatically
- **Production issue**: Not recommended for prod (can cause data loss)
- **Better approach**: Use Flyway or Liquibase
  - Version-controlled migrations
  - Rollback support
  - Audit trail of schema changes
- **Improvement area**: Add Flyway for production

---

### **Q24: Explain your indexing strategy.**

**Answer:**
- **Created indexes on**:
  - users.username (login queries)
  - books.isbn (uniqueness check)
  - books.author (search)
  - books.availability_status (filtering)
  - wishlists.book_id (notification queries - CRITICAL!)
- **Composite indexes**:
  - books(deleted, availability_status) - most common query
- **Performance impact**: 10-100x faster queries with indexes

---

### **Q25: What is the difference between @OneToMany and @ManyToOne?**

**Answer:**
- **@OneToMany**: One parent has many children (User → Books)
- **@ManyToOne**: Many children reference one parent (Books → User)
- **Bidirectional**: Can navigate both ways
- **In project**: Avoided relationships in Wishlist (used primitive Long)
- **Trade-off**: Simpler queries vs object-oriented design

---

## Kafka & Messaging

### **Q26: Why did you use Kafka for notifications?**

**Answer:**
- **Asynchronous processing**: Book update doesn't wait for notifications
- **Scalability**: Can add more notification consumers
- **Reliability**: Messages persisted, guaranteed delivery
- **Decoupling**: Book service doesn't know about notification logic
- **Multiple consumers**: Can add email, SMS, push notification services

---

### **Q27: Explain the flow when a book becomes available.**

**Answer:**
1. Book status updated: BORROWED → AVAILABLE
2. BookService queries wishlist: `SELECT user_id FROM wishlists WHERE book_id = ?`
3. For each user, create BookNotificationEvent
4. Publish to Kafka topic 'book-notification-topic'
5. Notification service consumes message
6. Process notification (log, send email, etc.)
7. Commit offset on success

---

### **Q28: What is @RetryableTopic and how does it work?**

**Answer:**
- **Automatic retry mechanism** for failed Kafka messages
- **Exponential backoff**: 2s, 4s, 8s delays
- **Retry topics**: Separate topics for each retry attempt
- **Dead Letter Queue (DLT)**: After 4 failed attempts
- **In project**:
  - Attempt 1: book-notification-topic
  - Attempt 2: book-notification-topic-retry-0 (2s delay)
  - Attempt 3: book-notification-topic-retry-1 (4s delay)
  - Attempt 4: book-notification-topic-retry-2 (8s delay)
  - Failed: book-notification-topic-dlt → @DltHandler

---

### **Q29: What happens if Kafka goes down?**

**Answer:**
- **Producer side** (Book Service):
  - Message buffered in memory (if configured)
  - Eventually throws exception if Kafka unavailable
  - Circuit breaker (Resilience4j) can prevent retries
- **Consumer side** (Notification Service):
  - Stops consuming, waits for Kafka to recover
  - On recovery, resumes from last committed offset
  - No message loss due to Kafka persistence

---

### **Q30: Explain Kafka consumer groups.**

**Answer:**
- **Consumer group**: Multiple consumers reading from same topic
- **In project**: notification-group with 1 consumer
- **Partition assignment**: Kafka assigns partitions to consumers
- **Scaling**: Add more consumers to process messages faster
- **Guarantee**: Each message consumed by only one consumer in group
- **Rebalancing**: On consumer join/leave, partitions redistributed

---

### **Q31: What is offset management in Kafka?**

**Answer:**
- **Offset**: Position of consumer in partition
- **Commit strategy**:
  - Manual: `ack.acknowledge()` after processing
  - Automatic: Spring commits after method returns
- **In project**: `ack-mode: record` (auto-commit after successful processing)
- **At-least-once delivery**: Message reprocessed on failure
- **Idempotency needed**: Handle duplicate messages gracefully

---

### **Q32: How do you ensure message ordering in Kafka?**

**Answer:**
- **Partition-level ordering**: Messages in same partition are ordered
- **Key-based partitioning**: Messages with same key go to same partition
- **In project**: Not explicitly handled (could use book_id as key)
- **Trade-off**: Ordering vs parallelism
- **Single partition**: Guaranteed order but no parallelism

---

### **Q33: What is the difference between Kafka and RabbitMQ?**

**Answer:**
- **Kafka**: Distributed commit log, high throughput, persistence
- **RabbitMQ**: Traditional message broker, lower latency
- **Kafka advantages**:
  - Better for event streaming
  - Replay messages (offset management)
  - Higher throughput
- **RabbitMQ advantages**:
  - More flexible routing
  - Better for complex workflows
- **Chose Kafka**: Better for event-driven architecture, scalability

---

## Security & JWT

### **Q34: Explain how JWT authentication works in your system.**

**Answer:**
1. User logs in with username/password
2. Login service validates credentials
3. Generates JWT token with username, userId, expiration
4. Returns token to client
5. Client includes token in Authorization header
6. JwtAuthenticationFilter validates token on each request
7. Sets SecurityContext for authorization

---

### **Q35: What information is stored in the JWT token?**

**Answer:**
- **Header**: Algorithm (HS256), token type (JWT)
- **Payload**:
  - userId
  - username
  - iat (issued at)
  - exp (expiration - 24 hours)
- **Signature**: HMAC SHA256 with secret key
- **Not storing**: Password, sensitive data, roles (should add)

---

### **Q36: How do you prevent JWT token tampering?**

**Answer:**
- **Signature verification**: Token signed with secret key
- **In JwtAuthenticationFilter**:
  - Parse token
  - Verify signature using same secret
  - If tampered, signature won't match
  - Throw exception, return 401
- **Secret key management**: Same secret across login and book services

---

### **Q37: What happens when JWT token expires?**

**Answer:**
- **Token lifetime**: 24 hours
- **On expiration**:
  - JwtAuthenticationFilter detects expired token
  - Throws JwtException
  - Returns 401 Unauthorized
- **Client action**: Must login again to get new token
- **No refresh token**: Could implement for better UX

---

### **Q38: How do you secure the JWT secret key?**

**Answer:**
- **Current**: In application.yml (NOT SECURE for production!)
- **Production approaches**:
  - Environment variables
  - Vault (HashiCorp Vault)
  - AWS Secrets Manager
  - Kubernetes Secrets
- **Recommendation**: Rotate keys periodically
- **Improvement area**: Use external secret management

---

### **Q39: Why use BCrypt for password hashing?**

**Answer:**
- **Salted hashing**: Each password has unique salt
- **Adaptive**: Can increase cost factor over time
- **Slow by design**: Prevents brute-force attacks
- **One-way**: Cannot reverse engineer original password
- **Industry standard**: Recommended by OWASP

---

### **Q40: How would you implement role-based access control (RBAC)?**

**Answer:**
- **Add roles table**: users_roles, roles (USER, LIBRARIAN, ADMIN)
- **Store in JWT**: Include roles in token payload
- **Spring Security**: Use @PreAuthorize("hasRole('ADMIN')")
- **Example**:
  - USER: Read, wishlist
  - LIBRARIAN: Create, update books
  - ADMIN: Delete books, manage users
- **Not currently implemented** - improvement area

---

## Resilience & Fault Tolerance

### **Q41: Explain what Resilience4j is and why you used it.**

**Answer:**
- **Lightweight fault tolerance library** for Java
- **Provides**: Circuit breaker, rate limiter, retry, bulkhead, time limiter
- **Why used**: Prevent cascading failures in microservices
- **In project**:
  - Circuit breaker for service-to-service calls
  - Fallback methods for graceful degradation
  - Retry policies for transient failures

---

### **Q42: Explain the Circuit Breaker pattern.**

**Answer:**
- **Three states**: Closed, Open, Half-Open
- **Closed**: Normal operation, requests pass through
- **Open**: Failure threshold reached, requests immediately fail (fast-fail)
- **Half-Open**: After timeout, test if service recovered
- **Benefits**:
  - Prevents resource exhaustion
  - Fast failure instead of waiting
  - Automatic recovery detection
  - Protects dependent services

---

### **Q43: How did you implement Circuit Breaker in your project?**

**Answer:**
- **Configuration**:
  - Failure rate threshold (e.g., 50%)
  - Wait duration in open state (e.g., 60s)
  - Sliding window size (e.g., 10 requests)
- **Applied to**:
  - Service-to-service calls via Eureka
  - External API calls (if any)
- **Fallback methods**:
  - Return cached data
  - Return default response
  - Throw meaningful exception

---

### **Q44: What is a fallback method and when is it called?**

**Answer:**
- **Fallback**: Alternative logic when primary fails
- **Triggered by**:
  - Circuit breaker open
  - Service timeout
  - Exception thrown
- **Example implementation**:
  ```java
  @CircuitBreaker(name = "bookService", fallbackMethod = "getBooksFallback")
  public List<Book> getBooks() { ... }
  
  public List<Book> getBooksFallback(Exception e) {
      return Collections.emptyList(); // or cached data
  }
  ```
- **Benefits**: Graceful degradation, better UX

---

### **Q45: How do you handle cascading failures?**

**Answer:**
- **Circuit breaker**: Stops calling failed services
- **Timeout settings**: Prevent long waits
- **Bulkhead pattern**: Isolate resources (thread pools)
- **Async processing**: Kafka decouples services
- **Service isolation**: Each service has own database
- **Health checks**: Detect failures early

---

### **Q46: Explain retry policy in Resilience4j vs Kafka @RetryableTopic.**

**Answer:**
- **Resilience4j Retry**:
  - For synchronous service calls
  - Immediate retries (e.g., 3 attempts with 1s wait)
  - No message persistence
- **Kafka @RetryableTopic**:
  - For asynchronous message processing
  - Exponential backoff (2s, 4s, 8s)
  - Messages persisted in retry topics
  - Dead Letter Queue for ultimate failures
- **Different use cases**: Sync vs async

---

### **Q47: What metrics would you monitor for circuit breakers?**

**Answer:**
- **Failure rate**: Percentage of failed requests
- **Circuit state**: Closed/Open/Half-Open
- **Call duration**: Response time trends
- **Fallback execution rate**: How often fallbacks used
- **Slow calls**: Requests exceeding threshold
- **Tools**: Spring Boot Actuator, Prometheus, Grafana

---

## Docker & Deployment

### **Q48: Explain your Docker setup.**

**Answer:**
- **Individual Dockerfiles**: Each service has Dockerfile
- **Multi-stage builds**: Build JAR, then create runtime image
- **Docker Compose**: Orchestrates all 9 containers
- **Networking**: All services on same Docker network
- **Volumes**: MySQL data persisted
- **Environment variables**: Configure services

---

### **Q49: What is the difference between Docker and Docker Compose?**

**Answer:**
- **Docker**: Platform for containerization, runs single containers
- **Docker Compose**: Tool for multi-container applications
- **Compose features**:
  - Define services in YAML
  - Networking between containers
  - Volume management
  - Dependency ordering (depends_on)
  - Single command to start all services

---

### **Q50: How do you ensure services start in the correct order?**

**Answer:**
- **depends_on** in docker-compose.yml:
  - Services depend on MySQL, Kafka
  - API Gateway depends on Eureka
- **Health checks**: Wait for service to be ready
- **Startup delay**: Script sleeps 10 seconds
- **Retry logic**: Services retry connecting to dependencies
- **Limitation**: depends_on doesn't wait for "ready", just "started"

---

### **Q51: How would you deploy this to production?**

**Answer:**
- **Kubernetes**: Recommended for production
  - Deployments, Services, ConfigMaps
  - Auto-scaling, self-healing
  - Load balancing, rolling updates
- **AWS ECS/Fargate**: Managed container service
- **CI/CD Pipeline**:
  - Jenkins/GitLab CI
  - Build Docker images
  - Push to registry (ECR, Docker Hub)
  - Deploy to cluster
- **Monitoring**: Prometheus, Grafana, ELK stack

---

### **Q52: What is a multi-stage Docker build?**

**Answer:**
- **Stage 1**: Build application (Maven, compile)
- **Stage 2**: Runtime (only JRE, JAR file)
- **Benefits**:
  - Smaller final image (no build tools)
  - Faster deployment
  - Better security (fewer tools in production)
- **Example**: Build with maven:3.8, run with openjdk:17-slim

---

## API Design & REST

### **Q53: Why did you choose REST over GraphQL or gRPC?**

**Answer:**
- **REST advantages**:
  - Simple, well-understood
  - Wide tooling support
  - Stateless
  - Cacheable
- **GraphQL**: Overkill for simple CRUD operations
- **gRPC**: Better for internal microservices, but REST fine for gateway
- **Decision**: REST sufficient for LMS use case

---

### **Q54: Explain the RESTful design of your APIs.**

**Answer:**
- **Resource-based URLs**: `/api/books`, `/api/books/{id}`
- **HTTP methods**:
  - GET: Retrieve
  - POST: Create
  - PUT: Update
  - DELETE: Delete
- **Status codes**: 200, 201, 400, 401, 404, 409, 503
- **Stateless**: Each request independent
- **JSON format**: Request and response bodies

---

### **Q55: How do you handle API versioning?**

**Answer:**
- **Current**: No versioning (v1 implicit)
- **Strategies**:
  - URL versioning: `/api/v1/books`, `/api/v2/books`
  - Header versioning: `Accept: application/vnd.api.v2+json`
  - Query param: `/api/books?version=2`
- **Recommendation**: URL versioning (most common)
- **When needed**: Breaking changes to API contract

---

### **Q56: What is pagination and why is it important?**

**Answer:**
- **Pagination**: Return data in chunks (pages)
- **Implementation**: `GET /api/books?page=0&size=10`
- **Benefits**:
  - Reduces network payload
  - Prevents out-of-memory
  - Better performance
  - Improved user experience
- **In project**: JpaRepository with Pageable support

---

### **Q57: How do you document your APIs?**

**Answer:**
- **Swagger/OpenAPI**: Configured with springdoc-openapi
- **Access**: http://localhost:8081/swagger-ui.html
- **Features**:
  - Interactive API documentation
  - Try API directly from browser
  - Request/response schemas
  - Example values
- **Benefits**: Easy for frontend developers, clear contract

---

## Performance & Optimization

### **Q58: What performance optimizations did you implement?**

**Answer:**
1. **Database indexes**: On username, ISBN, book_id in wishlist
2. **Pagination**: Limit data returned per request
3. **@Transactional(readOnly=true)**: Read-only queries optimization
4. **Soft delete**: Faster than hard delete (no cascading)
5. **Async processing**: Kafka for notifications (non-blocking)
6. **Connection pooling**: HikariCP (Spring Boot default)

---

### **Q59: How would you improve performance further?**

**Answer:**
- **Caching**: Redis for frequently accessed books, user data
- **Database read replicas**: Scale read operations
- **CDN**: For static content
- **Lazy loading**: Fetch related data only when needed
- **Database query optimization**: Review slow queries
- **Compression**: gzip responses
- **Rate limiting**: Prevent abuse
- **Connection pooling tuning**: Optimize HikariCP settings

---

### **Q60: How do you identify performance bottlenecks?**

**Answer:**
- **Application metrics**: Spring Boot Actuator
- **Database slow query log**: MySQL slow query log
- **APM tools**: New Relic, DataDog, Dynatrace
- **Profiling**: JProfiler, YourKit
- **Load testing**: JMeter, Gatling
- **Logging**: Track request duration
- **Database EXPLAIN**: Analyze query execution plans

---

### **Q61: What is the purpose of connection pooling?**

**Answer:**
- **Reuse database connections** instead of creating new ones
- **HikariCP**: Spring Boot default, very fast
- **Benefits**:
  - Reduced connection overhead
  - Better performance
  - Limited concurrent connections
  - Connection lifecycle management
- **Configuration**: pool size, timeout, max lifetime

---

## Error Handling & Logging

### **Q62: How do you handle exceptions in your application?**

**Answer:**
- **@ControllerAdvice**: Global exception handler
- **Custom exceptions**: BookNotFoundException, DuplicateIsbnException
- **ErrorResponse DTO**: Consistent error format
- **HTTP status mapping**:
  - 400: Validation errors
  - 401: Unauthorized
  - 404: Not found
  - 409: Conflict
  - 503: Service unavailable

---

### **Q63: What is @ControllerAdvice and how did you use it?**

**Answer:**
- **Global exception handler** for all controllers
- **@ExceptionHandler** methods for specific exceptions
- **Benefits**:
  - Centralized error handling
  - Consistent error responses
  - Avoid try-catch in controllers
- **Used for**: BookNotFoundException, DuplicateIsbnException, validation errors

---

### **Q64: How do you log in a distributed system?**

**Answer:**
- **Current**: SLF4J + Logback in each service
- **Improvement needed**:
  - **Correlation ID**: Track request across services
  - **Centralized logging**: ELK stack (Elasticsearch, Logstash, Kibana)
  - **Structured logging**: JSON format
  - **Log aggregation**: Collect logs from all services
  - **Log levels**: DEBUG for dev, INFO/WARN for prod

---

### **Q65: What information should you log?**

**Answer:**
- **DO log**:
  - Request/response (excluding sensitive data)
  - Errors with stack traces
  - Business events (book created, user registered)
  - Performance metrics (query duration)
  - Authentication attempts
- **DON'T log**:
  - Passwords
  - JWT tokens (full)
  - Credit card info
  - PII without masking

---

## Testing & Quality

### **Q66: What testing strategies did you use?**

**Answer:**
- **Unit tests**: Service layer logic (JUnit, Mockito)
- **Integration tests**: Repository tests with H2/Test containers
- **API tests**: REST endpoints (MockMvc)
- **End-to-end tests**: Complete flow script
- **Manual testing**: Postman collection

---

### **Q67: How do you test Kafka consumers?**

**Answer:**
- **EmbeddedKafka**: Spring Kafka test support
- **TestContainers**: Real Kafka in Docker for tests
- **Test approach**:
  1. Publish test message to topic
  2. Wait for consumer to process
  3. Verify expected behavior
  4. Check database state or mock calls

---

### **Q68: How would you test circuit breakers?**

**Answer:**
- **Simulate failures**: Mock service to throw exceptions
- **Verify state transitions**: Closed → Open → Half-Open
- **Test fallback**: Ensure fallback method called
- **Load testing**: Trigger circuit breaker with many failures
- **Metrics verification**: Check failure rate, circuit state

---

### **Q69: What is the difference between unit and integration tests?**

**Answer:**
- **Unit tests**:
  - Test single class/method
  - Mock dependencies
  - Fast, isolated
  - Example: Test BookService with mocked repository
- **Integration tests**:
  - Test multiple components
  - Real database (or TestContainers)
  - Slower, more realistic
  - Example: Test BookController + BookService + Database

---

## Scenario-Based Questions

### **Q70: A user reports books are not showing up in search. How do you debug?**

**Answer:**
1. **Check service status**: Is book-service running? Eureka dashboard
2. **Review logs**: book-service logs for errors
3. **Test endpoint**: Direct call to book-service (bypass gateway)
4. **Check database**: Run SQL query to verify data exists
5. **Verify indexes**: Ensure deleted=false filter working
6. **Check API Gateway**: Routing configuration
7. **Test with Swagger**: Eliminate client-side issues

---

### **Q71: Notification service is consuming messages very slowly. What do you do?**

**Answer:**
1. **Check consumer lag**: Kafka metrics, consumer group lag
2. **Scale consumers**: Add more instances (partition limit applies)
3. **Optimize processing**: Profile code, reduce processing time
4. **Check database**: Slow queries in notification processing?
5. **Increase partitions**: More partitions = more parallelism
6. **Batch processing**: Process multiple messages together
7. **Async within consumer**: Don't block on I/O

---

### **Q72: Database is running out of connections. What's the issue?**

**Answer:**
- **Possible causes**:
  - Connection leak (not closing connections)
  - Pool size too small
  - Too many concurrent requests
  - Long-running transactions
- **Solutions**:
  - Increase pool size (HikariCP max-pool-size)
  - Enable leak detection
  - Review transaction boundaries
  - Add connection timeout
  - Monitor active connections

---

### **Q73: How would you implement a "recommendation system" feature?**

**Answer:**
1. **Data collection**: Track user wishlists, borrows
2. **Algorithm**: Collaborative filtering or content-based
3. **Implementation options**:
   - Separate recommendation service
   - Kafka streaming for real-time updates
   - ML model (Python microservice)
   - Cache recommendations in Redis
4. **API**: `GET /api/books/recommendations`
5. **Background job**: Precompute recommendations nightly

---

### **Q74: A malicious user is making 1000 requests per second. How do you handle?**

**Answer:**
- **Rate limiting**: Implement at API Gateway
  - Per IP: 100 requests/minute
  - Per user: 1000 requests/hour
- **Tools**: Resilience4j RateLimiter, Redis
- **Response**: 429 Too Many Requests
- **Additional measures**:
  - CAPTCHA for suspicious activity
  - Block IP temporarily
  - Require authentication for all endpoints
  - Web Application Firewall (WAF)

---

### **Q75: How would you implement a "reserve book" feature?**

**Answer:**
1. **New table**: book_reservations (user_id, book_id, reserved_until)
2. **Business logic**:
   - User can reserve only if available
   - Reservation expires after 24 hours
   - Notify user when book becomes available
3. **Concurrency**: Optimistic locking or pessimistic locking
4. **Kafka event**: Book reserved → Notification sent
5. **Scheduled job**: Clean expired reservations
6. **API**: `POST /api/books/{id}/reserve`

---

### **Q76: System is experiencing high latency. How do you investigate?**

**Answer:**
1. **Check metrics**: Response times, throughput
2. **Database queries**: Slow query log, missing indexes
3. **Service health**: CPU, memory usage
4. **Network**: Service-to-service latency
5. **Kafka lag**: Consumer falling behind?
6. **Thread dumps**: Any deadlocks?
7. **Enable tracing**: Distributed tracing (Zipkin)
8. **Load test**: Identify breaking point

---

### **Q77: How would you migrate from monolith to microservices?**

**Answer:**
- **Strangler pattern**: Gradually extract services
- **Steps**:
  1. Identify bounded contexts
  2. Extract one service at a time (start with least dependent)
  3. Add API gateway
  4. Implement service discovery
  5. Migrate data (database per service)
  6. Test thoroughly
  7. Monitor closely
- **Challenges**: Data consistency, distributed transactions

---

### **Q78: A new requirement: Multi-tenancy support. How would you implement?**

**Answer:**
- **Approaches**:
  1. **Database per tenant**: Separate DB for each library
  2. **Schema per tenant**: Separate schema in same DB
  3. **Row-level isolation**: tenant_id column in all tables
- **Implementation**:
  - Extract tenant from JWT or subdomain
  - Hibernate multi-tenancy support
  - Filter all queries by tenant_id
- **Recommended**: Row-level (easiest) or Database per tenant (best isolation)

---

### **Q79: How would you implement event sourcing in this system?**

**Answer:**
- **Event sourcing**: Store all changes as events
- **Implementation**:
  - Events table: book_created, book_updated, book_deleted
  - Event store (Kafka, EventStore DB)
  - Replay events to rebuild state
- **Benefits**:
  - Complete audit trail
  - Time travel (state at any point)
  - Event replay for debugging
- **Challenges**: More complex, eventual consistency

---

### **Q80: Explain how you would add internationalization (i18n) support.**

**Answer:**
- **Spring i18n**: MessageSource, ResourceBundle
- **Files**: messages_en.properties, messages_es.properties
- **Accept-Language header**: Detect user language
- **Implementation**:
  - Error messages in properties files
  - LocaleResolver for language detection
  - @Value or MessageSource in code
- **Database**: Store user preferred language

---

## Bonus: System Design Questions

### **Q81: How would you design this system to handle 1 million users?**

**Answer:**
- **Horizontal scaling**: Multiple instances of each service
- **Load balancing**: API Gateway with load balancer
- **Database**: Read replicas, sharding
- **Caching**: Redis for hot data
- **CDN**: Static content delivery
- **Message queue**: Kafka partitioning
- **Monitoring**: Full observability stack
- **Auto-scaling**: Kubernetes HPA

---

### **Q82: What happens if MySQL crashes?**

**Answer:**
- **Impact**: All read/write operations fail
- **Mitigation**:
  - **Database replication**: Master-slave setup
  - **Automatic failover**: Promote slave to master
  - **Circuit breaker**: Fast-fail, return cached data
  - **Health checks**: Detect failure quickly
  - **Backup & restore**: Regular backups
- **Current**: Single point of failure (improvement needed)

---

### **Q83: How would you monitor this system in production?**

**Answer:**
- **Application metrics**: Spring Boot Actuator + Micrometer
- **Infrastructure**: Prometheus + Grafana
- **Logs**: ELK stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: Zipkin or Jaeger (distributed tracing)
- **Alerts**: PagerDuty, Slack integration
- **Dashboards**: Service health, request rate, error rate, latency
- **Business metrics**: Books created, users registered, notifications sent

---

### **Q84: What security vulnerabilities should you be aware of?**

**Answer:**
- **SQL injection**: Use parameterized queries (JPA handles this)
- **XSS**: Sanitize user input, encode output
- **CSRF**: Stateless JWT avoids this
- **Secrets exposure**: Don't commit secrets to Git
- **DDoS**: Rate limiting, WAF
- **Dependency vulnerabilities**: Regular updates, Dependabot
- **JWT security**: Strong secret, short expiration
- **Man-in-the-middle**: Use HTTPS (TLS)

---

### **Q85: How would you implement blue-green deployment?**

**Answer:**
- **Two environments**: Blue (current), Green (new)
- **Process**:
  1. Deploy new version to Green
  2. Run smoke tests
  3. Switch traffic to Green
  4. Monitor for issues
  5. Keep Blue for rollback
- **Tools**: Kubernetes, AWS ECS, Docker Swarm
- **Benefits**: Zero downtime, easy rollback
- **Database**: Backward-compatible schema changes

---

## Quick Recall - Key Points

### **Architecture:**
- 6 microservices + MySQL + Kafka
- Event-driven with Kafka
- Service discovery with Eureka
- API Gateway for routing
- Database per service

### **Key Technologies:**
- Spring Boot 3.x
- Spring Cloud (Gateway, Eureka)
- Spring Security (JWT)
- Spring Kafka
- Resilience4j
- MySQL + JPA/Hibernate
- Docker + Docker Compose

### **Design Patterns:**
- Circuit Breaker (Resilience4j)
- Saga (implicit via Kafka)
- API Gateway
- Database per Service
- Event Sourcing (partial - Kafka events)

### **Best Practices:**
- Exception handling with @ControllerAdvice
- Validation with Bean Validation
- Pagination for large datasets
- Soft deletes
- Transaction management
- Structured logging
- Security with JWT

### **Performance:**
- Database indexes
- Connection pooling
- Async processing (Kafka)
- Pagination
- @Transactional(readOnly=true)

### **Resilience:**
- Circuit breaker
- Retry mechanism (@RetryableTopic)
- Dead Letter Queue
- Fallback methods
- Health checks

### **Areas for Improvement:**
- Add caching (Redis)
- Implement RBAC
- Use Flyway for migrations
- Centralize error DTO
- Add distributed tracing
- Implement rate limiting
- External secret management
- Multiple Gateway instances

---

**Good luck with your interview!** 🎯🚀

Remember:
- Be honest about what you know and don't know
- Explain trade-offs in your decisions
- Mention improvement areas (shows maturity)
- Relate answers to real-world scenarios
- Ask clarifying questions when needed
