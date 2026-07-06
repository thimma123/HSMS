HSMS (Home Services Management System) is a microservices-based web application designed to manage and automate home service bookings such as plumbing, electrical repairs, cleaning, carpentry, and painting.

The system is divided into multiple independent microservices, each responsible for a specific domain: Auth Service for login/registration, User Service for customer and technician management, Assignment Service for job allocation, Payment Service for transactions, and Analytics Service for dashboards and reports.

All services communicate through Spring Cloud Gateway and are registered with Eureka Service Discovery for load balancing.

Data is stored in Oracle Database (SQL), ensuring relational integrity while also supporting JSON for semi‑structured data.

Security is enforced via Spring Security with JWT, providing role‑based access for Admins, Service Managers, Technicians, and Customers.

This architecture ensures modularity, and maintainability, making HSMS a robust solution for real‑world home service management.