package com.example.cloud.repositories;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import com.example.cloud.exceptions.FileException;
import com.example.cloud.exceptions.InputDataException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Repository
public class CloudRepository {
    @Value("${cloud.storeDirectory}")
    private String directory;
    private static final Logger log = Logger.getLogger(CloudRepository.class);

    public void saveFileToRepository(byte[] file, String fileName, String login) {
        try {
            String path = directory + login;
            Files.createDirectories(Paths.get(path));
            File pathToFile = new File(path + File.separator + fileName);

            if (pathToFile.exists()) {
                log.error("File " + fileName + " already  exists ");
                throw new InputDataException("Error input data");
            }

            FileOutputStream outputStream = new FileOutputStream(pathToFile);
            outputStream.write(file);
            outputStream.close();

        } catch (IOException e) {
            log.error("Failed to save file " + fileName);
            throw new InputDataException("Error input data");
        }

    }

    public void renameFile(String fileName, String newFileName, String login) {
        String path = directory + login + File.separator;
        File file = new File(path + fileName);
        File newFile = new File(path + newFileName);
        if (!file.isFile() || !file.renameTo(newFile)) {
            log.error("Error upload file: failed to rename file " + fileName);
            throw new FileException("Error upload file");
        }

    }

    public byte[] downloadFileFromRepository(String fileName, String login) {
        File file = new File(directory + login + File.separator + fileName);
        if (file.isFile()) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                return fileInputStream.readAllBytes();
            } catch (IOException e) {
                log.error("Error upload file " + fileName);
                throw new FileException("Error upload file");
            }
        } else {
            log.error("Error upload file " + fileName);
            throw new FileException("Error upload file");
        }
    }
}
