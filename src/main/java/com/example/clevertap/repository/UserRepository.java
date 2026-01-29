package com.example.clevertap.repository;

import com.example.clevertap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdentity(String identity);
    Optional<User> findByEmail(String email);
}
