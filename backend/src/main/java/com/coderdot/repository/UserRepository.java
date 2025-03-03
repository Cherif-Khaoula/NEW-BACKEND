package com.coderdot.repository;

import com.coderdot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = """
    SELECT u.email FROM users u 
    JOIN user_roles ur ON u.id = ur.user_id 
    JOIN roles r ON ur.role_id = r.id 
    WHERE r.name = 'CME'
""", nativeQuery = true)
    List<String> findEmailsOfCmeMembers();


    Optional<User> findFirstByEmail(String email);
    Optional<User> findByEmail(String email);



}
