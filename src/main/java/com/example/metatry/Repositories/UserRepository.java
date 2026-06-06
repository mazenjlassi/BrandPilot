package com.example.metatry.Repositories;

import com.example.metatry.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Fix: Change from findByname to findByName (capital N)
    Optional<User> findByName(String name);

    // Add this for email login
    Optional<User> findByEmail(String email);
}