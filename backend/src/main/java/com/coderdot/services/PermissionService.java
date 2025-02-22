package com.coderdot.services;

import com.coderdot.entities.Permission;
import com.coderdot.entities.Role;

import com.coderdot.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Permission non trouvée"));
    }

    public Permission updatePermission(Long id, Permission permissionRequest) {
        // Récupérer la permission existante
        Optional<Permission> existingPermissionOpt = permissionRepository.findById(id);
        if (existingPermissionOpt.isEmpty()) {
            throw new RuntimeException("Permission non trouvée avec l'ID : " + id);
        }

        Permission existingPermission = existingPermissionOpt.get();

        // Mettre à jour le nom de la permission
        if (permissionRequest.getName() != null && !permissionRequest.getName().isEmpty()) {
            existingPermission.setName(permissionRequest.getName());
        }

        // Sauvegarder la permission mise à jour
        return permissionRepository.save(existingPermission);
    }

    public void deletePermission(Long id) {
        Optional<Permission> permissionOpt = permissionRepository.findById(id);

        if (permissionOpt.isEmpty()) {
            throw new RuntimeException("Permission non trouvée avec l'ID : " + id);
        }

        Permission permission = permissionOpt.get();

        // Retirer la permission de tous les rôles associés
        for (Role role : permission.getRoles()) {  // Assure-toi que Permission a une méthode getRoles()
            role.getPermissions().remove(permission);
        }

        permissionRepository.deleteById(id);  // Supprimer après avoir retiré les associations
    }}

