package com.hsms.booking.repository;

import com.hsms.booking.entity.ServiceRequest;
import com.hsms.booking.enums.ServiceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for ServiceRequest entity
 */
@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

	/**
	 * Find all service requests by customer with pagination
	 */
	Page<ServiceRequest> findByCustomerId(Long customerId, Pageable pageable);

	/**
	 * Find all service requests by status with pagination
	 */
	Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);

	/**
	 * Find all service requests by status without pagination
	 */
	List<ServiceRequest> findByStatus(ServiceRequestStatus status);

	/**
	 * Find all service requests by customer and status
	 */
	List<ServiceRequest> findByCustomerIdAndStatus(Long customerId, ServiceRequestStatus status);

	/**
	 * Find paginated requests by customer and status
	 */
	Page<ServiceRequest> findByCustomerIdAndStatus(Long customerId, ServiceRequestStatus status, Pageable pageable);

	/**
	 * Find all service requests by technician with pagination
	 */
	Page<ServiceRequest> findByTechnicianId(Long technicianId, Pageable pageable);

	/**
	 * Find all service requests by technician and status
	 */
	List<ServiceRequest> findByTechnicianIdAndStatus(Long technicianId, ServiceRequestStatus status);

	/**
	 * Find all service requests by category
	 */
	List<ServiceRequest> findByCategoryId(Long categoryId);

	/**
	 * Find all service requests by category with pagination
	 */
	Page<ServiceRequest> findByCategoryId(Long categoryId, Pageable pageable);

	/**
	 * Find all service requests scheduled for a specific date range
	 */
	@Query("SELECT sr FROM ServiceRequest sr WHERE sr.scheduledDateTime BETWEEN :startDate AND :endDate")
	List<ServiceRequest> findRequestsByDateRange(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	/**
	 * Find all active requests (not completed or cancelled)
	 */
	@Query("SELECT sr FROM ServiceRequest sr WHERE sr.status NOT IN (:cancelledStatus, :completedStatus)")
	Page<ServiceRequest> findActiveRequests(@Param("cancelledStatus") ServiceRequestStatus cancelled,
			@Param("completedStatus") ServiceRequestStatus completed, Pageable pageable);

	/**
	 * Count all service requests by customer
	 */
	long countByCustomerId(Long customerId);

	/**
	 * Count all service requests by technician
	 */
	long countByTechnicianId(Long technicianId);

	/**
	 * Count all service requests by status
	 */
	long countByStatus(ServiceRequestStatus status);

	/**
	 * Check if service request exists for a customer
	 */
	boolean existsByRequestIdAndCustomerId(Long requestId, Long customerId);

	/**
	 * Find duplicate bookings (same customer, category, and similar time)
	 */
	List<ServiceRequest> findByCustomerIdAndCategoryIdAndScheduledDateTimeBetween(Long customerId, Long categoryId,
			LocalDateTime start, LocalDateTime end);
}
