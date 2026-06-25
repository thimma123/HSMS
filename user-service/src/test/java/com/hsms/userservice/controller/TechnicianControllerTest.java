package com.hsms.userservice.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.hsms.userservice.enums.AvailabilityStatus;
import com.hsms.userservice.model.TechnicianDetailResponseDTO;
import com.hsms.userservice.model.TechnicianProfileRequestDTO;
import com.hsms.userservice.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class TechnicianControllerTest {

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
    public void testCreateTechnician() throws Exception {
        TechnicianProfileRequestDTO dto = new TechnicianProfileRequestDTO();
        dto.setSkill("Plumbing");
        dto.setExperience(5);
        dto.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        TechnicianDetailResponseDTO response = new TechnicianDetailResponseDTO();
        response.setUserId(2L);
        response.setName("Bob Tech");
        response.setEmail("tech@example.com");
        response.setSkill("Plumbing");

        when(userService.createTechnician(any(TechnicianProfileRequestDTO.class)))
                .thenReturn(response);

        String token = generateToken(2L, "tech@example.com", "TECHNICIAN");

        mockMvc.perform(post("/api/technicians")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.name").value("Bob Tech"));
    }

    @Test
    public void testGetTechnician() throws Exception {
        TechnicianDetailResponseDTO response = new TechnicianDetailResponseDTO();
        response.setUserId(2L);
        response.setName("Bob Tech");
        response.setEmail("tech@example.com");

        when(userService.getTechnician(2L)).thenReturn(response);

        String token = generateToken(2L, "tech@example.com", "TECHNICIAN");

        mockMvc.perform(get("/api/technicians/2")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.name").value("Bob Tech"));
    }
}
