package com.fernando.microservices.cart_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fernando.microservices.cart_service.dto.ProductView;

@FeignClient(name = "ecomod-product-materialized-view-service", url = "http://ecomod-product-materialized-view-service:8080")
public interface OpenFeignConfig {
    
    @GetMapping("/api/v1/products/see/{id}")
    ProductView getProductViewById(@PathVariable String id);
}
