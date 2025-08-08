# HealthFirst API

A comprehensive Healthcare Provider and Patient Management System API built with Spring Boot, featuring JWT authentication, role-based access control, and comprehensive documentation.

## 🚀 Features

- **Provider Management**: Registration and authentication for healthcare providers
- **Patient Management**: Registration and authentication for patients
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Different roles for providers and patients
- **Swagger Documentation**: Interactive API documentation
- **PostgreSQL Database**: Robust data persistence
- **Input Validation**: Comprehensive request validation
- **Password Security**: BCrypt password hashing

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.4
- **Database**: PostgreSQL 16.9
- **Authentication**: JWT (JSON Web Tokens)
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven
- **Java Version**: 17

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 16.9
- Docker (optional)

## 🚀 Quick Start

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE provider_db;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/provider_db
spring.datasource.username=postgres
spring.datasource.password=root
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Access the interactive API documentation at: `http://localhost:8080/swagger-ui/index.html`

### OpenAPI Specification
Get the OpenAPI JSON specification at: `http://localhost:8080/v3/api-docs`

## 🔐 Authentication

The API uses JWT (JSON Web Token) authentication. After successful login, include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## 📡 API Endpoints

### Provider Management

#### Register Provider
```http
POST /api/v1/provider/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "password": "StrongP@ssw0rd",
  "specialization": "Cardiology",
  "licenseNumber": "LIC123456",
  "yearsOfExperience": 10,
  "clinicAddress": {
    "street": "123 Main St",
    "city": "Metropolis",
    "state": "NY",
    "zip": "10001"
  }
}
```

#### Provider Login
```http
POST /api/v1/provider/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "StrongP@ssw0rd"
}
```

### Patient Management

#### Register Patient
```http
POST /api/v1/patient/register
Content-Type: application/json

{
  "firstName": "Alice",
  "lastName": "Johnson",
  "email": "alice.johnson@example.com",
  "phoneNumber": "+1555123456",
  "password": "StrongP@ssw0rd",
  "dateOfBirth": "1990-05-15",
  "gender": "female",
  "address": {
    "street": "789 Pine St",
    "city": "Chicago",
    "state": "IL",
    "zip": "60601"
  },
  "emergencyContact": {
    "name": "Bob Johnson",
    "phone": "+1555123457",
    "relationship": "spouse"
  },
  "medicalHistory": ["Hypertension", "Diabetes"],
  "insuranceInfo": {
    "provider": "Blue Cross",
    "policyNumber": "BC123456"
  }
}
```

#### Patient Login
```http
POST /api/v1/patient/login
Content-Type: application/json

{
  "email": "alice.johnson@example.com",
  "password": "StrongP@ssw0rd"
}
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Manual Testing with cURL

#### Test Provider Registration
```bash
curl -X POST http://localhost:8080/api/v1/provider/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "password": "StrongP@ssw0rd",
    "specialization": "Cardiology",
    "licenseNumber": "LIC123456",
    "yearsOfExperience": 10,
    "clinicAddress": {
      "street": "123 Main St",
      "city": "Metropolis",
      "state": "NY",
      "zip": "10001"
    }
  }'
```

#### Test Patient Registration
```bash
curl -X POST http://localhost:8080/api/v1/patient/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Alice",
    "lastName": "Johnson",
    "email": "alice.johnson@example.com",
    "phoneNumber": "+1555123456",
    "password": "StrongP@ssw0rd",
    "dateOfBirth": "1990-05-15",
    "gender": "female",
    "address": {
      "street": "789 Pine St",
      "city": "Chicago",
      "state": "IL",
      "zip": "60601"
    }
  }'
```

## 📦 Postman Collection

Import the provided `HealthFirst_API_Postman_Collection.json` file into Postman for easy API testing.

### Collection Variables
- `base_url`: `http://localhost:8080`
- `provider_token`: JWT token for provider authentication
- `patient_token`: JWT token for patient authentication

## 🔧 Configuration

### Application Properties

| Property | Description | Default |
|----------|-------------|---------|
| `spring.datasource.url` | Database URL | `jdbc:postgresql://localhost:5433/provider_db` |
| `spring.datasource.username` | Database username | `postgres` |
| `spring.datasource.password` | Database password | `root` |
| `spring.jpa.hibernate.ddl-auto` | Hibernate DDL mode | `update` |
| `spring.jpa.show-sql` | Show SQL queries | `true` |

### Security Configuration

- JWT token expiration: 1 hour
- Password requirements: Minimum 8 characters with uppercase, lowercase, number, and special character
- CORS: Disabled for development
- CSRF: Disabled for API usage

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/healthfirst/provider/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── resources/
│       └── application.properties
└── test/                    # Test classes
```

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Hashing**: BCrypt password encryption
- **Input Validation**: Comprehensive request validation
- **Role-Based Access**: Different permissions for providers and patients
- **Token Expiration**: Automatic token expiration for security

## 📝 Validation Rules

### Provider Registration
- First/Last name: 2-50 characters
- Email: Valid email format, unique
- Phone: Valid phone format, unique
- Password: Minimum 8 characters with complexity requirements
- License number: Alphanumeric, unique
- Years of experience: 0-50
- Specialization: Must be from allowed list

### Patient Registration
- First/Last name: 2-50 characters
- Email: Valid email format, unique
- Phone: Valid phone format, unique
- Password: Minimum 8 characters with complexity requirements
- Date of birth: Must be in the past
- Gender: Must be from allowed enum values

## 🚨 Error Handling

The API returns appropriate HTTP status codes and error messages:

- `200`: Success
- `201`: Created
- `400`: Bad Request
- `401`: Unauthorized
- `409`: Conflict (duplicate entry)
- `422`: Validation Error
- `500`: Internal Server Error

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:
- Email: support@healthfirst.com
- Documentation: `http://localhost:8080/swagger-ui/index.html`
- Issues: Create an issue in the repository

## 🔄 Recent Updates

- ✅ Fixed JWT token generation for both providers and patients
- ✅ Added comprehensive Swagger documentation
- ✅ Created Postman collection for easy testing
- ✅ Enhanced security configuration
- ✅ Added input validation and error handling
- ✅ Implemented role-based authentication