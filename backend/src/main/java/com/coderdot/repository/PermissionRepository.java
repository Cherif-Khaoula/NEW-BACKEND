package com.coderdot.repository;

import com.coderdot.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // Méthodes de base CRUD héritées de JpaRepository

    Optional<Permission> findByName(String name);
}
