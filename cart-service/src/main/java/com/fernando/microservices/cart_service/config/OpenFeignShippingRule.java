package com.fernando.microservices.cart_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fernando.microservices.cart_service.dto.ShippingRuleDto;

@FeignClient(name = "ecomod-shipping-rules-service", url = "http://ecomod-shipping-rules-service:8080")
public interface OpenFeignShippingRule {
    
    @GetMapping("/api/v1/shipping-rules/price/{id}")
    ShippingRuleDto getRuleById(@PathVariable Long id);

    @GetMapping("/api/v1/shipping-rules")
    ShippingRuleDto getRuleByOriginAndDestination(@RequestParam String origin, @RequestParam String destination);
}
