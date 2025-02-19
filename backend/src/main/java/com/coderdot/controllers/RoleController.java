package com.coderdot.controllers;

import com.coderdot.entities.Role;
import com.coderdot.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    // 🔹 Créer un rôle avec des permissions associées
    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role roleRequest) {
        try {
            Role role = roleService.createRoleWithPermissions(roleRequest);
            return ResponseEntity.ok(role);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🔹 Récupérer tous les rôles
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // 🔹 Récupérer un rôle par ID
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    // 🔹 Mettre à jour un rôle
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleRequest) {
        return ResponseEntity.ok(roleService.updateRole(id, roleRequest));
    }

    // 🔹 Supprimer un rôle
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok("Rôle supprimé avec succès !");
    }
}
