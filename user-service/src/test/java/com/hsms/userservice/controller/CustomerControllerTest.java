package com.hsms.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsms.userservice.model.CustomerDetailResponseDTO;
import com.hsms.userservice.model.CustomerProfileRequestDTO;
import com.hsms.userservice.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
    public void testCreateCustomer() throws Exception {
        CustomerProfileRequestDTO dto = new CustomerProfileRequestDTO();
        dto.setAddress("123 Main St");
        dto.setCity("Metropolis");
        dto.setPincode("123456");

        CustomerDetailResponseDTO response = new CustomerDetailResponseDTO();
        response.setUserId(1L);
        response.setName("John Doe");
        response.setEmail("john@example.com");
        response.setAddress("123 Main St");
        response.setCity("Metropolis");
        response.setPincode("123456");

        when(userService.createCustomer(any(CustomerProfileRequestDTO.class), eq(1L), eq("john@example.com")))
                .thenReturn(response);

        String token = generateToken(1L, "john@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/customers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testGetCustomer() throws Exception {
        CustomerDetailResponseDTO response = new CustomerDetailResponseDTO();
        response.setUserId(1L);
        response.setName("John Doe");
        response.setEmail("john@example.com");

        when(userService.getCustomer(1L)).thenReturn(response);

        String token = generateToken(1L, "john@example.com", "CUSTOMER");

        mockMvc.perform(get("/api/customers/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
