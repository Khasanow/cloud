package com.example.cloud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cloud.entity.File;
import com.example.cloud.exceptions.InputDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    default void deleteByFileNameAndUsername(String fileName, String username) {
        File file = findByFileNameAndUserLogin(fileName, username).orElseThrow(() ->
                new InputDataException("Error input data"));
        file.setItsRemoved(true);
        save(file);
    }

    Optional<File> findByFileNameAndUserLogin(String fileName, String username);

    List<File> findFilesByUserLogin(String username);

    default List<File> findFileListByUsername(String username, int limit) {
        List<File> files = findFilesByUserLogin(username);
        List<File> newList = new ArrayList<>();

        for (File file : files) {
            if (!file.isItsRemoved()) {
                newList.add(file);
                limit--;

                if (limit == 0) {
                    break;
                }
            }
        }
        return newList;
    }

    default void renameFile(String fileName, String newFileName, String username) {
        File file = findByFileNameAndUserLogin(fileName, username).orElseThrow(() ->
                new InputDataException("Error input data"));
        file.setFileName(newFileName);
        save(file);
    }
}
