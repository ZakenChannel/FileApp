package ru.kuznec.fileapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kuznec.fileapp.model.File;
import ru.kuznec.fileapp.model.User;
import ru.kuznec.fileapp.repository.FileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("uploads");

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public boolean canUploadFile(User user, long fileSize) {
        List<File> userFiles = fileRepository.findByUserId(user.getId());
        long totalSize = userFiles.stream().mapToLong(File::getSize).sum();

        if (userFiles.size() >= user.getFileLimit()) {
            return false;
        }

        return (totalSize + fileSize) <= user.getStorageLimit();
    }

    public File saveFile(MultipartFile file, User user) throws IOException {
        Files.createDirectories(this.rootLocation);


        String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Files.copy(file.getInputStream(), this.rootLocation.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);

        File uploadedFile = File.builder()
                .filename(uniqueFilename)
                .size(file.getSize())
                .isPublic(false)
                .user(user)
                .uploadDate(LocalDateTime.now().toString())
                .build();

        return fileRepository.save(uploadedFile);
    }

    public List<File> getFilesByUser(User user) {
        return fileRepository.findByUserId(user.getId());
    }

    public List<File> getAllFiles() {
        return fileRepository.findAll();
    }

    public boolean deleteFile(UUID fileId) {
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            try {
                Files.deleteIfExists(Paths.get("uploads").resolve(file.getFilename()));
                fileRepository.delete(file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean deleteFile(UUID fileId, User user) {
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            if (file.getUser().getId().equals(user.getId())) {
                try {
                    Files.deleteIfExists(this.rootLocation.resolve(file.getFilename()));

                    fileRepository.delete(file);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public Optional<String> generatePublicLink(UUID fileId, User user) {
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isPresent()) {
            File file = fileOpt.get();
            if (file.getUser().getId().equals(user.getId())) {
                String publicLink = "http://localhost:8080/api/files/download/" + file.getId();

                file.setPublic(true);
                file.setPublicLink(publicLink);
                fileRepository.save(file);

                return Optional.of(publicLink);
            }
        }
        return Optional.empty();
    }

    public Path getFileById(UUID fileId) throws IOException {
        Optional<File> fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isPresent() && fileOpt.get().isPublic()) {
            File file = fileOpt.get();
            return this.rootLocation.resolve(file.getFilename());
        }
        throw new IOException("Файл не найден или не является публичным");
    }

    public boolean fileExists(UUID fileId) {
        return fileRepository.existsById(fileId);
    }
}
