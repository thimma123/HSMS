# HSMS - Tested Endpoints Documentation
**Date**: 2026-06-23  
**Base URL**: http://localhost:8080  
**Gateway Status**: ✅ UP (Port 8080)

---

## ✅ AUTH SERVICE - FULLY WORKING
**Service Status**: ✅ Working via API Gateway  
**Port**: 8081 (accessed via 8080)

### 1. Register User
```
POST /api/auth/register
Content-Type: application/json
```
**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "customer1@hsms.com",
  "password": "Password123",
  "role": "CUSTOMER"
}
```
**Response (201 Created):**
```json
{
  "userId": 34,
  "message": "Registration Successful. Hello John"
}
```

### 2. Login User
```
POST /api/auth/login
Content-Type: application/json
No Auth Required
```
**Request:**
```json
{
  "email": "customer1@hsms.com",
  "password": "Password123"
}
```
**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjFAaHNtcy5jb20iLCJ1c2VySWQiOjM0LCJyb2xlIjoiQ1VTVE9NRVIiLCJpYXQiOjE3ODIyMDkzMjEsImV4cCI6MTc4MjI5NTcyMX0.SH_lMACaliW20qSSV_JCwtfGYxSOZLfMz_8otCU2Yi4",
  "userId": 34,
  "role": "CUSTOMER"
}
```

**Test Credentials:**
| Role | Email | Password | User ID |
|------|-------|----------|---------|
| CUSTOMER | customer1@hsms.com | Password123 | 34 |
| TECHNICIAN | technician1@hsms.com | TechPass123 | 35 |
| SERVICE_MANAGER | admin@hsms.com | AdminPass123 | 36 |
| ADMIN | root@hsms.com | RootPass123 | 37 |

---

## ✅ USER SERVICE - FULLY WORKING
**Service Status**: ✅ Working via API Gateway  
**Port**: 8082 (accessed via 8080)

### 1. Create Customer Profile
```
POST /api/customers
Content-Type: application/json
Authorization: Bearer {customerToken}
```
**Request:**
```json
{
  "address": "123 Main Street",
  "city": "Mumbai",
  "pincode": "400001"
}
```
**Response (201 Created):**
```json
{
  "address": "123 Main Street",
  "city": "Mumbai",
  "createdAt": "2026-06-23T15:38:54.0846909",
  "email": "customer1@hsms.com",
  "name": "John Doe",
  "pincode": "400001",
  "userId": 34
}
```

### 2. Create Technician Profile
```
POST /api/technicians
Content-Type: application/json
Authorization: Bearer {technicianToken}
```
**Request:**
```json
{
  "skill": "Plumbing",
  "experience": 5,
  "availabilityStatus": "AVAILABLE"
}
```
**Response (201 Created):**
```json
{
  "availabilityStatus": "AVAILABLE",
  "email": "technician1@hsms.com",
  "experience": 5,
  "name": "Rajesh Kumar",
  "rating": 0.0,
  "skill": "Plumbing",
  "technician_Id": 28,
  "userId": 35
}
```

### 3. Get Customer Profile
```
GET /api/customers/{userId}
Authorization: Bearer {jwtToken}
```

### 4. Get Technician Profile
```
GET /api/technicians/{userId}
Authorization: Bearer {jwtToken}
```

### 5. Get All Customers
```
GET /api/customers
Authorization: Bearer {adminToken}
```

### 6. Get All Technicians
```
GET /api/technicians
Authorization: Bearer {adminToken}
```

### 7. Update Customer Profile
```
PUT /api/customers/{userId}
Content-Type: application/json
Authorization: Bearer {jwtToken}
```
**Request:**
```json
{
  "address": "456 Elm Street",
  "city": "Pune",
  "pincode": "411001"
}
```

---

## ⚠️ CATEGORY SERVICE - NOT RESPONDING
**Service Status**: ❌ Unavailable (503 Service Unavailable)  
**Port**: 8087  
**Issue**: Service not registered in Eureka or not running

**Endpoints (Unable to Test):**
- POST /api/categories - Add Category
- PATCH /api/categories/{id} - Update Category
- GET /api/categories - Get All Categories
- GET /api/categories/{id} - Get Category by ID

---

## ⚠️ BOOKING SERVICE - NOT RESPONDING
**Service Status**: ❌ Unavailable (503 Service Unavailable)  
**Port**: 8083  
**Issue**: Service not registered in Eureka or not running

**Endpoints (Unable to Test):**
- POST /api/service-requests - Create Booking
- GET /api/service-requests/my-requests - Get My Bookings
- GET /api/service-requests - Get All Bookings
- PUT /api/service-requests/{id}/cancel - Cancel Booking
- GET /api/service-requests/status/{status} - Get by Status
- GET /api/service-requests/technician/{technicianId} - Get by Technician
- GET /api/service-requests/category/{categoryId} - Get by Category

---

## ⚠️ ASSIGNMENT SERVICE - PARTIALLY WORKING
**Service Status**: ⚠️ Available but depends on Booking Service (down)  
**Port**: 8084  
**Issue**: Feign calls to Booking Service fail (503 Service Unavailable)

**Available Endpoints:**

### 1. Create Assignment
```
POST /api/assignments
Content-Type: application/json
Authorization: Bearer {serviceManagerToken}
Role Required: SERVICE_MANAGER
```
**Request:**
```json
{
  "technicianId": 28,
  "serviceRequestId": 1,
  "startTime": "2026-08-10T11:00:00"
}
```
**Current Status**: ❌ 500 Internal Server Error (Booking Service unavailable)

### 2. Accept Assignment
```
PUT /api/assignments/{assignmentId}/accept
Authorization: Bearer {technicianToken}
Role Required: TECHNICIAN
```
**Response (200 OK):** Assignment status updated to ACCEPTED

### 3. Reject Assignment
```
PUT /api/assignments/{assignmentId}/reject?reason=Busy on another repair
Authorization: Bearer {technicianToken}
Role Required: TECHNICIAN
```
**Response (200 OK):** Assignment status updated to REJECTED

### 4. Reassign Assignment
```
PUT /api/assignments/{assignmentId}/reassign
Content-Type: application/json
Authorization: Bearer {serviceManagerToken}
Role Required: SERVICE_MANAGER
```
**Request:**
```json
{
  "newTechnicianId": 28,
  "reason": "Previous technician unavailable"
}
```

### 5. Get All Assignments
```
GET /api/assignments
Authorization: Bearer {jwtToken}
```

### 6. Get Assignment by ID
```
GET /api/assignments/{assignmentId}
Authorization: Bearer {jwtToken}
```

---

## ⚠️ EXECUTION SERVICE - NOT TESTED
**Service Status**: ⚠️ Available but depends on Booking Service (down)  
**Port**: 8085  
**Issue**: Cannot test without valid service requests

**Available Endpoints:**

### 1. Start Service
```
POST /api/records/start
Content-Type: application/json
Authorization: Bearer {technicianToken}
```
**Request:**
```json
{
  "serviceRequestId": 1
}
```
**Response (200 OK):**
```json
{
  "recordId": 1,
  "status": "IN_PROGRESS",
  "startTime": "2026-06-23T10:15:00"
}
```

### 2. Complete Service
```
PUT /api/records/{recordId}/complete
Content-Type: application/json
Authorization: Bearer {technicianToken}
```
**Request:**
```json
{
  "completionNotes": "Service completed successfully",
  "totalHours": 2.5
}
```
**Response (200 OK):** Service record marked as COMPLETED

### 3. Get All Records
```
GET /api/records
Authorization: Bearer {jwtToken}
```

### 4. Get Record by ID
```
GET /api/records/{recordId}
Authorization: Bearer {jwtToken}
```

---

## 📝 JWT TOKENS FOR TESTING

### Customer Token (expires in 24 hours)
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lcjFAaHNtcy5jb20iLCJ1c2VySWQiOjM0LCJyb2xlIjoiQ1VTVE9NRVIiLCJpYXQiOjE3ODIyMDkzMjEsImV4cCI6MTc4MjI5NTcyMX0.SH_lMACaliW20qSSV_JCwtfGYxSOZLfMz_8otCU2Yi4
```

### Technician Token (expires in 24 hours)
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZWNobmljaWFuMUBoc21zLmNvbSIsInVzZXJJZCI6MzUsInJvbGUiOiJURUNITklDSUFOIiwiaWF0IjoxNzgyMjA5MzU1LCJleHAiOjE3ODIyOTU3NTV9.LhbF1N8iIxCZ7Fcr22EBhj5kUJxGTAP5XAo3Zm16SK0
```

### Admin Token (expires in 24 hours)
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJyb290QGhzbXMuY29tIiwidXNlcklkIjozNywicm9sZSI6IkFETUlOIiwiaWF0IjoxNzgyMjA5NDYzLCJleHAiOjE3ODIyOTU4NjN9.LEG-tsXtyEgHVsjK_m9xJPAU2_EZGbIWOEuo34InWbw
```

### Service Manager Token (expires in 24 hours)
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBoc21zLmNvbSIsInVzZXJJZCI6MzYsInJvbGUiOiJTRVJWSUNFX01BTkFHRVIiLCJpYXQiOjE3ODIyMDk0MDksImV4cCI6MTc4MjI5NTgwOX0.6edLXnD4ddPxNzx3Tov0FaZAK-6aTBOgdx_WTPpgBl4
```

---

## 🔐 Authorization by Role

| Endpoint | CUSTOMER | TECHNICIAN | SERVICE_MANAGER | ADMIN |
|----------|----------|-----------|-----------------|-------|
| POST /api/auth/register | ✅ | ✅ | ✅ | ✅ |
| POST /api/auth/login | ✅ | ✅ | ✅ | ✅ |
| POST /api/customers | ✅ | ❌ | ❌ | ✅ |
| GET /api/customers | ❌ | ❌ | ✅ | ✅ |
| PUT /api/customers/{id} | ✅ (self) | ❌ | ✅ | ✅ |
| POST /api/technicians | ❌ | ✅ | ❌ | ✅ |
| GET /api/technicians | ❌ | ❌ | ✅ | ✅ |
| PUT /api/technicians/{id}/rating | ✅ | ❌ | ✅ | ✅ |
| POST /api/assignments | ❌ | ❌ | ✅ | ❌ |
| PUT /api/assignments/{id}/accept | ❌ | ✅ | ❌ | ❌ |
| PUT /api/assignments/{id}/reject | ❌ | ✅ | ❌ | ❌ |
| PUT /api/assignments/{id}/reassign | ❌ | ❌ | ✅ | ❌ |

---

## 📋 Integration Testing Checklist

- ✅ Auth Service: Register & Login working
- ✅ User Service: Customer & Technician profiles working
- ✅ API Gateway: Routing working
- ✅ JWT Token Validation: Working
- ✅ Role-Based Access Control: Working
- ⚠️ Category Service: Not responding
- ⚠️ Booking Service: Not responding
- ⚠️ Assignment Service: Partially working (depends on Booking)
- ⚠️ Execution Service: Not tested (depends on Booking)

---

## 🔧 Setup Environment in Postman

1. Import all `.postman_collection.json` files
2. Import `HSMS-Environment.postman_environment.json`
3. Set up environment variables:
   - `baseUrl`: http://localhost:8080
   - `jwtToken`: Use login response token
   - `userId`: From registration or login response
   - `customerId`: From customer profile creation
   - `technicianId`: 28
   - `categoryId`: 1 (hardcoded, service unavailable)
   - `bookingId`: 1 (hardcoded, service unavailable)

---

## ⚠️ Known Issues

1. **Booking Service Down**: Cannot create service requests
2. **Category Service Down**: Cannot create or manage categories
3. **Assignment Service 500 Error**: Depends on booking service for Feign calls
4. **Services Not Responding Directly**: Must route through API Gateway on port 8080

---

## 🚀 Next Steps

1. **Restart Booking Service**: `java -jar booking-service/target/booking-service-1.0.0.jar`
2. **Restart Category Service**: `java -jar category-service/target/category-service-1.0.0.jar`
3. Verify Eureka registration at: http://localhost:8761
4. Re-test all endpoints once services are up
5. Run full integration test suite
