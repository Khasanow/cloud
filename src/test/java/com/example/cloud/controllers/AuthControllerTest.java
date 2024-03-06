package com.example.cloud.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.cloud.dto.AuthRequest;
import com.example.cloud.dto.AuthResponse;
import com.example.cloud.service.AuthService;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    private MockMvc mockMvc;
    @Mock
    private AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testCreateAuthToken() throws Exception {
        AuthRequest authRequest = new AuthRequest("username", "password");
        AuthResponse login = new AuthResponse("token");

        when(authService.createAuthToken(authRequest)).thenReturn(login);

        mockMvc.perform(post("/login")
                        .content(objectMapper.writeValueAsString(authRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authService, times(1)).createAuthToken(authRequest);
    }
}


