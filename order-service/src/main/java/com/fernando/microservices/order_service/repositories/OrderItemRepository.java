package com.fernando.microservices.order_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.order_service.entitty.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
}
