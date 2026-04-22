package com.fernando.microservices.inventory_service.services;

import java.math.BigInteger;
import java.time.LocalDateTime;

import org.apache.catalina.webresources.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fernando.microservices.common_service.events.ProductPublishedEvent;
import com.fernando.microservices.common_service.events.ProductStockChanged;
import com.fernando.microservices.inventory_service.entity.Stock;
import com.fernando.microservices.inventory_service.entity.StockStatus;
import com.fernando.microservices.inventory_service.repositories.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestClientService restClientService;
    private final CacheManager cacheManager;

    @Override
    @Cacheable(cacheNames = "stockCacheById", key = "#productId")
    public Stock getStockByProductIdAndUserId(String productId, Long userId) {
        return stockRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new RuntimeException("Stock not found for that product"));
    }

    @KafkaListener(topics = "product-published-topic", groupId = "published-event")
    @Override
    @Transactional
    public void initializeStock(ProductPublishedEvent event) {
        Stock stock = Stock.builder()
                .productId(event.getAggregateId())
                .userId(event.getUserId())
                .quantity(BigInteger.ZERO)
                .availableStock(BigInteger.ZERO)
                .reservedQuantity(BigInteger.ZERO)
                .updatedAt(LocalDateTime.now())
                .status(StockStatus.NOT_INITIALIZED)
                .build();

        stockRepository.save(stock);

        
    }

    @Override
    @Transactional
    @CacheEvict(value = "stockCacheById", key = "#productId")
    public void updateStock(String productId, BigInteger quantity, String type, Long userId) {
        Stock stock = stockRepository.findByProductIdAndUserId(productId, userId)
                .orElseThrow(() -> new RuntimeException("Stock not found for that product"));

        if (type.equals("add")) {
            stock.addStock(quantity);
            stock.setStatus(StockStatus.AVAILABLE);
            stockRepository.save(stock);
        } else if (type.equals("reduce")) {
            stock.reduceStock(quantity);
            stock.setStatus(StockStatus.AVAILABLE);
            stockRepository.save(stock);
        }
        ProductStockChanged productStockChanged = new ProductStockChanged(stock.getProductId(),
                    stock.getAvailableStock());
            kafkaTemplate.send("product-change-stock-topic", productStockChanged);

        restClientService.changeAvailableStockForProductView(productStockChanged);
    }

    @Override
    @Transactional
    @CachePut(value = "stockCacheById", key = "#productId")
    public void reserveStock(String productId, Long userId, BigInteger quantity) {
        try {
            Stock stock = stockRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("Stock not found for that product"));
    
            if (stock.getStatus().equals(StockStatus.NOT_INITIALIZED)) {
                throw new RuntimeException("Stock not initialized for that product");
            } else if (!stock.isAvailable(quantity)) {
                throw new RuntimeException("Insufficient stock available");
            } else if (userId == null) {
                throw new RuntimeException("User ID is required to reserve stock");
            }
    
            stock.reserveStock(quantity);
            stockRepository.save(stock);
    
            ProductStockChanged productStockChanged = new ProductStockChanged(stock.getProductId(),
                        stock.getAvailableStock());
    
            restClientService.changeAvailableStockForProductView(productStockChanged);   
        } catch (Exception e) {
            e.getStackTrace();
            throw new RuntimeException("Failed to reserve stock: " + e.getMessage());
        }
    }

}
