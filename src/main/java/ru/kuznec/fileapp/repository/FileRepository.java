package ru.kuznec.fileapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kuznec.fileapp.model.File;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
    List<File> findByUserId(UUID userId);
}
