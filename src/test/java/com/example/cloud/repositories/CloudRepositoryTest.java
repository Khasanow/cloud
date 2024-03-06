package com.example.cloud.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.cloud.exceptions.FileException;
import com.example.cloud.exceptions.InputDataException;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudRepositoryTest {

    private CloudRepository cloudRepository;
    String fileName;
    byte[] file;
    String login;

    @BeforeEach
    void setUp() {
        cloudRepository = new CloudRepository();
        fileName = "test.txt";
        file = fileName.getBytes();
        login = "testUser";
    }


    @Test
    public void saveFileToRepositoryTest() {
        // when
        cloudRepository.saveFileToRepository(file, fileName, login);
        // then
        File newFile = new File("nulltestUser/test.txt");
        assertThat(newFile.exists()).isTrue();
        newFile.delete();
    }

    @Test
    public void saveFileToRepositoryTestInputDataException() {
        // given
        cloudRepository.saveFileToRepository(file, fileName, login);
        // then
        File newFile = new File("nulltestUser/test.txt");
        assertThatThrownBy(() -> cloudRepository.saveFileToRepository(file, fileName, login))
                .isInstanceOf(InputDataException.class)
                .hasMessageContaining("Error input data");
        newFile.delete();
    }


    @Test
    void renameFile() {
        // given
        String newFileName = "newFileName.txt";
        cloudRepository.saveFileToRepository(file, fileName, login);
        // when
        cloudRepository.renameFile(fileName, newFileName, login);
        // then
        File newFile = new File("nulltestUser/newFileName.txt");
        assertThat(newFile.exists()).isTrue();
        newFile.delete();
    }

    @Test
    void renameFileFileException() {
        // given
        String newFileName = "newFileName.txt";
        // then
        File newFile = new File("nulltestUser/newFileName.txt");
        assertThat(newFile.exists()).isFalse();
        assertThatThrownBy(() -> cloudRepository.renameFile(fileName, newFileName, login))
                .isInstanceOf(FileException.class)
                .hasMessageContaining("Error upload file");
    }

    @Test
    void renameFileFileExceptionInvalidNewFileName() {
        // given
        String newFileName = "";
        cloudRepository.saveFileToRepository(file, fileName, login);
        // then
        File newFile = new File("nulltestUser/newFileName.txt");
        File oldFile = new File("nulltestUser/test.txt");
        assertThat(oldFile.exists()).isTrue();
        assertThat(newFile.exists()).isFalse();
        assertThatThrownBy(() -> cloudRepository.renameFile(fileName, newFileName, login))
                .isInstanceOf(FileException.class)
                .hasMessageContaining("Error upload file");
        oldFile.delete();
    }


    @Test
    void downloadFileFromRepository() {
        // given
        cloudRepository.saveFileToRepository(file, fileName, login);
        // when
        byte[] file1 = cloudRepository.downloadFileFromRepository(fileName, login);
        // then
        assertThat(file1).isEqualTo(file);
        new File("nulltestUser/test.txt").delete();
    }

    @Test
    void downloadFileFromRepositoryFileException() {
        // then
        assertThatThrownBy(() -> cloudRepository.downloadFileFromRepository(fileName, login))
                .isInstanceOf(FileException.class)
                .hasMessageContaining("Error upload file");
    }
}