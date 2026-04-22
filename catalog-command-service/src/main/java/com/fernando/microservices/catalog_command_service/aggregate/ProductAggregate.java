package com.fernando.microservices.catalog_command_service.aggregate;

import java.util.List;

import com.fernando.microservices.catalog_command_service.entity.ProductStatus;
import com.fernando.microservices.common_service.events.CategoryInfo;
import com.fernando.microservices.common_service.events.ProductStockChanged;
import com.fernando.microservices.common_service.events.ProductCategoryAddedEvent;
import com.fernando.microservices.common_service.events.ProductDescriptionUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductNameUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductOutOfStock;
import com.fernando.microservices.common_service.events.ProductPriceUpdatedEvent;
import com.fernando.microservices.common_service.events.ProductPublishedEvent;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductAggregate {
    private String aggregateId;
    private Long userId;
    private String name;
    private String description;
    private Double price;
    private List<String> imageUrls;
    private List<CategoryInfo> categories;
    private ProductStatus status;

    public void handle(ProductPublishedEvent event) {
        this.aggregateId = event.getAggregateId();
        this.userId = event.getUserId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.price = event.getPrice();
        this.imageUrls = event.getImageUrls();
        this.categories = event.getCategories();
        this.status = ProductStatus.CREATED;
    }

    public void handle(ProductNameUpdatedEvent event) {
        this.name = event.getName();
    }

    public void handle(ProductDescriptionUpdatedEvent event) {
        this.description = event.getDescription();
    }

    public void handle(ProductPriceUpdatedEvent event) {
        this.price = event.getPrice();
    }

    public void handle(ProductCategoryAddedEvent event) {
        this.categories.add(event.getCategory());
    }

    public void handle(ProductOutOfStock event) {
        this.status = ProductStatus.OUT_OF_STOCK;
    }

    public void handle (ProductStockChanged event) {
        this.status = ProductStatus.STOCK_AVAILABLE;
    }
}
