package com.example.email_service;

import java.time.LocalDateTime;

public class EmailDTO {
    private Long id;
    private String subject;
    private String content;
    private String recipient;
    private String sender;
    private LocalDateTime sentAt;
    private String attachmentPath;

    // Constructeur avec tous les paramètres
    public EmailDTO(Long id, String subject, String content, String recipient, String sender, LocalDateTime sentAt, String attachmentPath) {
        this.id = id;
        this.subject = subject;
        this.content = content;
        this.recipient = recipient;
        this.sender = sender;
        this.sentAt = sentAt;
        this.attachmentPath = attachmentPath;
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }
}
