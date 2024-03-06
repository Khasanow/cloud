package com.example.cloud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.cloud.entity.User;
import com.example.cloud.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {UserService.class})
@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void testLoadUserByUsername() throws UsernameNotFoundException {
        User user = new User();
        when(userRepository.findByLogin(Mockito.<String>any())).thenReturn(Optional.of(user));
        assertSame(user, userService.loadUserByUsername("testLogin"));
        verify(userRepository).findByLogin(Mockito.<String>any());
    }


    @Test
    void testLoadUserByUsername3() {
        when(userRepository.findByLogin(Mockito.<String>any()))
                .thenThrow(new UsernameNotFoundException("Bad credentials"));
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("testLogin"));
        verify(userRepository).findByLogin(Mockito.<String>any());
    }
}

