package com.example.email_service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailArchiveRepository emailArchiveRepository;

    private final RestTemplate restTemplate;

    private static final String ATTACHMENT_SERVICE_URL = "http://localhost:8083/api/attachments/upload";

    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEmailWithAttachment(String fromEmail, String to, String subject, String text, MultipartFile file)
            throws MessagingException, IOException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.setFrom(fromEmail);

        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                attachmentUrl = uploadFileToAttachmentService(file);
                helper.addAttachment(file.getOriginalFilename(), file);
            } catch (IOException e) {
                throw new IOException("Erreur lors de l'upload du fichier : " + e.getMessage());
            }
        }

        mailSender.send(message);

        // Archivage de l'e-mail
        EmailArchive archive = new EmailArchive(fromEmail, to, subject, text, attachmentUrl);
        emailArchiveRepository.save(archive);
    }

    private String uploadFileToAttachmentService(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(ATTACHMENT_SERVICE_URL, requestEntity, Map.class);
        } catch (Exception e) {
            throw new IOException("Service de pièces jointes inaccessible !");
        }

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().get("filePath").toString();
        } else {
            throw new IOException("Échec de l'upload du fichier !");
        }
    }

}
