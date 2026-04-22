package com.fernando.microservices.cart_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.cart_service.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, String> {
    
    Optional<Cart> findByUserId(Long userId);
    Optional<Cart> findByAnonymousToken(String anonymousToken);
}
