package com.fernando.microservices.inventory_service.controllers;

import java.math.BigInteger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.inventory_service.entity.Stock;
import com.fernando.microservices.inventory_service.services.StockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory/products")
@Slf4j
public class StockController {
    
    private final StockService stockService;

    @GetMapping("/{productId}")
    public Stock getStockByProductIdAndUserId(@PathVariable String productId, @RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        log.info("Received user header " + userId);

        return stockService.getStockByProductIdAndUserId(productId, userId);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<?> addOrRemoveStockQuantity(@PathVariable String productId, @RequestParam BigInteger quantity, @RequestParam String type, @RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        stockService.updateStock(productId, quantity, type, userId);
        
        return ResponseEntity.ok("Stock updated successfully");
    }

    @PutMapping("/reserve/{productId}/{quantity}")
    public ResponseEntity<?> reserveStockToUser(@PathVariable String productId, @PathVariable BigInteger quantity, @RequestParam Long userId) {
        stockService.reserveStock(productId, userId, quantity);
        return ResponseEntity.ok("reserved stock successfully");
    }
}
