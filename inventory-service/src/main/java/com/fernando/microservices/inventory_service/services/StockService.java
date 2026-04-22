package com.fernando.microservices.inventory_service.services;

import java.math.BigInteger;

import com.fernando.microservices.common_service.events.ProductPublishedEvent;
import com.fernando.microservices.inventory_service.entity.Stock;

public interface StockService {
    Stock getStockByProductIdAndUserId(String productId, Long userId);
    void initializeStock(ProductPublishedEvent event);
    void reserveStock(String productId, Long userId, BigInteger quantity);
    void updateStock(String productId, BigInteger quantity, String type, Long userId);
}
