package com.example.cloud.service;

import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.example.cloud.dto.FileDTO;
import com.example.cloud.entity.File;
import com.example.cloud.exceptions.InputDataException;
import com.example.cloud.repositories.FileRepository;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    private static final Logger log = Logger.getLogger(FileService.class);


    public void deleteByFileNameAndUsername(String fileName, String username) {
        fileRepository.deleteByFileNameAndUsername(fileName, username);
    }

    public void save(File file) {
        fileRepository.save(file);
    }

    public List<FileDTO> listFiles(String username, int limit) {
        if (limit <= 0) {
            log.error("Error input data: limit <= 0");
            throw new InputDataException("Error input data");
        }

        List<File> files = fileRepository.findFileListByUsername(username, limit);

        List<FileDTO> fileDTOS = new ArrayList<>();
        for (int i = files.size() - 1; i >= 0; i--) {
            fileDTOS.add(new FileDTO(files.get(i).getFileName(), files.get(i).getSize()));
        }
        return fileDTOS;

    }

    public void renameFile(String fileName, String newFileName, String username) {
        fileRepository.renameFile(fileName, newFileName, username);
    }
}
