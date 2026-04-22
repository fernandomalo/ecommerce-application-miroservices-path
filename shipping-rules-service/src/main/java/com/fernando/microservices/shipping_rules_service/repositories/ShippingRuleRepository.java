package com.fernando.microservices.shipping_rules_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.shipping_rules_service.entity.ShippingRule;

public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {
    
    Optional<ShippingRule> findByOriginZoneAndDestinationZone(String origin, String destination);
}
