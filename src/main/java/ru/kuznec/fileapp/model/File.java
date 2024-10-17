package ru.kuznec.fileapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private boolean isPublic;

    @Column
    private String publicLink;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String uploadDate;
}
