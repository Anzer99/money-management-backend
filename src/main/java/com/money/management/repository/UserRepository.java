package com.money.management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.management.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
