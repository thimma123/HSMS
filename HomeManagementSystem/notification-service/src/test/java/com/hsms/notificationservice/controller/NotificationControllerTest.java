package com.hsms.notificationservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsms.notificationservice.dto.NotificationDTO;
import com.hsms.notificationservice.entity.Notification;
import com.hsms.notificationservice.service.NotificationService;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

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
    public void testSaveNotification() throws Exception {
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(5L);
        dto.setMessage("Test notification message");
        dto.setStatus("PENDING");

        Notification response = new Notification();
        response.setNotificationId(1L);
        response.setUserId(5L);
        response.setMessage("Test notification message");
        response.setStatus("PENDING");

        when(notificationService.saveNotification(any(Notification.class))).thenReturn(response);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/notifications")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testGetNotificationById() throws Exception {
        Notification response = new Notification();
        response.setNotificationId(1L);
        response.setMessage("Notification 1");

        when(notificationService.getNotificationById(eq(1L))).thenReturn(response);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(get("/api/notifications/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(1))
                .andExpect(jsonPath("$.message").value("Notification 1"));
    }
}
