package com.fernando.microservices.catalog_command_service.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fernando.microservices.catalog_command_service.aggregate.ProductAggregate;
import com.fernando.microservices.catalog_command_service.entity.ProductEvent;
import com.fernando.microservices.catalog_command_service.repositories.CommandRepository;
import com.fernando.microservices.common_service.events.ProductStockChanged;
import com.fernando.microservices.common_service.events.ProductCategoryAddedEvent;
import com.fernando.microservices.common_service.events.ProductDescriptionUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductNameUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductOutOfStock;
import com.fernando.microservices.common_service.events.ProductPriceUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductPublishedEvent;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ProductAggregateService {

    private final CommandRepository commandRepository;
    private final ObjectMapper objectMapper;

    public ProductAggregate loadProduct(String productId) throws ClassNotFoundException {

        ProductAggregate aggregate = new ProductAggregate();

        List<ProductEvent> events = commandRepository.findByAggregateId(productId);

        if (events.isEmpty()) {
            return null;
        }

        for (ProductEvent productEvent : events) {
            Class<?> productClass = Class.forName(productEvent.getEventType());
            Object productOb = objectMapper.readValue(productEvent.getPayload(), productClass);

            if (productClass.equals(ProductPublishedEvent.class)) {
                aggregate.handle((ProductPublishedEvent) productOb);
            }

            if (productClass.equals(ProductNameUpdatedEvent.class)) {
                aggregate.handle((ProductNameUpdatedEvent) productOb);
            }

            if (productClass.equals(ProductNameUpdatedEvent.class)) {
                aggregate.handle((ProductNameUpdatedEvent) productOb);
            }
            
            if (productClass.equals(ProductDescriptionUpdatedEvent.class)) {
                aggregate.handle((ProductDescriptionUpdatedEvent) productOb);
            }

            if (productClass.equals(ProductPriceUpdatedEvent.class)) {
                aggregate.handle((ProductPriceUpdatedEvent) productOb);
            }

            if (productClass.equals(ProductCategoryAddedEvent.class)) {
                aggregate.handle((ProductCategoryAddedEvent) productOb);
            }

            if (productClass.equals(ProductOutOfStock.class)) {
                aggregate.handle((ProductOutOfStock) productOb);
            }

            if (productClass.equals(ProductStockChanged.class)) {
                aggregate.handle((ProductStockChanged) productOb);
            }
        }

        return aggregate;
    }
}
