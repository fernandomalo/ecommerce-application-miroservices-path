package com.fernando.microservices.auth_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.auth_service.entity.AppRole;
import com.fernando.microservices.auth_service.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    Optional<UserRole> findByRole(AppRole role);
}
