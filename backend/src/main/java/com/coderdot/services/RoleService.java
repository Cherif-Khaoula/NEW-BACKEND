package com.coderdot.services;
import org.springframework.transaction.annotation.Transactional;
import com.coderdot.entities.Permission;
import com.coderdot.entities.Role;
import com.coderdot.entities.User;
import com.coderdot.repository.PermissionRepository;
import com.coderdot.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public Role createRoleWithPermissions(Role roleRequest) {
        if (roleRequest.getName() == null || roleRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est obligatoire !");
        }

        if (roleRepository.findByName(roleRequest.getName()).isPresent()) {
            throw new IllegalArgumentException("Ce rôle existe déjà !");
        }

        Role role = new Role();
        role.setName(roleRequest.getName());

        // 🔹 Gestion des permissions
        Set<Permission> permissions = new HashSet<>();
        if (roleRequest.getPermissions() != null) {
            for (Permission p : roleRequest.getPermissions()) {
                Optional<Permission> existingPermission = permissionRepository.findByName(p.getName());
                if (existingPermission.isPresent()) {
                    permissions.add(existingPermission.get());
                } else {
                    Permission newPermission = new Permission(p.getName());
                    permissionRepository.save(newPermission);
                    permissions.add(newPermission);
                }
            }
        }

        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    }


    @Transactional
    public Optional<Role> updateRole(Long id, Role roleRequest) {
        Optional<Role> existingRoleOpt = roleRepository.findById(id);
        if (existingRoleOpt.isEmpty()) {
            return Optional.empty();
        }

        Role existingRole = existingRoleOpt.get();

        // Mise à jour du nom
        if (roleRequest.getName() != null && !roleRequest.getName().trim().isEmpty()) {
            existingRole.setName(roleRequest.getName());
        }

        // Mise à jour des permissions
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {
            for (Permission permission : roleRequest.getPermissions()) {
                if (permission.getId() == null) {
                    // Sauvegarder les nouvelles permissions avant de les associer au rôle
                    permissionRepository.save(permission);
                }
            }
            existingRole.setPermissions(roleRequest.getPermissions());
        }

        // Sauvegarder le rôle avec les permissions mises à jour
        return Optional.of(roleRepository.save(existingRole));
    }


    public void deleteRole(Long id) {
        Optional<Role> roleOpt = roleRepository.findById(id);

        if (roleOpt.isEmpty()) {
            throw new RuntimeException("Rôle non trouvé avec l'ID : " + id);
        }

        Role role = roleOpt.get();

        // Retirer le rôle de tous les utilisateurs associés
        for (User user : role.getUsers()) {  // Assure-toi que Role a une méthode getUsers()
            user.getRoles().remove(role);
        }

        // Supprimer le rôle après avoir retiré les associations
        roleRepository.deleteById(id);
    }
}
