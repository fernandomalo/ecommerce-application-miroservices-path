package com.fernando.microservices.shipping_rules_service.services;

import java.util.List;

import com.fernando.microservices.shipping_rules_service.entity.Zones;

public interface ZoneService {
    List<Zones> getZonesByParentId(Long parentId);
    List<Zones> getCountries();
}
