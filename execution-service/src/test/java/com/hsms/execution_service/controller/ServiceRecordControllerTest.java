package com.hsms.execution_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsms.execution_service.model.ServiceRecordDetailResponseDTO;
import com.hsms.execution_service.model.ServiceRecordRequestDTO;
import com.hsms.execution_service.model.ServiceRecordResponseDTO;
import com.hsms.execution_service.service.ServiceRecordService;

@SpringBootTest
@AutoConfigureMockMvc
public class ServiceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceRecordService serviceRecordService;

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
    public void testStartService() throws Exception {
        ServiceRecordRequestDTO dto = new ServiceRecordRequestDTO();
        dto.setServiceRequestId(1L);

        ServiceRecordResponseDTO response = new ServiceRecordResponseDTO();
        response.setRecordId(1L);
        response.setServiceRequestId(1L);
        response.setStatus("IN_PROGRESS");

        when(serviceRecordService.start(any(ServiceRecordRequestDTO.class))).thenReturn(response);

        String token = generateToken(10L, "tech@example.com", "TECHNICIAN");

        mockMvc.perform(post("/api/records/start")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    public void testCompleteService() throws Exception {
        ServiceRecordRequestDTO dto = new ServiceRecordRequestDTO();
        dto.setRemarks("Finished repairing.");
        dto.setActualCost(100.0);
        dto.setPaymentMethod(com.hsms.execution_service.entity.PaymentMethod.UPI);

        ServiceRecordDetailResponseDTO response = new ServiceRecordDetailResponseDTO();
        response.setRecordId(1L);
        response.setStatus("COMPLETED");

        when(serviceRecordService.complete(eq(1L), any(ServiceRecordRequestDTO.class))).thenReturn(response);

        String token = generateToken(10L, "tech@example.com", "TECHNICIAN");

        mockMvc.perform(put("/api/records/complete/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testUpdatePaymentStatus() throws Exception {
        ServiceRecordDetailResponseDTO response = new ServiceRecordDetailResponseDTO();
        response.setRecordId(1L);
        response.setStatus("COMPLETED");
        response.setPaymentStatus("SUCCESS");
        response.setPaymentMethod("CASH");

        when(serviceRecordService.updatePaymentStatus(eq(1L), eq("SUCCESS"), eq("CASH"))).thenReturn(response);

        String token = generateToken(10L, "tech@example.com", "TECHNICIAN");

        mockMvc.perform(put("/api/records/1/payment-status")
                .header("Authorization", "Bearer " + token)
                .param("status", "SUCCESS")
                .param("method", "CASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"));
    }
}
