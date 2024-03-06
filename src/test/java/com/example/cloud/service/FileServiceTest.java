package com.example.cloud.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.example.cloud.dto.FileDTO;
import com.example.cloud.entity.File;
import com.example.cloud.exceptions.InputDataException;
import com.example.cloud.repositories.FileRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {FileService.class})
@ExtendWith(SpringExtension.class)
class FileServiceTest {
    @MockBean
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    @Test
    void testDeleteByFileNameAndUsername() {
        // when
        doNothing().when(fileRepository).deleteByFileNameAndUsername(Mockito.any(), Mockito.any());
        fileService.deleteByFileNameAndUsername("test.txt", "testUser");
        // then
        verify(fileRepository).deleteByFileNameAndUsername(Mockito.any(), Mockito.any());
    }

    @Test
    void testSave() {
        // when
        when(fileRepository.save(Mockito.any())).thenReturn(new File("test.txt", 3, "User Login"));
        File file = new File("test.txt", 3, "testUser");
        fileService.save(file);
        // then
        verify(fileRepository).save(Mockito.any());
    }

    @Test
    void testListFilesEmptyList() {
        // when
        when(fileRepository.findFileListByUsername(Mockito.any(), anyInt())).thenReturn(new ArrayList<>());
        // then
        assertTrue(fileService.listFiles("testUser", 1).isEmpty());
    }

    @Test
    void testListFilesValidData() {
        // given
        ArrayList<File> fileList = new ArrayList<>();
        fileList.add(new File("test.txt", 3, "testUser"));
        // when
        when(fileRepository.findFileListByUsername(Mockito.any(), anyInt())).thenReturn(fileList);
        List<FileDTO> actualListFilesResult = fileService.listFiles("testUser", 1);
        // then
        assertEquals(1, actualListFilesResult.size());
        FileDTO getResult = actualListFilesResult.get(0);
        assertEquals("test.txt", getResult.getFilename());
        assertEquals(3, getResult.getSize());
    }


    @Test
    void testListFilesInputDataException() {
        // then
        assertThrows(InputDataException.class, () -> fileService.listFiles("testUser", -1));
    }

    @Test
    void testRenameFile() {
        doNothing().when(fileRepository).renameFile(Mockito.any(), Mockito.any(), Mockito.any());
        fileService.renameFile("foo.txt", "test.txt", "testUser");
        verify(fileRepository).renameFile(Mockito.any(), Mockito.any(), Mockito.any());
    }

}

