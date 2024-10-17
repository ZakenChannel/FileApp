package ru.kuznec.fileapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDTO {
    private UUID id;
    private String username;
    private String role;
    private boolean blocked;
    private int fileLimit;
    private long storageLimit;

    public UserDTO(UUID id, String username, String role, boolean blocked, int fileLimit, long storageLimit) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.blocked = blocked;
        this.fileLimit = fileLimit;
        this.storageLimit = storageLimit;
    }
}
