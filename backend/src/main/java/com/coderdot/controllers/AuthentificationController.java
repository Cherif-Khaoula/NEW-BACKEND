package com.coderdot.controllers;

import com.coderdot.dto.AuthentificationRequest;
import com.coderdot.dto.AuthentificationResponse;
import com.coderdot.entities.User;
import com.coderdot.repository.UserRepository;
import com.coderdot.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthentificationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    /**
     * Endpoint pour l'authentification des utilisateurs.
     */
    @PostMapping("/authenticate")
    public AuthentificationResponse createAuthentificationToken(
            @RequestBody AuthentificationRequest authentificationRequest, HttpServletResponse response
    ) throws IOException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authentificationRequest.getEmail(), authentificationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Incorrect email or password.");
            return null;
        } catch (DisabledException disabledException) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not activated");
            return null;
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authentificationRequest.getEmail());
        Optional<User> optionalUser = userRepository.findFirstByEmail(userDetails.getUsername());

        if (optionalUser.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return null;
        }

        final String role = userDetails.getAuthorities().isEmpty() ? "USER" : userDetails.getAuthorities().iterator().next().getAuthority();
        final String jwt = jwtUtil.generateToken(userDetails.getUsername(), role);


        // Définition des headers avant d'écrire dans la réponse
        response.setHeader("Access-Control-Expose-Headers", HEADER_STRING);
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader(HEADER_STRING, TOKEN_PREFIX + jwt);
return null;
      //  return new AuthentificationResponse(jwt ,optionalUser.get().getId(), optionalUser.get().getRole() ,optionalUser.get().getName());
    }

    /**
     * Récupérer tous les utilisateurs (Accessible uniquement aux administrateurs).
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupérer un utilisateur par son ID (Accessible uniquement aux administrateurs).
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Créer un nouvel utilisateur (Accessible uniquement aux administrateurs).
     */
    @PostMapping("/users/create/")
    @PreAuthorize("hasAuthority('ADMIN')")
    public User createUser(@RequestBody User user) {
        // Vérifier si l'email existe déjà
        Optional<User> existingUser = userRepository.findFirstByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        // Crypter le mot de passe
        String encodedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Sauvegarder l'utilisateur avec le mot de passe crypté
        return userRepository.save(user);
    }

    /**
     * Mettre à jour un utilisateur existant (Accessible uniquement aux administrateurs).
     */
    @PutMapping("/users/modifier/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public User updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mettre à jour les informations de l'utilisateur
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        return userRepository.save(user);
    }

    /**
     * Supprimer un utilisateur (Accessible uniquement aux administrateurs).
     */
    @DeleteMapping("/users/supprimer/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
