package com.fernando.microservices.shipping_rules_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.shipping_rules_service.entity.ShippingRule;
import com.fernando.microservices.shipping_rules_service.services.ShippingRuleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shipping-rules")
public class ShippingRuleController {

    private final ShippingRuleService shippingRuleService;

    @GetMapping
    public ShippingRule getRuleByOriginAndDestination(@RequestParam String origin, @RequestParam String destination) {
        return shippingRuleService.getShippingRule(origin, destination);
    }

    @GetMapping("/price/{id}")
    public ShippingRule getRuleById(@PathVariable Long id) {
        return shippingRuleService.getShippingRuleById(id);
    }
}
