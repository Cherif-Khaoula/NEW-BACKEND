package com.example.email_service;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.MalformedJwtException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil; // Gestion des tokens JWT

    @PostMapping("/send-email-attachment")
    public ResponseEntity<Map<String, String>> sendEmailWithAttachment(
            HttpServletRequest request,
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("text") String text,
            @RequestParam("file") MultipartFile file) {

        Map<String, String> response = new HashMap<>();

        try {
            // Extraction du token
            String token = extractToken(request);
            if (token == null) {
                response.put("error", "Aucun token fourni !");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String userEmail = jwtUtil.extractUsername(token);

            // Vérifier l'adresse email cible
            if (!isValidEmail(to)) {
                response.put("error", "Adresse e-mail invalide !");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Envoyer l'email avec la pièce jointe
            emailService.sendEmailWithAttachment(userEmail, to, subject, text, file);
            response.put("message", "E-mail envoyé avec succès depuis : " + userEmail);
            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            response.put("error", "Token expiré !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (SignatureException e) {
            response.put("error", "Signature du token invalide !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (MalformedJwtException e) {
            response.put("error", "Format du token invalide !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (MessagingException | IOException e) {
            response.put("error", "Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Autowired
    private EmailArchiveRepository emailArchiveRepository;

    @GetMapping("/archive")
    public ResponseEntity<List<EmailDTO>> getArchivedEmails() {
        List<EmailDTO> emails = emailArchiveRepository.findAll().stream()
                .map(email -> {
                    String attachmentUrl = null;
                    if (email.getAttachmentPath() != null) {
                        // Construire l'URL de la pièce jointe en utilisant l'API de fichiers
                        attachmentUrl = "http://localhost:8083/api/attachments/view-by-path?filePath=" +
                                URLEncoder.encode(email.getAttachmentPath(), StandardCharsets.UTF_8);
                    }

                    return new EmailDTO(
                            email.getId(),
                            email.getSubject(),
                            email.getContent(),
                            email.getRecipient(),
                            email.getSender(),
                            email.getSentAt(),
                            attachmentUrl // URL de la pièce jointe
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(emails);
    }


    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    @GetMapping("/sent")
    public ResponseEntity<List<EmailDTO>> getSentEmails(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = jwtUtil.extractUsername(token);

        List<EmailDTO> sentEmails = emailArchiveRepository.findBySender(userEmail)
                .stream()
                .map(email -> mapToEmailDTO(email))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sentEmails);
    }

    @GetMapping("/received")
    public ResponseEntity<List<EmailDTO>> getReceivedEmails(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userEmail = jwtUtil.extractUsername(token);

        List<EmailDTO> receivedEmails = emailArchiveRepository.findByRecipient(userEmail)
                .stream()
                .map(email -> mapToEmailDTO(email))
                .collect(Collectors.toList());

        return ResponseEntity.ok(receivedEmails);
    }

    // Méthode pour convertir une entité Email en DTO
    private EmailDTO mapToEmailDTO(EmailArchive email) {
        String attachmentUrl = (email.getAttachmentPath() != null) ?
                "http://localhost:8083/api/attachments/view-by-path?filePath=" +
                        URLEncoder.encode(email.getAttachmentPath(), StandardCharsets.UTF_8) : null;

        return new EmailDTO(
                email.getId(),
                email.getSubject(),
                email.getContent(),
                email.getRecipient(),
                email.getSender(),
                email.getSentAt(),
                attachmentUrl
        );
    }

}
