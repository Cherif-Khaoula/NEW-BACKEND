package com.coderdot.repository;

import com.coderdot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findFirstByEmail(String email);
    Optional<User> findByEmail(String email);

}
