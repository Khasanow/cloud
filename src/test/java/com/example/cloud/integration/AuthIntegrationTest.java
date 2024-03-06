package com.example.cloud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.cloud.dto.AuthRequest;
import com.example.cloud.entity.Role;
import com.example.cloud.entity.User;
import com.example.cloud.repositories.UserRepository;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest {
    private static final String AUTH_ENDPOINT = "/login";
    private static final String LOGIN = "testLogin";
    private static final String PASSWORD = "testPassword";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    private static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url=", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username=", POSTGRES::getUsername);
        registry.add("spring.datasource.password=", POSTGRES::getPassword);
    }

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        User user = User.builder()
                .login(LOGIN)
                .password(encoder.encode(PASSWORD))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        System.out.println("Новый юзер");
    }

    @AfterEach
    public void clean() {
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateAuthToken() throws Exception {
        AuthRequest request = new AuthRequest(LOGIN, PASSWORD);

        MockHttpServletRequestBuilder requestPost = post(AUTH_ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        String authToken = mockMvc.perform(requestPost)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertFalse(authToken.isEmpty());
        assertTrue(authToken.contains("auth-token"));

    }

    @ParameterizedTest
    @MethodSource("arguments")
    void testCreateAuthTokenBadRequest(String login, String password) throws Exception {
        AuthRequest request = new AuthRequest(login, password);

        MockHttpServletRequestBuilder requestPost = post(AUTH_ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestPost)
                .andExpect(status().isBadRequest());
    }

    public static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of(LOGIN, "badPassword"),
                Arguments.of("", PASSWORD),
                Arguments.of(LOGIN, ""),
                Arguments.of("", ""),
                Arguments.of(null, null)
        );
    }
}



