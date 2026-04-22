package com.fernando.microservices.inventory_service.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.fernando.microservices.inventory_service.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    
    Optional<Stock> findByProductIdAndUserId(String productId, Long userId);
    Optional<Stock> findByProductId(String productId);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT IGNORE INTO stock 
        (product_id, user_id, quantity, available_stock, reserved_quantity, status, updated_at)
        VALUES (:productId, :userId, 0, 0, 0, :status, :updatedAt)
        """, nativeQuery = true)
    void insertIfNotExists(
        @Param("productId") String productId,
        @Param("userId") Long userId,
        @Param("status") String status,
        @Param("updatedAt") LocalDateTime updatedAt
    );
}
