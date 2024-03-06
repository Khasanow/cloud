package com.example.cloud.controllers;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.cloud.dto.FileDTO;
import com.example.cloud.dto.PutRequest;
import com.example.cloud.service.FileService;
import com.example.cloud.service.StorageService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;


@RestController
public class CloudController {
    private final StorageService storageService;
    private final FileService fileService;
    private static final Logger log = Logger.getLogger(CloudController.class);

    public CloudController(StorageService storageService, FileService fileService) {
        this.storageService = storageService;
        this.fileService = fileService;
    }

    @PostMapping("/file")
    public void uploadFile(Principal principal,@RequestParam("filename") String fileName,
                           @RequestBody MultipartFile file) throws IOException {
        log.info("POST Request: uploadFile: " + fileName);
        storageService.saveFile(principal.getName(), file.getBytes(), fileName);
    }

    @DeleteMapping("/file")
    public void deleteFile(Principal principal, @RequestParam("filename") String fileName) {
        log.info("DELETE Request: delete file: " + fileName);
        storageService.deleteFile(principal.getName(), fileName);
    }


    @GetMapping("/file")
    public byte[] downloadFile(Principal principal, @RequestParam("filename") String fileName) {
        log.info("GET Request: download file: " + fileName);
        return storageService.downloadFile(principal.getName(), fileName);
    }

    @PutMapping("/file")
    public void editFileName(Principal principal, @RequestParam("filename") String fileName,
                             @RequestBody PutRequest putRequest) {
        log.info("PUT Request: edit filename: " + fileName);
        storageService.renameFile(principal.getName(), fileName, putRequest.getFilename());
    }

    @GetMapping("/list")
    public List<FileDTO> getFileList(Principal principal,
                                     @RequestParam(name = "limit", defaultValue = "5") int limit) {
        log.info("GET Request: get FileList");
        return fileService.listFiles(principal.getName(), limit);
    }
}