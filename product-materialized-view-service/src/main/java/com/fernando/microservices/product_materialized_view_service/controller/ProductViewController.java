package com.fernando.microservices.product_materialized_view_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.common_service.events.ProductStockChanged;
import com.fernando.microservices.product_materialized_view_service.entity.ProductView;
import com.fernando.microservices.product_materialized_view_service.service.ProductReadSideService;
import com.fernando.microservices.product_materialized_view_service.service.ProductViewQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products/see")
public class ProductViewController {
    
    private final ProductReadSideService readSideService;
    private final ProductViewQueryService queryService;

    @GetMapping("/list")
    public List<ProductView> getProductViews() {
        return readSideService.getAllProductViews();
    }

    @PutMapping("/change-stock/{id}")
    public ResponseEntity<?> changeAvailableStock(@PathVariable String id, @RequestBody ProductStockChanged event) {
        queryService.changeStock(event);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ProductView getProductViewById(@PathVariable String id) {
        return readSideService.getProductViewById(id);
    }

    @GetMapping("/category/{category}")
    public List<ProductView> getProductViewsByCategory(@PathVariable String category) {
        return readSideService.getProductViewsByCategory(category);
    }
}
