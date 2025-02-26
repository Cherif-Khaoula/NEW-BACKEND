package com.example.email_service;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailArchiveRepository extends JpaRepository<EmailArchive, Long> {
    List<EmailArchive> findBySender(String sender);
    List<EmailArchive> findByRecipient(String recipient);
}
