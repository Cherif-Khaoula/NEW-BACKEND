package com.coderdot.services;


import com.coderdot.entities.Role;
import com.coderdot.entities.User;
import com.coderdot.repository.RoleRepository;
import com.coderdot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User createUser(String name, String email, String password, String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // À sécuriser avec BCrypt !
        user.setRole(role);

        return userRepository.save(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
