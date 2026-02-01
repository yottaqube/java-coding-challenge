# Order Processing and Notification System

A comprehensive Java Spring Boot application that provides order management capabilities with multi-channel notifications, security, and comprehensive testing.

## 🏗️ Architecture Overview

This application implements a modern microservice-ready architecture with clean separation of concerns:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controller    │───▶│     Service      │───▶│   Repository    │
│   (REST API)    │    │  (Business Logic)│    │  (Data Access)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Security      │    │  Notification    │    │    Database     │
│  (Auth & Authz) │    │    Service       │    │     (H2)        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🚀 Key Features

### 1. Order Management API
- **Create Orders**: POST `/api/orders` - Create new orders with validation
- **Retrieve Orders**: GET `/api/orders/{id}` - Get order details by ID  
- **Update Status**: PUT `/api/orders/{id}/status` - Update order status with business rules
- **Search Orders**: GET `/api/orders/search` - Advanced search with pagination and filtering

### 2. Order Lifecycle Management
Orders follow a controlled state machine:
- **Initial State**: `CREATED`
- **Terminal States**: `CANCELLED`, `COMPLETED`
- **Business Rules**: Only transitions from `CREATED` to terminal states are allowed

### 3. Multi-Channel Notification System
- **Email Notifications**: Configurable email service integration
- **SMS Notifications**: Configurable SMS service integration  
- **Event-Driven**: Notifications triggered on order creation and status changes
- **Retry Mechanism**: Automatic retry with exponential backoff for failed notifications
- **Asynchronous Processing**: Non-blocking notification delivery
- **Future Enhancement**: Can be enhanced with Apache Kafka for scalable event-driven messaging

### 4. Security Implementation
- **Spring Security**: HTTP Basic Authentication for simplicity
- **Method-Level Security**: `@PreAuthorize` annotations on endpoints
- **In-Memory Users**: Demo users for testing (production should use database)
- **CSRF Protection**: Disabled for REST API, configurable for web apps
- **Future Enhancement**: Can be modified to use JWT for more security features

### 5. Error Handling & Logging
- **Global Exception Handler**: Centralized error handling with consistent JSON responses
- **Validation**: Bean validation with detailed field-level error messages
- **Comprehensive Logging**: Structured logging at appropriate levels
- **Meaningful HTTP Status Codes**: RESTful error responses

### 6. Testing Strategy
- **Unit Tests**: Service and controller layer testing with mocks
- **Integration Tests**: Full-stack testing with real database
- **WireMock Tests**: External service integration testing
- **Security Tests**: Authentication and authorization testing

## 📋 Technical Decisions & Rationale

### Framework Choices

**Spring Boot 3.2.2**: Latest stable version providing:
- Auto-configuration reducing boilerplate
- Embedded server for easy deployment
- Production-ready features (Actuator)
- Excellent testing support

**H2 In-Memory Database**: Chosen for simplicity and testing:
- Zero configuration required
- Perfect for development and demonstration
- Easy to switch to production database (PostgreSQL, MySQL)
- Supports SQL standards for complex queries

**Spring Security**: Industry standard for Java security:
- Mature and well-tested framework
- Flexible authentication/authorization
- Method-level security support
- Easy integration with external systems

### Design Patterns

**Repository Pattern**: Clean data access abstraction:
- Separates business logic from data access
- Enables easy testing with mocks
- Supports complex queries with Spring Data JPA

**Service Layer Pattern**: Encapsulates business logic:
- Transaction management
- Business rule validation
- Coordinates between different services

**DTO Pattern**: Clean API contracts:
- Separation between internal models and API
- Validation at the boundary
- API evolution without breaking changes

### Retry Implementation

The notification system implements retry logic using Spring Retry:

```java
@Retryable(
    retryFor = {ResourceAccessException.class, Exception.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
```

**Why This Approach**:
- **Declarative**: Clean, annotation-based configuration
- **Exponential Backoff**: Reduces load on failing services
- **Configurable**: Easy to adjust retry parameters
- **Exception-Specific**: Only retries on transient failures

**Alternative Approaches Considered**:
1. **Circuit Breaker**: Considered but overkill for this scope
2. **Manual Retry Logic**: More complex, less maintainable
3. **Message Queue**: Would add complexity but better for production

### Notification Architecture

**Asynchronous Processing**: 
```java
@Async
public void sendOrderCreatedNotification(Order order) {
    // Non-blocking notification processing
}
```

**Benefits**:
- Order operations aren't blocked by notification failures
- Better user experience with faster response times
- Natural fault tolerance

**Configuration-Driven Channels**:
```properties
notification.channels.email.enabled=true
notification.channels.sms.enabled=true
notification.channels.email.url=http://localhost:8081/email
```

This allows easy addition of new channels (Slack, Push Notifications, etc.) without code changes.

### Security Strategy

**Simplified Authentication**: HTTP Basic for demonstration:
- Easy to test with tools like Postman/curl
- No complex token management required
- Standard for internal/B2B APIs

**Production Considerations**:
- JWT tokens for stateless authentication
- OAuth2/OIDC for external integrations
- Database-backed user management
- Role-based access control (RBAC)

### Database Design

**Single Table Strategy**: Orders table with all necessary fields:
- Simple for demonstration purposes
- Easy to query and understand
- Includes audit fields (created_at, updated_at)

**Production Considerations**:
- Separate customer management
- Order line items for complex orders
- Audit logging table
- Database indexes for performance

## 🛠️ Setup & Running

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Clone and Build**:
   ```bash
   git clone <repository-url>
   cd java-coding-challenge
   mvn clean compile
   ```

2. **Run Tests**:
   ```bash
   mvn test
   ```

3. **Start Application**:
   ```bash
   docker compose up --build -d
   ```

4. **Access H2 Console** (Optional):
   - URL: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `sa123`

### Testing the API

**Authentication**: All endpoints require HTTP Basic Authentication
- Username: `user`, Password: `user123`
- Username: `admin`, Password: `admin123`

**Sample API Calls**:

1. **Create Order**:
   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -u user:user123 \
     -d '{
       "customerName": "John Doe",
       "productName": "Laptop",
       "quantity": 2,
       "price": 999.99,
       "customerEmail": "john@example.com",
       "customerPhone": "+1234567890"
     }'
   ```

2. **Get Order**:
   ```bash
   curl -X GET http://localhost:8080/api/orders/1 \
     -u user:password
   ```

3. **Update Order Status**:
   ```bash
   curl -X PUT http://localhost:8080/api/orders/1/status \
     -H "Content-Type: application/json" \
     -u user:user123 \
     -d '{"status": "COMPLETED"}'
   ```

4. **Search Orders**:
   ```bash
   curl -X GET "http://localhost:8080/api/orders/search?customerName=John&status=CREATED&page=0&size=10" \
     -u user:user123
   ```


5. **Benchmarking (optional)**:
   ```bash
   curl -u admin:admin123 http://localhost:8080/api/performance/jpql-vs-native

   curl -u admin:admin123 http://localhost:8080/api/performance/query-benchmark
   ```

## 🧪 Testing Strategy

### Test Categories

**Unit Tests** (`*Test.java`):
- Service layer logic testing
- Controller input/output testing  
- Validation testing
- Mock external dependencies

**Integration Tests** (`*IntegrationTest.java`):
- Full application context
- Real database operations
- End-to-end workflow testing
- Security integration

**External Service Tests** (`*ServiceIntegrationTest.java`):
- WireMock for external API testing
- Retry mechanism validation
- Error handling verification

### Running Tests

```bash
# All tests
mvn test

# With coverage
# With coverage report
mvn clean test jacoco:report

# View coverage report  
open target/site/jacoco/index.html
```

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration  
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# Notification Configuration
notification.channels.email.enabled=true
notification.channels.email.url=http://localhost:8081/email
notification.retry.max-attempts=3
notification.retry.delay=1000

# Logging Configuration
logging.level.com.danielkim.order.demo=DEBUG
```

### External Service Integration

The notification system expects external services to accept POST requests with this payload:

```json
{
  "orderId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890",
  "productName": "Laptop",
  "quantity": 2,
  "price": 999.99,
  "totalValue": 1999.98,
  "status": "CREATED",
  "eventType": "ORDER_CREATED",
  "message": "Your order has been created successfully",
  "timestamp": "2024-01-28T10:30:00"
}
```

### WireMock for Testing

For testing notifications, you can use WireMock to simulate external services:
Docker compose has the settings in it.

## 📈 Production Considerations

### Scalability
- **Database**: Move to PostgreSQL/MySQL with connection pooling
- **Caching**: Add Redis for frequently accessed orders
- **Load Balancing**: Multiple application instances behind load balancer
- **Message Queues**: Use RabbitMQ/Kafka for notifications at scale

### Security Enhancements
- **JWT Authentication**: Stateless token-based auth
- **Rate Limiting**: Prevent API abuse
- **Input Sanitization**: Prevent injection attacks
- **HTTPS Only**: TLS encryption for all communications

### Monitoring & Observability
- **Metrics**: Micrometer with Prometheus/Grafana
- **Application Logging**: Centralized logging with ELK stack
- **Health Checks**: Custom health indicators

### Data Management
- **Database Migrations**: Flyway/Liquibase for schema versioning
- **Backup Strategy**: Automated database backups
- **Data Retention**: Policies for historical order data
- **Read Replicas**: Separate read/write database instances
