package com.example.cloud.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import com.example.cloud.dto.FileDTO;
import com.example.cloud.dto.PutRequest;
import com.example.cloud.service.FileService;
import com.example.cloud.service.StorageService;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CloudControllerTest {
    private StorageService storageService;
    private FileService fileService;
    private CloudController cloudController;
    Principal principal;


    @BeforeEach
    public void setup() {
        storageService = mock(StorageService.class);
        fileService = mock(FileService.class);
        principal = mock(Principal.class);
        cloudController = new CloudController(storageService, fileService);
    }

    @Test
    void testUploadFile() throws Exception {
        //given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());
        // when
        when(principal.getName()).thenReturn("user");
        doNothing().when(storageService).saveFile(Mockito.any(), Mockito.any(), Mockito.any());
        cloudController.uploadFile(principal, "test.txt",
                new MockMultipartFile("test.txt", new ByteArrayInputStream("test.txt".getBytes())));
        // then
        verify(storageService, times(1)).saveFile(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testDeleteFile() {
        // when
        doNothing().when(storageService).deleteFile(Mockito.any(), Mockito.any());
        cloudController.deleteFile(principal, "test.txt");
        // then
        verify(storageService, times(1)).deleteFile(Mockito.any(), Mockito.any());
    }

    @Test
    void testDownloadFile() {
        // when
        when(storageService.downloadFile(Mockito.any(), Mockito.any())).thenReturn("test.txt".getBytes());
        byte[] actual = cloudController.downloadFile(principal, "test.txt");
        // then
        assertArrayEquals(actual, "test.txt".getBytes());
        verify(storageService, times(1)).downloadFile(Mockito.any(), Mockito.any());
    }

    @Test
    void testEditFileName() {
        // given
        PutRequest putRequest = new PutRequest("newFileName.txt");
        // when
        doNothing().when(storageService).renameFile(Mockito.any(), Mockito.any(), Mockito.any());
        cloudController.editFileName(principal, "test.txt", putRequest);
        // then
        verify(storageService, times(1)).renameFile(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void testGetFileList() {
        // given
        List<FileDTO> files = new ArrayList<>();
        FileDTO fileDTO = new FileDTO("test.txt", 256);
        files.add(fileDTO);
        // when
        when(fileService.listFiles(principal.getName(), 3)).thenReturn(files);
        List<FileDTO> actual = cloudController.getFileList(principal, 3);
        // then
        assertEquals(actual.size(), 1);
        assertEquals(fileDTO, actual.get(0));
        verify(fileService, times(1)).listFiles(Mockito.any(), Mockito.anyInt());
    }
}

