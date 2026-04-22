package com.fernando.microservices.product_materialized_view_service.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fernando.microservices.product_materialized_view_service.entity.ProductView;
import com.fernando.microservices.product_materialized_view_service.repositories.ProductViewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReadSideService {
    
    private final ProductViewRepository viewRepository;

    @Transactional(readOnly = true)
    // @Cacheable(cacheNames = "productViewCache")
    public List<ProductView> getAllProductViews() {
        return viewRepository.findAll();
    }

    @Transactional(readOnly = true)
    // @Cacheable(cacheNames = "productViewCacheByCategory", key = "#category")
    public List<ProductView> getProductViewsByCategory(String category) {
        return viewRepository.findByCategory(category);
    }

    // @Cacheable(cacheNames = "productViewCacheById", key = "#id")
    public ProductView getProductViewById(String id) {
        return viewRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
