package com.hsms.authservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
import com.hsms.authservice.model.LoginRequestDTO;
import com.hsms.authservice.model.LoginResponseDTO;
import com.hsms.authservice.model.RegisterRequestDTO;
import com.hsms.authservice.model.RegisterResponseDTO;
import com.hsms.authservice.service.AuthService;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegister() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");
        dto.setRole("CUSTOMER");

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setUserId(1L);
        response.setMessage("Hello John");

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.message").value("Hello John"));
    }

    @Test
    public void testLogin() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken("dummy_token");
        response.setUserId(1L);
        response.setRole("CUSTOMER");

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummy_token"))
                .andExpect(jsonPath("$.userId").value(1));
    }
}
