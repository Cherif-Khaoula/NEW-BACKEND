package com.example.email_service;



import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
public class EmailArchive {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String recipient;
    private String subject;

    @Column(columnDefinition = "TEXT") // Stocker un texte long
    private String content;

    private String attachmentPath; // Chemin du fichier attaché (optionnel)

    private LocalDateTime sentAt; // Date d'envoi

    public EmailArchive() {
        this.sentAt = LocalDateTime.now();
    }

    public EmailArchive(String sender, String recipient, String subject, String content, String attachmentPath) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.attachmentPath = attachmentPath;
        this.sentAt = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getContent() {
        return content;
    }

    public String getRecipient() {
        return recipient;
    }
// Getters et Setters
}
