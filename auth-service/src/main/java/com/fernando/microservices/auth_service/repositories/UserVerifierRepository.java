package com.fernando.microservices.auth_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.auth_service.entity.UserInfo;
import com.fernando.microservices.auth_service.entity.UserVerifier;

public interface UserVerifierRepository extends JpaRepository<UserVerifier, Long> {
    
    Optional<UserVerifier> findByCode(Integer code);
    Optional<UserVerifier> findByUser(UserInfo user);
}
