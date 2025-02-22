package com.coderdot.controllers;

import com.coderdot.dto.AuthentificationRequest;
import com.coderdot.dto.AuthentificationResponse;
import com.coderdot.entities.User;
import com.coderdot.repository.UserRepository;
import com.coderdot.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AuthentificationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    @PostMapping("/authenticate")
    public AuthentificationResponse createAuthentificationToken(
            @RequestBody AuthentificationRequest authentificationRequest, HttpServletResponse response
    ) throws IOException {
        System.out.println("Authentification request received for: " + authentificationRequest.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authentificationRequest.getEmail(), authentificationRequest.getPassword())
            );
            System.out.println("Authentication successful for: " + authentificationRequest.getEmail());
        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed: Incorrect email or password");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Incorrect email or password.");
            return null;
        } catch (DisabledException disabledException) {
            System.out.println("Authentication failed: User is not activated");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not activated");
            return null;
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authentificationRequest.getEmail());
        System.out.println("User details loaded for: " + userDetails.getUsername());

        Optional<User> optionalUser = userRepository.findFirstByEmail(userDetails.getUsername());
        if (optionalUser.isEmpty()) {
            System.out.println("User not found for email: " + userDetails.getUsername());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            return null;
        }

        final User user = optionalUser.get();
        final String jwt = jwtUtil.generateToken(user);

        System.out.println("JWT generated: " + jwt);

        response.setHeader("Access-Control-Expose-Headers", HEADER_STRING);
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader(HEADER_STRING, TOKEN_PREFIX + jwt);

        // Récupérer les rôles
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        // Récupérer les permissions associées aux rôles
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream()) // Supposant que chaque rôle a une liste de permissions
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        return new AuthentificationResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getPassword(), // Hashé (assuré que c'est bien sécurisé dans la base)
                roles,
                permissions,
                user.getName()
        );
    }

}
