package com.example.cloud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.cloud.dto.AuthRequest;
import com.example.cloud.entity.User;
import com.example.cloud.jwt.JwtTokenUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {AuthService.class})
@ExtendWith(SpringExtension.class)
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenUtils jwtTokenUtils;

    @MockBean
    private UserService userService;

    @Test
    void testCreateAuthToken() {
        when(userService.loadUserByUsername(Mockito.<String>any())).thenReturn(new User());
        when(jwtTokenUtils.generateToken(Mockito.<UserDetails>any())).thenReturn("ABC123");
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenReturn(new TestingAuthenticationToken("Principal", "Credentials"));
        assertEquals("ABC123", authService.createAuthToken(new AuthRequest("Login", "iloveyou")).getAuthToken());
        verify(userService).loadUserByUsername(Mockito.<String>any());
        verify(jwtTokenUtils).generateToken(Mockito.<UserDetails>any());
        verify(authenticationManager).authenticate(Mockito.<Authentication>any());
    }

    @Test
    void testCreateAuthTokenBadCredentialsException() {
        when(authenticationManager.authenticate(Mockito.<Authentication>any()))
                .thenThrow(new BadCredentialsException("Msg"));
        assertThrows(com.example.cloud.exceptions.AuthenticationException.class,
                () -> authService.createAuthToken(new AuthRequest("Login", "iloveyou")));
        verify(authenticationManager).authenticate(Mockito.<Authentication>any());
    }

}

