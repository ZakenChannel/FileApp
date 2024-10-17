package ru.kuznec.fileapp.controller;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kuznec.fileapp.service.FileService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private final FileService fileService;

    public FileDownloadController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
        try {
            Path filePath = fileService.getFileById(fileId);
            Resource resource = new PathResource(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}