package com.fernando.microservices.catalog_command_service.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fernando.microservices.catalog_command_service.aggregate.ProductAggregate;
import com.fernando.microservices.catalog_command_service.dto.ProductRequest;
import com.fernando.microservices.catalog_command_service.entity.Category;
import com.fernando.microservices.catalog_command_service.entity.ProductEvent;
import com.fernando.microservices.catalog_command_service.exceptions.DatabaseException;
import com.fernando.microservices.catalog_command_service.repositories.CategoryRepository;
import com.fernando.microservices.catalog_command_service.repositories.CommandRepository;
import com.fernando.microservices.common_service.events.CategoryInfo;
import com.fernando.microservices.common_service.events.ProductCategoryAddedEvent;
import com.fernando.microservices.common_service.events.ProductDescriptionUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductNameUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductOutOfStock;
import com.fernando.microservices.common_service.events.ProductPriceUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductPublishedEvent;
import com.fernando.microservices.common_service.events.ProductStockChanged;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ProductCommandService {
    
    private final CommandRepository commandRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final ProductAggregateService aggregateService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void createProduct(ProductRequest productRequest, Long userId, String roles) {

        if (!roles.equals("BUSINESS")) {
            System.out.println("not business");
            throw new RuntimeException("User is not allowed to perform this action");
        }

        List<Category> categories = categoryRepository.findAllById(productRequest.getCategoryIds());

        List<CategoryInfo> categoryInfos = categories.stream()
            .map(category -> new CategoryInfo(category.getId(), category.getName()))
            .toList();

        ProductPublishedEvent productPublishedEvent = new ProductPublishedEvent(
            UUID.randomUUID().toString(), 
            userId,
            productRequest.getName(), 
            productRequest.getDescription(), 
            productRequest.getPrice(), 
            productRequest.getImageUrls(), 
            categoryInfos
        );

        String payload = objectMapper.writeValueAsString(productPublishedEvent);

        ProductEvent event = new ProductEvent();
        event.setAggregateId(productPublishedEvent.getAggregateId());
        event.setEventType(ProductPublishedEvent.class.getName());
        event.setPayload(payload);
        event.setCreatedAt(LocalDateTime.now());

        try {
            commandRepository.save(event);
            kafkaTemplate.send("product-published-topic", productPublishedEvent);
        } catch (Exception e) {
            throw new DatabaseException();
        }
    }

    @Transactional
    public void updateProduct(String productId, ProductRequest productRequest) throws ClassNotFoundException {
        ProductAggregate aggregate = aggregateService.loadProduct(productId);
        List<Object> events = new ArrayList<>();
        List<ProductEvent> dbEvents = new ArrayList<>();
        List<Runnable> kafkaSends = new ArrayList<>();
        
        if (aggregate == null) {
            throw new RuntimeException("Product not found by the id: " + productId);
        }

        if (StringUtils.hasText(productRequest.getName())) {
            events.add(new ProductNameUpdatedEvent(
                productId,
                productRequest.getName()
            ));
        }
        if (StringUtils.hasText(productRequest.getDescription())) {
            events.add(new ProductDescriptionUpdatedEvent(
                productId,
                productRequest.getDescription()
            ));
        }
        if (productRequest.getPrice() != null) {
            events.add(new ProductPriceUpdatedEvent(
                productId,
                productRequest.getPrice()
            ));
        }
        if (productRequest.getCategoryIds() != null && !productRequest.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(productRequest.getCategoryIds());

            if (!categories.isEmpty()) {
                for (Category categoryOb : categories) {
                    CategoryInfo categoryInfo = new CategoryInfo(categoryOb.getId(), categoryOb.getName());
                    if (aggregate.getCategories().contains(categoryInfo)) {
                        throw new RuntimeException("Category already in product");
                    }
                    events.add(new ProductCategoryAddedEvent(
                        productId,
                        categoryInfo
                    ));
                }
            }
        }

        for (Object eventOb : events) {
            String payload = objectMapper.writeValueAsString(eventOb);

            ProductEvent event = new ProductEvent();
            event.setAggregateId(productId);
            event.setEventType(eventOb.getClass().getName());
            event.setPayload(payload);
            event.setCreatedAt(LocalDateTime.now());
            dbEvents.add(event);

            if (eventOb.getClass().equals(ProductNameUpdatedEvent.class)) {
                kafkaSends.add(() -> kafkaTemplate.send("product-name-updated-topic", eventOb));
            }
            if (eventOb.getClass().equals(ProductDescriptionUpdatedEvent.class)) {
                kafkaSends.add(() -> kafkaTemplate.send("product-description-updated-topic", eventOb));
            }
            if (eventOb.getClass().equals(ProductPriceUpdatedEvent.class)) {
                kafkaSends.add(() -> kafkaTemplate.send("product-price-updated-topic", eventOb));
            }
            if (eventOb.getClass().equals(ProductCategoryAddedEvent.class)) {
                kafkaSends.add(() -> kafkaTemplate.send("product-category-added-topic", eventOb));
            }

            // kafkaTemplate.send("product-events-topic", eventOb);
        }

        commandRepository.saveAll(dbEvents);
        kafkaSends.forEach(Runnable::run);
    }

    @Transactional
    @KafkaListener(topics = "product-out-of-stock-topic", groupId = "catalog-service")
    public void listenOutOfStock(ProductOutOfStock event) {
        String payload = objectMapper.writeValueAsString(event);

        ProductEvent productEvent = new ProductEvent();
        productEvent.setAggregateId(event.getAggregateId());
        productEvent.setEventType(ProductOutOfStock.class.getName());
        productEvent.setPayload(payload);
        productEvent.setCreatedAt(LocalDateTime.now());
        commandRepository.save(productEvent);
    }

    @Transactional
    @KafkaListener(topics = "product-change-stock-topic", groupId = "catalog-service")
    public void listenChangeStock(ProductStockChanged event) {
        String payload = objectMapper.writeValueAsString(event);

        ProductEvent productEvent = new ProductEvent();
        productEvent.setAggregateId(event.getAggregateId());
        productEvent.setEventType(ProductStockChanged.class.getName());
        productEvent.setPayload(payload);
        productEvent.setCreatedAt(LocalDateTime.now());
        commandRepository.save(productEvent);
    }
}
