package com.fernando.microservices.auth_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.auth_service.entity.UserInfo;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
    
    Optional<UserInfo> findByEmail(String email);
}
