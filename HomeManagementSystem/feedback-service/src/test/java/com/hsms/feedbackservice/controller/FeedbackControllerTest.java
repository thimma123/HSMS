package com.hsms.feedbackservice.controller;

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
import com.hsms.feedbackservice.dto.FeedbackDTO;
import com.hsms.feedbackservice.entity.Feedback;
import com.hsms.feedbackservice.service.FeedbackService;

@SpringBootTest
@AutoConfigureMockMvc
public class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedbackService feedbackService;

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
    public void testSaveFeedback() throws Exception {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setUserId(5L);
        dto.setServiceRequestId(1L);
        dto.setRating(5);
        dto.setComments("Great service!");

        Feedback feedbackResponse = new Feedback();
        feedbackResponse.setFeedbackId(1L);
        feedbackResponse.setServiceRequestId(1L);
        feedbackResponse.setRating(5);
        feedbackResponse.setComments("Great service!");

        when(feedbackService.saveFeedback(any(Feedback.class))).thenReturn(feedbackResponse);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/feedback")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackId").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    public void testGetFeedbackById() throws Exception {
        Feedback feedbackResponse = new Feedback();
        feedbackResponse.setFeedbackId(1L);
        feedbackResponse.setRating(5);

        when(feedbackService.getFeedbackById(eq(1L))).thenReturn(feedbackResponse);

        String token = generateToken(5L, "customer@example.com", "CUSTOMER");

        mockMvc.perform(get("/api/feedback/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedbackId").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }
}
