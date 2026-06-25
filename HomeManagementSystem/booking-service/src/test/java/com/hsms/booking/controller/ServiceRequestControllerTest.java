package com.hsms.booking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsms.booking.dto.ServiceRequestDTO;
import com.hsms.booking.dto.ServiceRequestResponse;
import com.hsms.booking.enums.ServiceRequestStatus;
import com.hsms.booking.service.ServiceRequestService;

@SpringBootTest(classes = com.hsms.booking.BookingServiceApplication.class)
@AutoConfigureMockMvc
public class ServiceRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceRequestService serviceRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateToken(Long userId, String email, String role) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode("VGhpc0lzTXlTZWNyZXRLZXlGb3JKV1RUb2tlbjEyMzQ1Njc4OTA=");
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
        return io.jsonwebtoken.Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("userId", userId)
                .signWith(key)
                .compact();
    }

    @Test
    public void testCreateServiceRequest() throws Exception {
        ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.setCategoryId(1L);
        dto.setAddress("123 Main St");
        dto.setCity("Metropolis");
        dto.setPincode("123456");
        dto.setScheduledDateTime(LocalDateTime.now().plusDays(1));

        ServiceRequestResponse response = new ServiceRequestResponse();
        response.setRequestId(1L);
        response.setStatus(ServiceRequestStatus.CREATED);
        response.setCustomerId(5L);

        when(serviceRequestService.createServiceRequest(any(ServiceRequestDTO.class), eq(5L)))
                .thenReturn(response);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/service-requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    public void testUpdateServiceRequest() throws Exception {
        ServiceRequestDTO dto = new ServiceRequestDTO();
        dto.setCategoryId(1L);
        dto.setAddress("456 Elm St");
        dto.setScheduledDateTime(LocalDateTime.now().plusDays(1));

        ServiceRequestResponse response = new ServiceRequestResponse();
        response.setRequestId(1L);
        response.setStatus(ServiceRequestStatus.CREATED);
        response.setCustomerId(5L);

        when(serviceRequestService.updateServiceRequest(eq(1L), any(ServiceRequestDTO.class), eq(5L)))
                .thenReturn(response);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(put("/api/service-requests/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }
}
