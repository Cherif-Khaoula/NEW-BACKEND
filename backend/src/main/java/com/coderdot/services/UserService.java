package com.coderdot.services;

import com.coderdot.entities.Role;
import com.coderdot.entities.User;
import com.coderdot.repository.RoleRepository;
import com.coderdot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    // Injection de PasswordEncoder via constructeur
    @Autowired
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // 🔹 Créer un utilisateur
    public User createUser(User userRequest) {

        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Cet email est déjà utilisé !");
        }

        // Ajouter les rôles existants à l'utilisateur
        Set<Role> roles = new HashSet<>();
        if (userRequest.getRoles() != null) {
            for (Role role : userRequest.getRoles()) {
                Optional<Role> existingRole = roleRepository.findByName(role.getName());
                if (existingRole.isPresent()) {
                    roles.add(existingRole.get());
                } else {
                    throw new IllegalArgumentException("Rôle introuvable : " + role.getName());
                }
            }
        }

        userRequest.setRoles(roles);

        // Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(userRequest.getPassword());
        userRequest.setPassword(encodedPassword);

        // Sauvegarder l'utilisateur
        return userRepository.save(userRequest);
    }

    // 🔹 Récupérer un utilisateur par ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // 🔹 Mettre à jour un utilisateur
    public Optional<User> updateUser(Long id, User userRequest) {
        Optional<User> existingUserOpt = userRepository.findById(id);
        if (existingUserOpt.isEmpty()) {
            return Optional.empty();
        }

        User existingUser = existingUserOpt.get();

        // Mettre à jour les champs
        if (userRequest.getName() != null && !userRequest.getName().trim().isEmpty()) {
            existingUser.setName(userRequest.getName());
        }

        if (userRequest.getEmail() != null && !userRequest.getEmail().trim().isEmpty()) {
            existingUser.setEmail(userRequest.getEmail());
        }

        if (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()) {
            // Encoder le mot de passe mis à jour
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Gestion des rôles
        Set<Role> roles = new HashSet<>();
        if (userRequest.getRoles() != null) {
            for (Role role : userRequest.getRoles()) {
                Optional<Role> existingRole = roleRepository.findByName(role.getName());
                if (existingRole.isPresent()) {
                    roles.add(existingRole.get());
                } else {
                    throw new IllegalArgumentException("Rôle introuvable : " + role.getName());
                }
            }
        }
        existingUser.setRoles(roles);

        return Optional.of(userRepository.save(existingUser));
    }

    // 🔹 Récupérer tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 🔹 Supprimer un utilisateur
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        userRepository.deleteById(id);
    }
    public List<String> getEmailsOfCmeMembers() {
        return userRepository.findEmailsOfCmeMembers();
    }
}
