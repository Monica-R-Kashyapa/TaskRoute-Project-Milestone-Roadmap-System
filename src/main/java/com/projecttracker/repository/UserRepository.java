package com.projecttracker.repository;

import com.projecttracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.role = 'MANAGER'")
    java.util.List<User> findAllManagers();
    
    @Query("SELECT u FROM User u WHERE u.role = 'CLIENT'")
    java.util.List<User> findAllClients();
}
