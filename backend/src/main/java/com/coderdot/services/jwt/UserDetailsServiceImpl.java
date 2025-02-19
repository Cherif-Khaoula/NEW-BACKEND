package com.coderdot.services.jwt;

import com.coderdot.entities.User;
import com.coderdot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findFirstByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User user = userOptional.get();
return null;
        // Convertit le rôle en chaîne de caractères avant de le passer à SimpleGrantedAuthority
         //List<SimpleGrantedAuthority> authorities = Collections.singletonList(
         //       new SimpleGrantedAuthority(user.getRole().name()) // Ajout de .name()
     //   );

       //return new org.springframework.security.core.userdetails.User(
         //       user.getEmail(),
            //    user.getPassword(),
            //    authorities
      //  );
    }
}
