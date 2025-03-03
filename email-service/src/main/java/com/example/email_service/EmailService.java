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

    public void sendEmailWithAttachment(String fromEmail, List<String> toList, String subject, String text, MultipartFile file)
            throws MessagingException, IOException {

        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            try {
                attachmentUrl = uploadFileToAttachmentService(file);
            } catch (IOException e) {
                throw new IOException("Erreur lors de l'upload du fichier : " + e.getMessage());
            }
        }

        for (String to : toList) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text);

                if (attachmentUrl != null) {
                    helper.addAttachment(file.getOriginalFilename(), file);
                }

                mailSender.send(message);
                System.out.println("✅ Email envoyé avec succès à : " + to);

                // Archivage
                EmailArchive archive = new EmailArchive(fromEmail, to, subject, text, attachmentUrl);
                emailArchiveRepository.save(archive);

            } catch (Exception e) {
                System.err.println("❌ Erreur lors de l'envoi de l'email à : " + to + " -> " + e.getMessage());
            }
        }
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


    public void sendEmailWithoutAttachment(String from, List<String> toList, String subject, String text) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(toList.toArray(new String[0])); // Convertir la liste en tableau
        helper.setSubject(subject);
        helper.setText(text, true);

        mailSender.send(message);

        // 🔴 Archivage pour chaque destinataire
        for (String to : toList) {
            EmailArchive archive = new EmailArchive(from, to, subject, text, null);
            emailArchiveRepository.save(archive);
        }
    }

}
