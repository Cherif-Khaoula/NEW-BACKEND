package com.example.dossier_service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




@RestController
@RequestMapping("/dossiers")
public class DossierController {
    @Autowired
    private JwtUtil jwtUtil; // Gestion des tokens JWT

    @Autowired
    private DossierService dossierService;

    @Autowired
    private HttpServletRequest request; // Pour récupérer le token depuis la requête

    private boolean hasPermission(String token, String requiredPermission) {
        try {
            List<String> permissions = jwtUtil.extractPermissions(token);
            return permissions.contains(requiredPermission);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createDossier(@RequestBody Dossier dossier, HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            // 🔹 Extraction du token depuis la requête
            String token = extractToken(request);
            if (token == null || !hasPermission(token, "AJOUTERDOSSIER")) {
                response.put("error", "Permission refusée !");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 🔹 Création du dossier en passant la requête pour le token
            Dossier createdDossier = dossierService.createDossier(dossier, request);
            return ResponseEntity.ok(createdDossier);

        } catch (ExpiredJwtException e) {
            response.put("error", "Token expiré !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (SignatureException e) {
            response.put("error", "Signature du token invalide !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (MalformedJwtException e) {
            response.put("error", "Format du token invalide !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{numeroDossier}")
    public ResponseEntity<Dossier> getDossier(@PathVariable String numeroDossier) {
        return ResponseEntity.ok(dossierService.getDossierByNumero(numeroDossier));
    }

    @PutMapping("/{numeroDossier}")
    public ResponseEntity<Dossier> updateDossier(@PathVariable String numeroDossier, @RequestBody Dossier dossier) {
        return ResponseEntity.ok(dossierService.updateDossier(numeroDossier, dossier));
    }

    @DeleteMapping("/{numeroDossier}")
    public ResponseEntity<Void> deleteDossier(@PathVariable String numeroDossier) {
        dossierService.deleteDossier(numeroDossier);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Dossier>> getAllDossiers() {
        return ResponseEntity.ok(dossierService.getAllDossiers());
    }
}
