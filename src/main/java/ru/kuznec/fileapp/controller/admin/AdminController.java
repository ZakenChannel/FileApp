package ru.kuznec.fileapp.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kuznec.fileapp.dto.FileDTO;
import ru.kuznec.fileapp.dto.LimitDTO;
import ru.kuznec.fileapp.dto.UserDTO;
import ru.kuznec.fileapp.model.File;
import ru.kuznec.fileapp.model.User;
import ru.kuznec.fileapp.service.FileService;
import ru.kuznec.fileapp.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final FileService fileService;

    public AdminController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable UUID userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    user.isBlocked(),
                    user.getFileLimit(),
                    user.getStorageLimit()
            );

            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.");
        }
    }

    @PostMapping("/block/{userId}")
    public ResponseEntity<?> blockUser(@PathVariable UUID userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            userService.blockUser(userId);
            return ResponseEntity.ok("Пользователь заблокирован.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден.");
        }
    }

    @PostMapping("/unblock/{userId}")
    public ResponseEntity<?> unblockUser(@PathVariable UUID userId) {
        userService.unblockUser(userId);
        return ResponseEntity.ok("Пользователь разблокирован.");
    }

    @PostMapping("/set-limits/{userId}")
    public ResponseEntity<?> setUserLimits(@PathVariable UUID userId, @RequestBody LimitDTO limitDTO) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFileLimit(limitDTO.getFileLimit());
            user.setStorageLimit(limitDTO.getStorageLimit());
            userService.save(user);

            return ResponseEntity.ok("Лимиты обновлены");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileDTO>> getAllFiles() {
        List<File> allFiles = fileService.getAllFiles();
        List<FileDTO> fileDTOs = allFiles.stream().map(file -> new FileDTO(
                file.getId(),
                file.getFilename(),
                file.getSize(),
                file.getPublicLink(),
                file.getUploadDate(),
                file.isPublic(),
                file.getUser().getUsername()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(fileDTOs);
    }

    @GetMapping("/user/{userId}/files")
    public ResponseEntity<List<FileDTO>> getFilesByUser(@PathVariable UUID userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            List<File> userFiles = fileService.getFilesByUser(userOpt.get());
            List<FileDTO> fileDTOs = userFiles.stream().map(file -> new FileDTO(
                    file.getId(),
                    file.getFilename(),
                    file.getSize(),
                    file.getPublicLink(),
                    file.getUploadDate(),
                    file.isPublic(),
                    file.getUser().getUsername()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(fileDTOs);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable UUID fileId) {
        if (fileService.deleteFile(fileId)) {
            return ResponseEntity.ok("Файл успешно удален");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Файл не найден");
        }
    }
}

