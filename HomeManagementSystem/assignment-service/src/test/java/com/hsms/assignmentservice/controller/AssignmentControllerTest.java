package com.hsms.assignmentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsms.assignmentservice.model.AssignmentRequestDTO;
import com.hsms.assignmentservice.model.AssignmentResponseDTO;
import com.hsms.assignmentservice.service.AssignmentService;

@SpringBootTest
@AutoConfigureMockMvc
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssignmentService assignmentService;

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
    public void testAssignTechnician() throws Exception {
        AssignmentRequestDTO dto = new AssignmentRequestDTO();
        dto.setServiceRequestId(1L);
        dto.setTechnicianId(10L);
        dto.setUserId(5L);

        AssignmentResponseDTO response = new AssignmentResponseDTO();
        response.setId(1L);
        response.setServiceRequestId(1L);
        response.setTechnicianId(10L);
        response.setStatus("ASSIGNED");

        when(assignmentService.assignTechnician(any(AssignmentRequestDTO.class)))
                .thenReturn(response);

        String token = generateToken(4L, "manager@example.com", "SERVICE_MANAGER");

        mockMvc.perform(post("/api/assignments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    public void testAcceptJob() throws Exception {
        AssignmentResponseDTO response = new AssignmentResponseDTO();
        response.setId(1L);
        response.setStatus("ACCEPTED");

        when(assignmentService.acceptJob(eq(1L))).thenReturn(response);

        String token = generateToken(10L, "tech@example.com", "TECHNICIAN");

        mockMvc.perform(put("/api/assignments/1/accept")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}
