package com.fernando.microservices.catalog_command_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.catalog_command_service.entity.ProductEvent;

public interface CommandRepository extends JpaRepository<ProductEvent, Long> {
    
    List<ProductEvent> findByAggregateId(String aggregateId);
}
