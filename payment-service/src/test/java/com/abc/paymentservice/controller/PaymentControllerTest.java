package com.abc.paymentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.abc.paymentservice.model.PaymentRequestDTO;
import com.abc.paymentservice.model.PaymentResponseDTO;
import com.abc.paymentservice.service.PaymentService;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

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
     void testCreatePayment() throws Exception {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setServiceRequestId(1L);
        dto.setAmount(100.0);
        dto.setPaymentMethod("CARD");
        dto.setCustomerId(5L);

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(1L);
        response.setBookingId(1L);
        response.setAmount(100.0);
        response.setStatus("SUCCESS");

        when(paymentService.createPayment(any(PaymentRequestDTO.class))).thenReturn(response);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/payments/save")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
     void testGetPaymentById() throws Exception {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(1L);
        response.setBookingId(1L);
        response.setStatus("SUCCESS");

        when(paymentService.getPaymentById(eq(1L))).thenReturn(response);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(get("/api/payments/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
     void testUpdatePaymentStatus() throws Exception {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(1L);
        response.setBookingId(1L);
        response.setStatus("SUCCESS");
        response.setPaymentMethod("CASH");

        when(paymentService.updatePaymentStatus(eq(1L), any(com.abc.paymentservice.enums.PaymentStatus.class), eq("CASH")))
                .thenReturn(response);

        String token = generateToken(5L, "technician@example.com", "TECHNICIAN");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/payments/status/1")
                .header("Authorization", "Bearer " + token)
                .param("status", "SUCCESS")
                .param("method", "CASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"));
    }
}
