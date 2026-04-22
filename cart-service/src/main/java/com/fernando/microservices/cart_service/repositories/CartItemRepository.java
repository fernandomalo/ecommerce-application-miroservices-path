package com.fernando.microservices.cart_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.cart_service.entity.CartItem;
import com.fernando.microservices.cart_service.entity.CartItemStatus;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    Optional<CartItem> findByCartIdAndProductProductId(String cartId, String productId);

    List<CartItem> findByStatusAndCartId(CartItemStatus status, String cartId);
}
