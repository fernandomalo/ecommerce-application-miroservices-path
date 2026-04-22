package com.fernando.microservices.shipping_rules_service.services;

import com.fernando.microservices.shipping_rules_service.entity.ShippingRule;

public interface ShippingRuleService {
    ShippingRule getShippingRule(String origin, String destination);
    ShippingRule getShippingRuleById(Long id);
}
