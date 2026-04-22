package com.fernando.microservices.shipping_rules_service.services;

import org.springframework.stereotype.Service;

import com.fernando.microservices.shipping_rules_service.entity.ShippingRule;
import com.fernando.microservices.shipping_rules_service.repositories.ShippingRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShippingRuleServiceImpl implements ShippingRuleService {

    private final ShippingRuleRepository shippingRuleRepository;

    @Override
    public ShippingRule getShippingRule(String origin, String destination) {
        return shippingRuleRepository.findByOriginZoneAndDestinationZone(origin, destination)
            .orElseGet(() -> {
                ShippingRule shippingRule = new ShippingRule();
                shippingRule.setPrice(0.0);
                return shippingRule;
            });
    }

    @Override
    public ShippingRule getShippingRuleById(Long id) {
        return shippingRuleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Shipping rule not found by id"));
    }

}
