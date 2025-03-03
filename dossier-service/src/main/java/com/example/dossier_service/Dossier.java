package com.example.dossier_service;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "dossiers")
public class Dossier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(unique = true, nullable = false)
    private String numeroDossier;

    @Getter
    @Setter
    @Column(nullable = false)
    private String objet;

    @Setter
    @Getter
    @Column(nullable = false) // Géré comme String au lieu d'un Enum
    private String type;

    @Setter
    @Getter
    @Column(nullable = false)
    private String champ1 = "/";

    @Setter
    @Getter
    @Column(nullable = false)
    private String champ2 = "/";

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    @Setter
    @Getter
    @Column(nullable = false)
    private String champ3 = "/";

    @Getter
    @Setter
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Getter
    private LocalDateTime dateModification;

    // Getters & Setters
}
