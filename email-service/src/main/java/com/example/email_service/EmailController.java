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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil; // Classe utilitaire pour extraire les infos du JWT

    @PostMapping("/send-email-attachment")
    public ResponseEntity<Map<String, String>> sendEmailWithAttachment(
            HttpServletRequest request,
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("text") String text,
            @RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();

        try {
            String token = extractToken(request);
            if (token == null) {
                response.put("error", "Erreur : Aucun token fourni !");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String userEmail = jwtUtil.extractUsername(token);
            emailService.sendEmailWithAttachment(userEmail, to, subject, text, file);

            response.put("message", "E-mail envoyé avec succès depuis : " + userEmail);
            return ResponseEntity.ok(response); // ✅ Toujours un JSON
        } catch (ExpiredJwtException e) {
            response.put("error", "Erreur JWT : Le token est expiré !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (SignatureException e) {
            response.put("error", "Erreur JWT : Signature invalide !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (MalformedJwtException e) {
            response.put("error", "Erreur JWT : Format du token invalide !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (MessagingException | IOException e) {
            response.put("error", "Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
