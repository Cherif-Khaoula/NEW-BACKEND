package com.coderdot.services;

import com.coderdot.entities.Permission;
import com.coderdot.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        Permission permission = getPermissionById(id);
        permission.setName(permissionRequest.getName());
        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}
