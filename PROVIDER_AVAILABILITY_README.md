# Provider Availability Management Module

A robust Spring Boot module for managing healthcare provider availability and appointment slots with advanced features including timezone handling, recurrence patterns, and comprehensive search capabilities.

## 🏗️ Architecture Overview

### Core Entities

1. **ProviderAvailability** - Defines provider availability windows with recurrence patterns
2. **AppointmentSlot** - Individual appointment slots generated from availability

### Key Features

- ✅ **Timezone-aware storage** - All times stored in UTC, converted for user display
- ✅ **Recurrence support** - Daily, weekly, monthly patterns with custom day selection
- ✅ **Overlap prevention** - Validates no conflicting availability windows
- ✅ **Slot generation** - Auto-generates appointment slots based on availability
- ✅ **Comprehensive search** - Filter by location, date, appointment type, price, etc.
- ✅ **Booking protection** - Prevents deletion of availability with booked appointments

## 🗂️ Database Schema

### ProviderAvailability Table
```sql
CREATE TABLE provider_availability (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL, -- UTC
    end_time TIMESTAMP NOT NULL, -- UTC
    timezone VARCHAR(50) NOT NULL,
    recurrence_type VARCHAR(20), -- NONE, DAILY, WEEKLY, MONTHLY
    slot_duration_minutes INTEGER NOT NULL,
    price DECIMAL(10,2),
    currency VARCHAR(3),
    location VARCHAR(255),
    appointment_type VARCHAR(100),
    special_requirements TEXT,
    status VARCHAR(20) NOT NULL, -- ACTIVE, INACTIVE, SUSPENDED, DELETED
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Recurrence days for weekly patterns
CREATE TABLE provider_availability_recurrence_days (
    availability_id BIGINT REFERENCES provider_availability(id),
    day_of_week VARCHAR(20) NOT NULL
);
```

### AppointmentSlot Table
```sql
CREATE TABLE appointment_slots (
    id BIGSERIAL PRIMARY KEY,
    provider_availability_id BIGINT REFERENCES provider_availability(id),
    provider_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL, -- UTC
    end_time TIMESTAMP NOT NULL, -- UTC
    timezone VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL, -- AVAILABLE, BOOKED, CANCELLED, COMPLETED, NO_SHOW
    price DECIMAL(10,2),
    currency VARCHAR(3),
    location VARCHAR(255),
    appointment_type VARCHAR(100),
    special_requirements TEXT,
    patient_id BIGINT,
    booking_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## 🚀 API Endpoints

### 1. Create Provider Availability
**POST** `/api/v1/provider/availability`

Creates availability and auto-generates appointment slots.

```json
{
  "providerId": 1,
  "startTime": "2024-01-15T09:00:00",
  "endTime": "2024-01-15T17:00:00",
  "timezone": "America/New_York",
  "recurrenceType": "WEEKLY",
  "recurrenceDays": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "recurrenceEndDate": "2024-02-15T17:00:00",
  "slotDurationMinutes": 30,
  "price": 100.00,
  "currency": "USD",
  "location": "New York Medical Center",
  "appointmentType": "CONSULTATION",
  "specialRequirements": "Wheelchair accessible",
  "status": "ACTIVE",
  "notes": "Regular consultation hours"
}
```

**Response:**
```json
{
  "id": 1,
  "providerId": 1,
  "startTime": "2024-01-15T14:00:00",
  "endTime": "2024-01-15T22:00:00",
  "timezone": "America/New_York",
  "recurrenceType": "WEEKLY",
  "recurrenceDays": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "slotDurationMinutes": 30,
  "price": 100.00,
  "currency": "USD",
  "location": "New York Medical Center",
  "appointmentType": "CONSULTATION",
  "status": "ACTIVE",
  "totalSlots": 48,
  "availableSlots": 48,
  "bookedSlots": 0,
  "cancelledSlots": 0,
  "appointmentSlots": [...]
}
```

### 2. Get Provider Availability
**GET** `/api/v1/provider/{providerId}/availability`

Returns availability with slot statistics and detailed slot data.

**Response:**
```json
{
  "id": 1,
  "providerId": 1,
  "totalSlots": 48,
  "availableSlots": 45,
  "bookedSlots": 3,
  "cancelledSlots": 0,
  "appointmentSlots": [
    {
      "id": 1,
      "providerId": 1,
      "startTime": "2024-01-15T14:00:00",
      "endTime": "2024-01-15T14:30:00",
      "status": "AVAILABLE",
      "price": 100.00,
      "currency": "USD",
      "location": "New York Medical Center",
      "appointmentType": "CONSULTATION"
    }
  ]
}
```

### 3. Update Appointment Slot
**PUT** `/api/v1/provider/availability/{slotId}`

Updates slot timing, status, pricing, or notes.

```json
{
  "price": 150.00,
  "status": "CANCELLED",
  "bookingNotes": "Patient requested cancellation"
}
```

### 4. Delete Provider Availability
**DELETE** `/api/v1/provider/availability/{availabilityId}?deleteRecurring=false`

Deletes availability (optionally all recurring slots).

### 5. Search Available Slots
**GET** `/api/v1/availability/search`

Patients can search for available slots by various criteria.

**Query Parameters:**
- `startDate` (required) - Start date for search
- `endDate` - End date for search (defaults to 30 days)
- `location` - Location filter
- `appointmentType` - Appointment type filter
- `providerId` - Specific provider filter
- `maxPrice` - Maximum price filter
- `timezone` - Timezone for date conversion
- `slotDurationMinutes` - Specific slot duration

**Example:**
```
GET /api/v1/availability/search?startDate=2024-01-15&location=New York&appointmentType=CONSULTATION&maxPrice=200
```

**Response:**
```json
[
  {
    "id": 1,
    "providerId": 1,
    "startTime": "2024-01-15T14:00:00",
    "endTime": "2024-01-15T14:30:00",
    "status": "AVAILABLE",
    "price": 100.00,
    "currency": "USD",
    "location": "New York Medical Center",
    "appointmentType": "CONSULTATION"
  }
]
```

## 🌍 Timezone Handling

The system handles timezones robustly:

1. **Storage**: All times stored in UTC in the database
2. **Input**: Accepts times in provider's timezone
3. **Conversion**: Automatically converts to UTC for storage
4. **Display**: Converts back to user's timezone for display

**Example:**
- Provider sets availability: 9:00 AM - 5:00 PM EST
- Stored in DB: 2:00 PM - 10:00 PM UTC (EST + 5 hours)
- Displayed to user: Converted back to their timezone

## 🔄 Recurrence Patterns

### Supported Patterns
- **NONE**: Single availability window
- **DAILY**: Repeats every day
- **WEEKLY**: Repeats on specific days of the week
- **MONTHLY**: Repeats monthly on the same date

### Weekly Recurrence Example
```json
{
  "recurrenceType": "WEEKLY",
  "recurrenceDays": ["MONDAY", "WEDNESDAY", "FRIDAY"],
  "recurrenceEndDate": "2024-02-15T17:00:00"
}
```

This creates availability slots for every Monday, Wednesday, and Friday until February 15, 2024.

## 🧪 Testing

### Unit Tests
Run unit tests for the service layer:
```bash
mvn test -Dtest=ProviderAvailabilityServiceTest
```

### Integration Tests
Run integration tests for the controller:
```bash
mvn test -Dtest=ProviderAvailabilityControllerTest
```

### Test Coverage
- ✅ Slot creation and validation
- ✅ Recurrence logic
- ✅ Timezone conversion
- ✅ Overlap detection
- ✅ Booking protection
- ✅ Search functionality

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Maven 3.6+

### Database Setup
1. Create PostgreSQL database:
```sql
CREATE DATABASE provider_db;
```

2. Update `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/provider_db
spring.datasource.username=postgres
spring.datasource.password=root
```

### Running the Application
```bash
mvn spring-boot:run
```

The application will start on port 8081 with automatic table creation.

### API Documentation
Once running, access Swagger UI at:
```
http://localhost:8081/swagger-ui.html
```

## 🔐 Security Considerations

- All endpoints should be protected with authentication
- Provider can only manage their own availability
- Patients can only search for available slots
- Input validation prevents malicious data

## 📊 Performance Optimizations

- Database indexes on frequently queried columns
- Pagination for large result sets
- Caching for timezone conversions
- Batch operations for slot generation

## 🐛 Error Handling

The system provides comprehensive error handling:

- **400 Bad Request**: Invalid input data
- **404 Not Found**: Resource not found
- **409 Conflict**: Business rule violations (e.g., overlapping availability)
- **500 Internal Server Error**: Unexpected errors

## 🔄 Future Enhancements

- [ ] Real-time availability updates
- [ ] Integration with calendar systems
- [ ] Advanced recurrence patterns (custom intervals)
- [ ] Bulk operations for multiple providers
- [ ] Analytics and reporting
- [ ] Mobile app support
- [ ] Push notifications for availability changes

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details. 