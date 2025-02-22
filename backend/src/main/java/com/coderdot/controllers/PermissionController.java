package com.coderdot.controllers;

import com.coderdot.entities.Permission;
import com.coderdot.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    // 🔹 Créer une permission
    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permissionRequest) {
        return ResponseEntity.ok(permissionService.createPermission(permissionRequest));
    }

    // 🔹 Récupérer toutes les permissions
    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    // 🔹 Récupérer une permission par ID
    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    // 🔹 Mettre à jour une permission
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePermission(@PathVariable Long id, @RequestBody Permission permissionRequest) {
        try {
            Permission updatedPermission = permissionService.updatePermission(id, permissionRequest);
            return ResponseEntity.ok(updatedPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur serveur : " + e.getMessage());
        }
    }
    // 🔹 Supprimer une permission
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok("Permission supprimée avec succès !");
    }

}