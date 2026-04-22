package com.fernando.microservices.shipping_rules_service.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.shipping_rules_service.entity.Zones;
import com.fernando.microservices.shipping_rules_service.services.ZoneService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/zones")
public class ZoneController {
    
    private final ZoneService zoneService;

    @GetMapping("/countries")
    public List<Zones> getCountries() {
        return zoneService.getCountries();
    }

    @GetMapping("/{parentId}")
    public List<Zones> getZonesByParentId(@PathVariable Long parentId) {
        return zoneService.getZonesByParentId(parentId);
    }
}
