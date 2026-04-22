package com.fernando.microservices.product_materialized_view_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fernando.microservices.product_materialized_view_service.entity.ProductView;

public interface ProductViewRepository extends JpaRepository<ProductView, String> {
    
    @Query("SELECT p FROM ProductView p WHERE :category MEMBER OF p.categories")
    List<ProductView> findByCategory(@Param("category") String category);
}
