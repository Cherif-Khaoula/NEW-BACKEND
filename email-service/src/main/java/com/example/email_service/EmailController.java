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
    private boolean hasPermission(String token, String requiredPermission) {
        try {
            List<String> permissions = jwtUtil.extractPermissions(token);
            return permissions.contains(requiredPermission);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/send-email-attachment")
    public ResponseEntity<Map<String, String>> sendEmailWithAttachment(
            HttpServletRequest request,
            @RequestParam("to") List<String> toList, // 🔹 Modification ici pour accepter plusieurs e-mails
            @RequestParam("subject") String subject,
            @RequestParam("text") String text,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Map<String, String> response = new HashMap<>();

        try {
            // Extraction du token
            String token = extractToken(request);
            if (token == null || !hasPermission(token, "SENDEMAIL")) {
                response.put("error", "Permission refuse !");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String userEmail = jwtUtil.extractUsername(token);

            // Vérifier les adresses e-mail cibles
            for (String to : toList) {
                if (!isValidEmail(to)) {
                    response.put("error", "Adresse e-mail invalide : " + to);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            // Vérifier si un fichier est présent ou non
            if (file != null && !file.isEmpty()) {
                emailService.sendEmailWithAttachment(userEmail, toList, subject, text, file);
                response.put("message", "E-mails avec pièce jointe envoyés avec succès depuis : " + userEmail);
            } else {
                emailService.sendEmailWithoutAttachment(userEmail, toList, subject, text);
                response.put("message", "E-mails sans pièce jointe envoyés avec succès depuis : " + userEmail);
            }

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
    public ResponseEntity<?> getArchivedEmails(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Aucun token fourni !");
        }

        try {
            // Extraction des permissions du token
            List<String> permissions = jwtUtil.extractPermissions(token);

            // Vérifier si l'utilisateur a la permission "SEEALLEMAIL"
            if (!permissions.contains("SEEALLEMAIL")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : permission SEEALLEMAIL requise !");
            }

            List<EmailDTO> emails = emailArchiveRepository.findAll().stream()
                    .map(email -> {
                        String attachmentUrl = null;
                        if (email.getAttachmentPath() != null) {
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
                                attachmentUrl
                        );
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(emails);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expiré !");
        } catch (SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Signature du token invalide !");
        } catch (MalformedJwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Format du token invalide !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne : " + e.getMessage());
        }
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
        if (token == null || !hasPermission(token, "SEEEMAIL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
        if (token == null || !hasPermission(token, "SEEEMAIL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
