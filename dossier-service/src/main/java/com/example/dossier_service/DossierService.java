package com.example.dossier_service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DossierService {
    @Autowired
    private DossierRepository dossierRepository;

    @Autowired
    private RestTemplate restTemplate; // 🔥 Pour appeler UserService & EmailService

    private static final String USERSERVICE_URL = "http://localhost:8080/api/users/emails-cme";
    private static final String EMAILSERVICE_URL = "http://localhost:8082/email/send-email-attachment";

    @Autowired
    private HttpServletRequest request; // Pour récupérer le token depuis la requête

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public Dossier createDossier(Dossier dossier, HttpServletRequest request) {
        // Vérifier et remplir les champs vides avec "/"
        if (dossier.getChamp1() == null || dossier.getChamp1().isEmpty()) dossier.setChamp1("/");
        if (dossier.getChamp2() == null || dossier.getChamp2().isEmpty()) dossier.setChamp2("/");
        if (dossier.getChamp3() == null || dossier.getChamp3().isEmpty()) dossier.setChamp3("/");

        // Définir la date de création si elle n'est pas fournie
        if (dossier.getDateCreation() == null) {
            dossier.setDateCreation(LocalDateTime.now());
        }
        Dossier savedDossier = dossierRepository.save(dossier);
        // Vérifier si le dossier a bien été inséré avant d'envoyer un email
        if (savedDossier == null ) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec de l'enregistrement du dossier");
        }
        // 1️⃣ Récupérer le token JWT depuis la requête
        String token = extractToken(request);

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token manquant");
        }

        // 2️⃣ Ajouter le token JWT dans l'en-tête
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);  // 🔥 CORRECTION : Ajout du point-virgule ici

        // 3️⃣ Effectuer la requête GET avec le token pour récupérer les emails
        ResponseEntity<List> response;
        try {
            response = restTemplate.exchange(USERSERVICE_URL, HttpMethod.GET, entity, List.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Erreur lors de la récupération des emails CME : " + e.getMessage());
        }

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<String> emailsCME = response.getBody();

            if (!emailsCME.isEmpty()) {
                try {
                    MultiValueMap<String, String> emailRequest = new LinkedMultiValueMap<>();
                    emailRequest.add("to", String.join(",", emailsCME)); // 🔹 Convertir la liste en String
                    emailRequest.add("subject", "🔔 Nouveau dossier à traiter");
                    emailRequest.add("text", "Un nouveau dossier a été ajouté et doit être traité.\nDétails : " + dossier.toString());

                    // 🔥 CORRECTION : Ajout du token dans l'en-tête pour `EmailService`
                    HttpHeaders emailHeaders = new HttpHeaders();
                    emailHeaders.set("Authorization", "Bearer " + token);  // 🔥 Ajout du token ici
                    emailHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(emailRequest, emailHeaders);

                    ResponseEntity<String> emailResponse = restTemplate.postForEntity(EMAILSERVICE_URL, requestEntity, String.class);

                    if (emailResponse.getStatusCode() == HttpStatus.OK) {

                        System.out.println("✅ Email envoyé avec succès aux membres CME !");

                    } else {
                        System.err.println("❌ Erreur lors de l'envoi de l'email : " + emailResponse.getBody());
                    }

                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de l'appel à EmailService : " + e.getMessage());
                }
            }
        }

        return savedDossier;
    }


public Dossier getDossierByNumero(String numeroDossier) {
        return dossierRepository.findByNumeroDossier(numeroDossier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dossier non trouvé"));
    }

    public Dossier updateDossier(String numeroDossier, Dossier updatedDossier) {
        Dossier dossier = getDossierByNumero(numeroDossier);
        dossier.setObjet(updatedDossier.getObjet());
        dossier.setType(updatedDossier.getType());
        dossier.setChamp1(updatedDossier.getChamp1().isEmpty() ? "/" : updatedDossier.getChamp1());
        dossier.setChamp2(updatedDossier.getChamp2().isEmpty() ? "/" : updatedDossier.getChamp2());
        dossier.setChamp3(updatedDossier.getChamp3().isEmpty() ? "/" : updatedDossier.getChamp3());
        dossier.setDateModification(LocalDateTime.now());

        return dossierRepository.save(dossier);
    }

    public void deleteDossier(String numeroDossier) {
        dossierRepository.deleteByNumeroDossier(numeroDossier);
    }

    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }
}
