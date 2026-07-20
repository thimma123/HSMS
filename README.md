# Home Service Management System (HSMS)

## Overview

Home Service Management System (HSMS) is a **microservices-based backend application** designed to connect customers with skilled service providers such as plumbers, electricians, cleaners, and other technicians.

The system manages the complete lifecycle of a home service request, including user authentication, service booking, technician assignment, job execution, payments, and customer feedback.

The project focuses on building a scalable and maintainable architecture using independent microservices with secure API communication.



## Key Features

* JWT-based authentication and authorization
* Role-based access control for customers, technicians, and admins
* Service category management
* Customer booking management
* Technician assignment workflow
* Job execution tracking
* Payment status management
* Customer ratings and feedback
* Notification handling
* Analytics support for platform insights

# Architecture

HSMS follows a **Microservices Architecture** where each service is responsible for a specific business domain.

### Microservices

| Service              | Responsibility                                   |
| -------------------- | ------------------------------------------------ |
| Auth Service         | Handles authentication and JWT token generation  |
| User Service         | Manages customer, technician, and admin profiles |
| Category Service     | Maintains available service categories           |
| Booking Service      | Handles customer service requests                |
| Assignment Service   | Assigns technicians to bookings                  |
| Execution Service    | Tracks job progress and completion details       |
| Payment Service      | Manages payment records and status               |
| Feedback Service     | Handles customer reviews and ratings             |
| Notification Service | Sends booking and payment notifications          |
| Analytics Service    | Provides business insights and reports           |

# Tech Stack

## Backend

* Java 17+
* Spring Boot
* Spring MVC
* Spring Data JPA
* Spring Security
* JWT Authentication
* Spring Cloud OpenFeign
* Eureka Service Discovery
* REST APIs

## Database

* Oracle Database XE
* Hibernate ORM
* JPA

## Development Tools

* Maven
* Git
* Postman
* Eclipse

# System Workflow

1. User registers and authenticates through the Auth Service.
2. JWT token is generated for secure communication.
3. Customer selects a service category.
4. Booking Service creates a service request.
5. Assignment Service allocates a suitable technician.
6. Execution Service tracks job progress.
7. Payment Service manages transaction status.
8. Feedback Service collects customer reviews after completion.

# Security Implementation

* Stateless authentication using JWT
* Spring Security filters for request validation
* Role-based authorization
* Secure communication between services

# Project Structure

HSMS
│
├── Auth-Service
├── User-Service
├── Category-Service
├── Booking-Service
├── Assignment-Service
├── Execution-Service
├── Payment-Service
├── Feedback-Service
├── Notification-Service
└── Analytics-Service

# Running the Application

Each microservice can be started independently using Spring Boot.

Services should be started in the following order:

1. Service Discovery (Eureka Server)
2. Auth Service
3. User Service
4. Category Service
5. Booking Service
6. Assignment Service
7. Execution Service
8. Payment Service
9. Feedback Service
10. Notification Service
11. Analytics Service

# API Testing

APIs can be tested using Postman.

Authentication flow:

Register User
      ↓
Login
      ↓
Receive JWT Token
      ↓
Access Secured APIs

Protected APIs require:

Authorization: Bearer <JWT_TOKEN>

# Future Enhancements

* Docker containerization
* Cloud deployment
* API Gateway integration improvements
* Real-time notifications
* Payment gateway integration
* Advanced analytics dashboard
