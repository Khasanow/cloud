package com.example.cloud.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import com.example.cloud.entity.File;
import com.example.cloud.exceptions.InputDataException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestPropertySource(locations = "classpath:application.properties")
@DataJpaTest
class FileRepositoryTest {
    @Autowired
    private FileRepository fileRepository;
    File testFile;

    @BeforeEach
    void setUp() {
        testFile = new File("test.txt", 256, "testUser");
        fileRepository.save(testFile);
    }

    @AfterEach
    void tearDown() {
        fileRepository.deleteAll();
    }

    @Test
    void testDeleteByFileNameAndUsernameValidData() {
        // when
        fileRepository.deleteByFileNameAndUsername("test.txt", "testUser");
        // then
        Optional<File> file = fileRepository.findByFileNameAndUserLogin(
                "test.txt", "testUser");
        assertTrue(file.isPresent());
        assertThat(file.get().isItsRemoved()).isTrue();
    }

    @Test
    void testDeleteByFileNameAndUsernameInputDataException() {
        assertThatThrownBy(() -> fileRepository.deleteByFileNameAndUsername("test1.txt", "testUser1"))
                .isInstanceOf(InputDataException.class)
                .hasMessageContaining("Error input data");
    }


    @ParameterizedTest
    @MethodSource("arguments")
    void testFindByFileNameAndUserLogin(String fileName, String username, int valid) {
        if (valid == 1 && fileName != null) {
            // when
            Optional<File> foundFile = fileRepository.findByFileNameAndUserLogin("test.txt", "testUser");
            // then
            if (foundFile.isPresent()) {
                File file = foundFile.get();
                assertThat(file).isEqualTo(testFile);
            }
        } else {
            assertThat(fileRepository.findByFileNameAndUserLogin(fileName, username)).isEmpty();
        }
    }

    public static Stream<Arguments> arguments() {
        return Stream.of(
                Arguments.of("test.txt", "testUser", 1),
                Arguments.of("test1.txt", "testUser1", 0),
                Arguments.of(null, "testUser", 1),
                Arguments.of("test.txt", null, 0),
                Arguments.of(null, null, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("arguments")
    void testFindFileByUserLogin(String fileName, String username, int size) {
        // when
        List<File> files = fileRepository.findFilesByUserLogin(username);
        // then
        assertThat(files.size()).isEqualTo(size);
        if (size != 0 && fileName != null) {
            assertThat(files.get(0).getFileName()).isEqualTo(fileName);
        }
    }

    @Test
    void testFindFilesByUsernameWithLimit3() {
        // given
        File testFile2 = new File("test2.txt", 256, "testUser");
        File testFile3 = new File("test3.txt", 256, "testUser");
        File testFile4 = new File("test4.txt", 256, "testUser");
        File testFile5 = new File("test5.txt", 256, "testUser");
        fileRepository.save(testFile2);
        fileRepository.save(testFile3);
        fileRepository.save(testFile4);
        fileRepository.save(testFile5);

        // when
        List<File> files = fileRepository.findFileListByUsername("testUser", 3);

        // then
        assertThat(files.size()).isEqualTo(3);
    }

    @Test
    void testFindFilesByUsernameWithRemovedFiles() {
        // given
        File testFile2 = new File("test2.txt", 256, "testUser");
        testFile2.setItsRemoved(true);
        File testFile3 = new File("test3.txt", 256, "testUser");
        fileRepository.save(testFile2);
        fileRepository.save(testFile3);
        // when
        List<File> files = fileRepository.findFileListByUsername("testUser", 3);
        // then
        assertThat(files.size()).isEqualTo(2);
    }

    @Test
    void testFindFilesByUsernameInvalidUser() {
        // when
        List<File> files = fileRepository.findFileListByUsername("testUser1", 3);
        // then
        assertThat(files).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("argumentsByRenameTest")
    void testRenameFile(String fileName, String newFileName, String username, boolean exception) {
        if (!exception) {
            // when
            fileRepository.renameFile(fileName, newFileName, username);
            // then
            assertThat(fileRepository.findByFileNameAndUserLogin(newFileName, username)).isPresent();
        } else {
            assertThatThrownBy(() -> fileRepository.renameFile(fileName, newFileName, username))
                    .isInstanceOf(InputDataException.class)
                    .hasMessageContaining("Error input data");
        }

    }

    public static Stream<Arguments> argumentsByRenameTest() {
        return Stream.of(
                Arguments.of("test.txt", "testNewName.txt", "testUser", false),
                Arguments.of("test1.txt", "testNewName.txt", "testUser", true),
                Arguments.of(null, "testNewName.txt", "testUser", true),
                Arguments.of("test.txt", "testNewName.txt", "testUser1", true),
                Arguments.of("test.txt", "testNewName.txt", null, true),
                Arguments.of(null, null, null, true)
        );
    }


}

