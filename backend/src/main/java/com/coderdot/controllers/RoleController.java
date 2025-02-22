package com.coderdot.controllers;

import com.coderdot.entities.Role;
import com.coderdot.services.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AJOUTERROLE')")
    public ResponseEntity<?> createRole(@RequestBody Role roleRequest) {
        try {
            Role role = roleService.createRoleWithPermissions(roleRequest);
            return ResponseEntity.ok(role);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur serveur");
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GETALLROLE')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GETROLE')")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODIFIERROLE')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role roleRequest) {
        try {
            Optional<Role> updatedRole = roleService.updateRole(id, roleRequest);
            return updatedRole.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur serveur");
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETEROLE')")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok("Rôle supprimé avec succès !");
    }
}