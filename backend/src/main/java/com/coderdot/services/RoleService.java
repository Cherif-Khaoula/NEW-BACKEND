package com.coderdot.services;

import com.coderdot.entities.Role;
import com.coderdot.entities.Permission;
import com.coderdot.repository.PermissionRepository;
import com.coderdot.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public Role createRoleWithPermissions(Role roleRequest) {
        if (roleRequest.getRoleName() == null || roleRequest.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est obligatoire !");
        }

        if (roleRepository.findByName(roleRequest.getRoleName()).isPresent()) {
            throw new IllegalArgumentException("Ce rôle existe déjà !");
        }

        Role role = new Role();
        role.setRoleName(roleRequest.getRoleName());

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
        return roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    }

    public Role updateRole(Long id, Role roleRequest) {
        Role role = getRoleById(id);
        role.setRoleName(roleRequest.getRoleName());

        // 🔹 Mise à jour des permissions
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

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
