package com.example.demo.repositories;

import com.example.demo.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    Optional<Token> findByUsername(String username);

    @Modifying
    void deleteByUsername(String username);
}