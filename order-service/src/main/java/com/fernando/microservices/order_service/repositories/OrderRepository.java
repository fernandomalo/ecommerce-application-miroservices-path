package com.fernando.microservices.order_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.order_service.entitty.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserId(Long userId);
    Optional<Order> findByUserIdAndId(Long userId, Long orderId);
}
