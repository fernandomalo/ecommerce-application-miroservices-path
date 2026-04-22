package com.fernando.microservices.product_materialized_view_service.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fernando.microservices.common_service.events.ProductCategoryAddedEvent;
import com.fernando.microservices.common_service.events.ProductDescriptionUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductNameUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductPriceUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductPublishedEvent;
import com.fernando.microservices.common_service.events.ProductSendCompanyInfoEvent;
import com.fernando.microservices.common_service.events.ProductStockChanged;
import com.fernando.microservices.product_materialized_view_service.entity.ProductView;
import com.fernando.microservices.product_materialized_view_service.repositories.ProductViewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductViewQueryService {
    
    private final ProductViewRepository viewRepository;

    @KafkaListener(topics = "product-published-topic", groupId = "view-service")
    @CacheEvict(cacheNames = {"productViewCache", "productViewCacheByCategory"}, allEntries = true)
    public void handle(ProductPublishedEvent event) {
        List<String> categories = event.getCategories().stream()
            .map(c -> c.getCategoryName())
            .toList();

        ProductView productView = ProductView.builder()
                    .id(event.getAggregateId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .price(event.getPrice())
                    .imageUrls(event.getImageUrls())
                    .categories(categories)
                    .createdAt(LocalDateTime.now())
                    .build();
            
        viewRepository.save(productView);
    }

    // @KafkaListener(topics = "product-change-stock-topic", groupId = "view-service")
    // public void handle(ProductStockChanged event) {
    //     ProductView productView = viewRepository.findById(event.getAggregateId())
    //         .orElseThrow(() -> new RuntimeException("Product not found by that id"));

    //     productView.setAvailableStock(event.getAvailableStock());
    //     viewRepository.save(productView);
    // }

    @CachePut(cacheNames = "productViewCacheById", key = "#event.aggregateId")
    @CacheEvict(cacheNames = "productViewCacheByCategory", allEntries = true)
    public void changeStock(ProductStockChanged event) {
        ProductView productView = viewRepository.findById(event.getAggregateId())
            .orElseThrow(() -> new RuntimeException("Product not found by that id"));

        productView.setAvailableStock(event.getAvailableStock());
        viewRepository.save(productView);
    }

    @KafkaListener(topics = "product-name-updated-topic", groupId = "view-service")
    @CachePut(cacheNames = "productViewCacheById", key = "#event.aggregateId")
    @CacheEvict(cacheNames = "productViewCacheByCategory", allEntries = true)
    public void handle(ProductNameUpdatedEvent event) {
        ProductView productView = viewRepository.findById(event.getAggregateId())
            .orElseThrow(() -> new RuntimeException("Product not found by that id"));

        productView.setName(event.getName());
        viewRepository.save(productView);
    }

    @KafkaListener(topics = "product-description-updated-topic", groupId = "view-service")
    @CachePut(cacheNames = "productViewCacheById", key = "#event.aggregateId")
    @CacheEvict(cacheNames = "productViewCacheByCategory", allEntries = true)
    public void handle(ProductDescriptionUpdatedEvent event) {
        ProductView productView = viewRepository.findById(event.getAggregateId())
            .orElseThrow(() -> new RuntimeException("Product not found by that id"));

        productView.setDescription(event.getDescription());
        viewRepository.save(productView);
    }

    @KafkaListener(topics = "product-price-updated-topic", groupId = "view-service")
    @CachePut(cacheNames = "productViewCacheById", key = "#event.aggregateId")
    @CacheEvict(cacheNames = "productViewCacheByCategory", allEntries = true)
    public void handle(ProductPriceUpdatedEvent event) {
        ProductView productView = viewRepository.findById(event.getAggregateId())
            .orElseThrow(() -> new RuntimeException("Product not found by that id"));

        productView.setPrice(event.getPrice());
        viewRepository.save(productView);
    }

    @KafkaListener(topics = "product-category-added-topic", groupId = "view-service")
    @CachePut(cacheNames = "productViewCacheById", key = "#event.aggregateId")
    @CacheEvict(cacheNames = "productViewCacheByCategory", allEntries = true)
    public void handle(ProductCategoryAddedEvent event) {
        ProductView productView = viewRepository.findById(event.getAggregateId())
            .orElseThrow(() -> new RuntimeException("Product not found by that id"));

        productView.getCategories().add(event.getCategory().getCategoryName());
        viewRepository.save(productView);
    }

    @KafkaListener(topics = "product-company-info-topic", groupId = "view-service-v2")
    @CachePut(cacheNames = "productViewCacheById", key = "#event.aggregateId")
    @CacheEvict(cacheNames = "productViewCacheByCategory", allEntries = true)
    public void handle(ProductSendCompanyInfoEvent event) {
        ProductView productView = viewRepository.findById(event.getAggregateId())
            .orElseThrow(() -> new RuntimeException("Product not found by that id"));

        productView.setCompanyId(event.getCompanyId());
        productView.setCompanyName(event.getCompanyName());
        productView.setCompanySlug(event.getCompanySlug());

        viewRepository.save(productView);
    }
}
