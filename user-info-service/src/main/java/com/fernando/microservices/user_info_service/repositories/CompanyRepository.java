package com.fernando.microservices.user_info_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.user_info_service.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Optional<Company> findByUserInfoUserId(Long id);
}
