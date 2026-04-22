package com.fernando.microservices.shipping_rules_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.shipping_rules_service.entity.Zones;

public interface ZoneRepository extends JpaRepository<Zones, Long> {
    
    Optional<Zones> findByName(String name);
    List<Zones> findByParentId(Long parentId);

    List<Zones> findByParentIdIsNull();
}
