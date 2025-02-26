package com.example.attachments_service;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private String fileName;
    @Setter
    @Getter
    private String filePath;
    private String fileType;
    private long fileSize;

    public Attachment() {}

    public Attachment(String fileName, String filePath, String fileType, long fileSize) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    // Getters et Setters

}
