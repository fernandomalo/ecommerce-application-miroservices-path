package com.fernando.microservices.shipping_rules_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fernando.microservices.shipping_rules_service.entity.Zones;
import com.fernando.microservices.shipping_rules_service.repositories.ZoneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {
    
    private final ZoneRepository zoneRepository;

    @Override
    public List<Zones> getZonesByParentId(Long parentId) {
        return zoneRepository.findByParentId(parentId);
    }

    @Override
    public List<Zones> getCountries() {
        return zoneRepository.findByParentIdIsNull();
    }
    
}
