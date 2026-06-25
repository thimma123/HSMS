package com.hsms.assignmentservice.controller;

import com.hsms.assignmentservice.entity.Assignment;
import com.hsms.assignmentservice.entity.AssignmentStatus;
import com.hsms.assignmentservice.exception.AssignmentNotFoundException;
import com.hsms.assignmentservice.exception.DuplicateAssignmentException;
import com.hsms.assignmentservice.exception.TechnicianNotAvailableException;
import com.hsms.assignmentservice.feignclient.BookingServiceClient;
import com.hsms.assignmentservice.feignclient.CategoryClient;
import com.hsms.assignmentservice.feignclient.TechnicianClient;
import com.hsms.assignmentservice.feignclient.UserServiceClient;
import com.hsms.assignmentservice.model.*;
import com.hsms.assignmentservice.repository.AssignmentAuditRepository;
import com.hsms.assignmentservice.repository.AssignmentRepository;
import com.hsms.assignmentservice.service.AssignmentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AssignmentServiceImplTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentAuditRepository assignmentAuditRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private TechnicianClient technicianClient;
    @Mock private BookingServiceClient bookingServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private CategoryClient categoryClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AssignmentServiceImpl service;

    private AssignmentRequestDTO requestDTO;
    private TechnicianDetailResponseDTO technician;
    private ServiceRequestDTO serviceRequest;
    private CategoryResponseDTO category;
    private UserDTO user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        requestDTO = new AssignmentRequestDTO();
        requestDTO.setServiceRequestId(1L);
        requestDTO.setTechnicianId(100L);
        requestDTO.setUserId(200L);
        requestDTO.setStartTime(LocalDateTime.now());

        technician = new TechnicianDetailResponseDTO();
        technician.setTechnicianId(100L);
        technician.setUserId(100L);
        technician.setAvailabilityStatus("Available");
        technician.setSkill("Electrical");

        serviceRequest = new ServiceRequestDTO();
        serviceRequest.setRequestId(1L);
        serviceRequest.setCategoryId(10L);
        serviceRequest.setStatus("CREATED");

        category = new CategoryResponseDTO();
        category.setCategoryId(10L);
        category.setCategoryName("Electrical");

        user = new UserDTO();
        user.setId(200L);
    }

    @Test
    void testAssignTechnician_success() {
        // Arrange
        when(assignmentRepository.findByServiceRequestId(1L)).thenReturn(Optional.empty());
        when(technicianClient.getTechnicianById(100L))
                .thenReturn(ResponseEntity.ok(technician));
        when(bookingServiceClient.getServiceRequestById(1L))
                .thenReturn(ResponseEntity.ok(serviceRequest));
        when(categoryClient.getCategoryById(10L))
                .thenReturn(ResponseEntity.ok(category));
        when(userServiceClient.getUserById(200L)).thenReturn(user);
        when(assignmentRepository.existsByTechnicianIdAndStartTimeAndStatusIn(any(), any(), any()))
                .thenReturn(false);

        Assignment saved = new Assignment();
        saved.setId(1L);
        saved.setTechnicianId(100L);
        saved.setServiceRequestId(1L);
        saved.setStatus(AssignmentStatus.ASSIGNED);

        when(assignmentRepository.save(any(Assignment.class))).thenReturn(saved);
        when(modelMapper.map(saved, AssignmentResponseDTO.class)).thenReturn(new AssignmentResponseDTO());

        // Act
        AssignmentResponseDTO response = service.assignTechnician(requestDTO);

        // Assert
        assertThat(response).isNotNull();
        verify(assignmentRepository).save(any(Assignment.class));
        verify(assignmentAuditRepository).save(any());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void testAssignTechnician_duplicateAssignment_throwsException() {
        when(assignmentRepository.findByServiceRequestId(1L))
                .thenReturn(Optional.of(new Assignment()));

        assertThrows(DuplicateAssignmentException.class,
                () -> service.assignTechnician(requestDTO));
    }

    @Test
    void testAssignTechnician_technicianNotAvailable_throwsException() {
        technician.setAvailabilityStatus("Busy");
        when(assignmentRepository.findByServiceRequestId(1L)).thenReturn(Optional.empty());
        when(technicianClient.getTechnicianById(100L))
                .thenReturn(ResponseEntity.ok(technician));

        assertThrows(TechnicianNotAvailableException.class,
                () -> service.assignTechnician(requestDTO));
    }

    @Test
    void testAcceptJob_success() {
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTechnicianId(100L);
        assignment.setServiceRequestId(1L);
        assignment.setStatus(AssignmentStatus.ASSIGNED);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any())).thenReturn(assignment);
        when(modelMapper.map(any(), eq(AssignmentResponseDTO.class)))
                .thenReturn(new AssignmentResponseDTO());

        AssignmentResponseDTO response = service.acceptJob(1L);

        assertThat(response).isNotNull();
        verify(assignmentRepository).save(any());
        verify(assignmentAuditRepository).save(any());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void testAcceptJob_notFound_throwsException() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AssignmentNotFoundException.class,
                () -> service.acceptJob(1L));
    }

    @Test
    void testRejectJob_success() {
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTechnicianId(100L);
        assignment.setServiceRequestId(1L);
        assignment.setStatus(AssignmentStatus.ASSIGNED);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any())).thenReturn(assignment);
        when(modelMapper.map(any(), eq(AssignmentResponseDTO.class)))
                .thenReturn(new AssignmentResponseDTO());

        AssignmentResponseDTO response = service.rejectJob(1L, "Not interested");

        assertThat(response).isNotNull();
        verify(assignmentRepository).save(any());
        verify(assignmentAuditRepository).save(any());
        verify(eventPublisher).publishEvent(any());
    }
}
