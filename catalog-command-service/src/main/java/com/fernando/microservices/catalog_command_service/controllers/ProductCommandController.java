package com.fernando.microservices.catalog_command_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.catalog_command_service.aggregate.ProductAggregate;
import com.fernando.microservices.catalog_command_service.dto.ProductRequest;
import com.fernando.microservices.catalog_command_service.services.ProductAggregateService;
import com.fernando.microservices.catalog_command_service.services.ProductCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductCommandController {
    
    private final ProductCommandService commandService;
    private final ProductAggregateService aggregateService;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest productRequest, @RequestHeader("X-User-Id") String userIdString, @RequestHeader("roles") String roles) {
        Long userId = Long.parseLong(userIdString);
        System.out.println("user-id: " + userIdString);
        System.out.println("roles: " + roles);
        try {
            commandService.createProduct(productRequest, userId, roles);
            return ResponseEntity.ok("Product created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong when creating a product");
        }
    }

    @PatchMapping("/update/{productId}")
    public ResponseEntity<?> updateProductById(@PathVariable String productId, @RequestBody ProductRequest productRequest) {
        try {
            commandService.updateProduct(productId, productRequest);
            return ResponseEntity.ok("Product updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong when updating a product " + e);
        }
    }

    @GetMapping("/{id}")
    public ProductAggregate findAggregate(@PathVariable String id) throws ClassNotFoundException {
        return aggregateService.loadProduct(id);
    }
}
