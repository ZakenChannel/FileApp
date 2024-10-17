package ru.kuznec.fileapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kuznec.fileapp.dto.FileDTO;
import ru.kuznec.fileapp.model.File;
import ru.kuznec.fileapp.model.User;
import ru.kuznec.fileapp.service.FileService;
import ru.kuznec.fileapp.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    private final UserService userService;

    public FileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!fileService.canUploadFile(user, file.getSize())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Превышены лимиты на загрузку файлов");
            }

            try {
                File uploadedFile = fileService.saveFile(file, user);

                FileDTO fileDTO = new FileDTO(
                        uploadedFile.getId(),
                        uploadedFile.getFilename(),
                        uploadedFile.getSize(),
                        uploadedFile.getPublicLink(),
                        uploadedFile.getUploadDate(),
                        uploadedFile.isPublic(),
                        user.getUsername()
                );

                return ResponseEntity.ok(fileDTO);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Пользователь не найден");
        }
    }

    @GetMapping
    public ResponseEntity<List<File>> getAllFiles(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            List<File> files = fileService.getFilesByUser(userOpt.get());
            return ResponseEntity.ok(files);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable UUID fileId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (fileService.deleteFile(fileId, user)) {
                return ResponseEntity.ok("Файл успешно удален");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Файл не найден или у вас нет прав для его удаления");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{fileId}/public-link")
    public ResponseEntity<?> getPublicLink(@PathVariable UUID fileId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            Optional<String> publicLink = fileService.generatePublicLink(fileId, user);
            if (publicLink.isPresent()) {
                return ResponseEntity.ok(publicLink.get());
            } else {
                if (fileService.fileExists(fileId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Файл не найден или у вас нет прав на его просмотр");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Файл не найден");
                }
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}