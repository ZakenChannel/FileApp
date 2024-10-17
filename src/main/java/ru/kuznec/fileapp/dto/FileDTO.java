package ru.kuznec.fileapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class FileDTO {
    private final UUID id;
    private final String filename;
    private final long size;
    private final String publicLink;
    private final String uploadDate;
    private final boolean isPublic;
    private final String username;

    public FileDTO(UUID id, String filename, long size, String publicLink, String uploadDate, boolean isPublic, String username) {
        this.id = id;
        this.filename = filename;
        this.size = size;
        this.publicLink = publicLink;
        this.uploadDate = uploadDate;
        this.isPublic = isPublic;
        this.username = username;
    }
}
