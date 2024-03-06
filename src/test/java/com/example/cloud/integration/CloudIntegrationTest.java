package com.example.cloud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.cloud.dto.AuthRequest;
import com.example.cloud.dto.FileDTO;
import com.example.cloud.dto.PutRequest;
import com.example.cloud.entity.Role;
import com.example.cloud.entity.User;
import com.example.cloud.repositories.FileRepository;
import com.example.cloud.repositories.UserRepository;
import com.example.cloud.service.StorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CloudIntegrationTest {

    private static final String AUTH_ENDPOINT = "/login";
    private static final String ENDPOINT_FILE = "/file";
    private static final String ENDPOINT_LIST = "/list";
    private static final String BAD_REQUEST_MESSAGE = "Error input data";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Error upload file";
    private static final File IMAGE = new File("./src/test/resources/testFile.jpg");
    private static final String FILE_NAME = "testFile.jpg";
    private static final String TEST_LOGIN = "testLogin";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String PARAM_NAME = "filename";
    private static final String HEADER_NAME = "auth-token";
    private static final String BEARER = "Bearer ";
    private static final String PATH_TO_TEST_FILE = "/cloudStore/testLogin/testFile.jpg";
    private static final String PATH_TO_RENAME_TEST_FILE = "/cloudStore/testLogin/newFileName.txt";
    private static final String NEW_FILE_NAME = "newFileName.txt";
    private static final String PARAM_LIMIT = "limit";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres").withDatabaseName("postgres").withUsername("postgres").withPassword("postgres");

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
    FileRepository fileRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    StorageService storageService;
    private String authToken;


    @BeforeEach
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        User user = User.builder().login(TEST_LOGIN).password(encoder.encode(TEST_PASSWORD)).role(Role.USER).build();
        userRepository.save(user);

        AuthRequest request = new AuthRequest(TEST_LOGIN, TEST_PASSWORD);

        MockHttpServletRequestBuilder requestPost = post(AUTH_ENDPOINT)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON);

        authToken = mockMvc.perform(requestPost)
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString()
                .replace("{\"auth-token\":\"", BEARER);

        authToken = authToken.replace("\"}", "");
    }

    @AfterEach
    public void clean() {
        userRepository.deleteAll();
        fileRepository.deleteAll();
        SecurityContextHolder.clearContext();
        new File(PATH_TO_TEST_FILE).delete();
        new File(PATH_TO_RENAME_TEST_FILE).delete();
    }

    private static MockMultipartFile getFile() throws IOException {
        return new MockMultipartFile(
                "file",
                FILE_NAME,
                MediaType.IMAGE_JPEG_VALUE,
                new FileInputStream(IMAGE));
    }

    @Test
    void testUploadFileValidData() throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(ENDPOINT_FILE).file(getFile());

        MockHttpServletRequestBuilder request = requestBuilder
                .header(HEADER_NAME, authToken)
                .param(PARAM_NAME, FILE_NAME)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request).andExpect(status().isOk());

        Optional<com.example.cloud.entity.File> file = fileRepository.findByFileNameAndUserLogin(FILE_NAME, TEST_LOGIN);
        assertTrue(file.isPresent());
        assertTrue(new File(PATH_TO_TEST_FILE).isFile());
    }

    @Test
    void testUploadFileNoFileName() throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(ENDPOINT_FILE).file(getFile());

        MockHttpServletRequestBuilder request = requestBuilder
                .header(HEADER_NAME, authToken)
                .param(PARAM_NAME, "")
                .contentType(MediaType.MULTIPART_FORM_DATA);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains(BAD_REQUEST_MESSAGE));
        Optional<com.example.cloud.entity.File> file = fileRepository.findByFileNameAndUserLogin(FILE_NAME, TEST_LOGIN);
        assertFalse(file.isPresent());
        assertFalse(new File(PATH_TO_TEST_FILE).isFile());
    }


    @Test
    void testUploadFileNoFile() throws Exception {
        MockHttpServletRequestBuilder request = post(ENDPOINT_FILE)
                .header(HEADER_NAME, authToken)
                .param(PARAM_NAME, FILE_NAME)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        Optional<com.example.cloud.entity.File> file = fileRepository.findByFileNameAndUserLogin(FILE_NAME, TEST_LOGIN);
        assertFalse(file.isPresent());
        assertFalse(new File(PATH_TO_TEST_FILE).isFile());
    }

    @Test
    void testUploadFileUnauthorizedRequest() throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(ENDPOINT_FILE).file(getFile());

        MockHttpServletRequestBuilder request = requestBuilder
                .header(HEADER_NAME, authToken.replace(BEARER, " "))
                .param(PARAM_NAME, FILE_NAME)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteFileValidData() throws Exception {
        storageService.saveFile(TEST_LOGIN, FILE_NAME.getBytes(), FILE_NAME);
        mockMvc.perform(delete(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isOk());

        Optional<com.example.cloud.entity.File> file = fileRepository.findByFileNameAndUserLogin(FILE_NAME, TEST_LOGIN);
        assertTrue(file.isPresent());
        assertTrue(file.get().isItsRemoved());

        assertTrue(new File(PATH_TO_TEST_FILE).isFile());
    }

    @Test
    void testDeleteFileUnauthorizedRequest() throws Exception {
        mockMvc.perform(delete(ENDPOINT_FILE).param(PARAM_NAME, FILE_NAME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteFileInputDataException() throws Exception {
        String response = mockMvc.perform(delete(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse()
                .getContentAsString();

        assertTrue(response.contains(BAD_REQUEST_MESSAGE));
    }

    @Test
    void testDownloadFileValidData() throws Exception {
        storageService.saveFile(TEST_LOGIN, FILE_NAME.getBytes(), FILE_NAME);

        byte[] response = mockMvc.perform(get(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsByteArray();

        assertArrayEquals(response, FILE_NAME.getBytes());
    }

    @Test
    void testDownloadFileUnauthorizedRequest() throws Exception {
        mockMvc.perform(get(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDownloadFileNoFile() throws Exception {
        String response = mockMvc.perform(get(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse()
                .getContentAsString();

        assertTrue(response.contains(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    void testDownloadFileNoFileName() throws Exception {
        String response = mockMvc.perform(get(ENDPOINT_FILE)
                        .param(PARAM_NAME, "")
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse()
                .getContentAsString();

        assertTrue(response.contains(BAD_REQUEST_MESSAGE));
    }

    @Test
    void testEditFileNameValidData() throws Exception {
        storageService.saveFile(TEST_LOGIN, FILE_NAME.getBytes(), FILE_NAME);
        PutRequest putRequest = new PutRequest(NEW_FILE_NAME);

        mockMvc.perform(put(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .content(objectMapper.writeValueAsString(putRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isOk());

        Optional<com.example.cloud.entity.File> file = fileRepository
                .findByFileNameAndUserLogin(NEW_FILE_NAME, TEST_LOGIN);
        assertTrue(file.isPresent());

        assertTrue(new File(PATH_TO_RENAME_TEST_FILE).isFile());
    }

    @Test
    void testEditFileNameUnauthorizedRequest() throws Exception {
        PutRequest putRequest = new PutRequest(NEW_FILE_NAME);

        mockMvc.perform(put(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .content(objectMapper.writeValueAsString(putRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEditFileNameNoFile() throws Exception {
        PutRequest putRequest = new PutRequest(NEW_FILE_NAME);

        mockMvc.perform(put(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .content(objectMapper.writeValueAsString(putRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isInternalServerError());

        Optional<com.example.cloud.entity.File> file = fileRepository
                .findByFileNameAndUserLogin(NEW_FILE_NAME, TEST_LOGIN);
        assertFalse(file.isPresent());

        assertFalse(new File(PATH_TO_RENAME_TEST_FILE).isFile());
    }

    @Test
    void testEditFileNameNoFileName() throws Exception {
        PutRequest putRequest = new PutRequest(NEW_FILE_NAME);

        String response = mockMvc.perform(put(ENDPOINT_FILE)
                        .param(PARAM_NAME, "")
                        .content(objectMapper.writeValueAsString(putRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains(BAD_REQUEST_MESSAGE));
        Optional<com.example.cloud.entity.File> file = fileRepository
                .findByFileNameAndUserLogin(NEW_FILE_NAME, TEST_LOGIN);
        assertFalse(file.isPresent());

        assertFalse(new File(PATH_TO_RENAME_TEST_FILE).isFile());
    }

    @Test
    void testEditFileNameNoNewFileName() throws Exception {
        PutRequest putRequest = new PutRequest("");

        String response = mockMvc.perform(put(ENDPOINT_FILE)
                        .param(PARAM_NAME, FILE_NAME)
                        .content(objectMapper.writeValueAsString(putRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_NAME, authToken))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains(BAD_REQUEST_MESSAGE));
        Optional<com.example.cloud.entity.File> file = fileRepository
                .findByFileNameAndUserLogin(NEW_FILE_NAME, TEST_LOGIN);
        assertFalse(file.isPresent());

        assertFalse(new File(PATH_TO_RENAME_TEST_FILE).isFile());
    }


    @Test
    void testGetFileListValidData() throws Exception {

        storageService.saveFile(TEST_LOGIN, FILE_NAME.getBytes(), FILE_NAME);
        storageService.saveFile(TEST_LOGIN, NEW_FILE_NAME.getBytes(), NEW_FILE_NAME);

        String response = mockMvc.perform(get(ENDPOINT_LIST)
                        .header(HEADER_NAME, authToken)
                        .param(PARAM_LIMIT, String.valueOf(3)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, FileDTO.class);
        List<FileDTO> fileDTOS = objectMapper.readValue(response, collectionType);

        assertFalse(fileDTOS.isEmpty());
        assertEquals(2, fileDTOS.size());

        assertEquals(fileDTOS.get(1).getFilename(), FILE_NAME);
        assertEquals(fileDTOS.get(0).getFilename(), NEW_FILE_NAME);
    }

    @Test
    void testGetFileListUnauthorizedRequest() throws Exception {
        mockMvc.perform(get(ENDPOINT_LIST)
                        .param(PARAM_LIMIT, String.valueOf(3)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetFileListNullLimit() throws Exception {
        String response = mockMvc.perform(get(ENDPOINT_LIST)
                        .header(HEADER_NAME, authToken)
                        .param(PARAM_LIMIT, String.valueOf(0)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertTrue(response.contains(BAD_REQUEST_MESSAGE));
    }

    @Test
    void testGetFileListEmptyList() throws Exception {

        String response = mockMvc.perform(get(ENDPOINT_LIST)
                        .header(HEADER_NAME, authToken)
                        .param(PARAM_LIMIT, String.valueOf(3)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, FileDTO.class);
        List<FileDTO> fileDTOS = objectMapper.readValue(response, collectionType);

        assertTrue(fileDTOS.isEmpty());
    }
}
