package com.example.dossier_service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {
    Optional<Dossier> findByNumeroDossier(String numeroDossier);
    void deleteByNumeroDossier(String numeroDossier);
}
