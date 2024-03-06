package com.example.cloud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.cloud.entity.File;
import com.example.cloud.exceptions.InputDataException;
import com.example.cloud.repositories.CloudRepository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {StorageService.class})
@ExtendWith(SpringExtension.class)
class StorageServiceTest {
    @MockBean
    private CloudRepository cloudRepository;

    @MockBean
    private FileService fileService;

    @Autowired
    private StorageService storageService;

    @Test
    void testSaveFile() {
        doNothing().when(cloudRepository)
                .saveFileToRepository(Mockito.<byte[]>any(), Mockito.<String>any(), Mockito.<String>any());
        doNothing().when(fileService).save(Mockito.<File>any());
        storageService.saveFile("testUser", "test.txt".getBytes(), "test.txt");
        verify(cloudRepository).saveFileToRepository(Mockito.<byte[]>any(), Mockito.<String>any(), Mockito.<String>any());
        verify(fileService).save(Mockito.<File>any());
    }

    @Test
    void testSaveFileInputDataException() {
        assertThrows(InputDataException.class,
                () -> storageService.saveFile("testUser", "test.txt".getBytes(), null));
    }

    @Test
    void testDeleteFileValidData() {
        doNothing().when(fileService).deleteByFileNameAndUsername(Mockito.<String>any(), Mockito.<String>any());
        storageService.deleteFile("testUser", "test.txt");
        verify(fileService).deleteByFileNameAndUsername(Mockito.<String>any(), Mockito.<String>any());
    }

    @Test
    void testDeleteFileInputDataException() {
        assertThrows(InputDataException.class,
                () -> storageService.deleteFile("testUser", null));
    }

    @Test
    void testDownloadFile() {
        when(cloudRepository.downloadFileFromRepository(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("test.txt".getBytes());
        byte[] actualDownloadFileResult = storageService.downloadFile("testUser", "test.txt");
        assertArrayEquals(actualDownloadFileResult, "test.txt".getBytes());
        verify(cloudRepository).downloadFileFromRepository(Mockito.<String>any(), Mockito.<String>any());
    }

    @Test
    void testDownloadFileInputDataException() {
        assertThrows(InputDataException.class,
                () -> storageService.downloadFile("testUser", null));
    }

    @Test
    void testRenameFile() {
        doNothing().when(cloudRepository).renameFile(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any());
        doNothing().when(fileService).renameFile(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any());
        storageService.renameFile("testUser", "test.txt", "newFileName.txt");
        verify(cloudRepository).renameFile(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any());
        verify(fileService).renameFile(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any());
    }

    @Test
    void testRenameFileInvalidFileName() {
        assertThrows(InputDataException.class,
                () -> storageService.renameFile("testUser", null, "newFileName.txt"));
    }

    @Test
    void testRenameFileInvalidNewFileName() {
        assertThrows(InputDataException.class,
                () -> storageService.renameFile("testUser", "test.txt", null));
    }
}

