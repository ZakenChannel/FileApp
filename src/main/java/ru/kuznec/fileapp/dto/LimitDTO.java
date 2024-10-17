package ru.kuznec.fileapp.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LimitDTO {
    private int fileLimit;
    private long storageLimit;
}

