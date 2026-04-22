package com.fernando.microservices.common_service.events;

public class ProductCategoryAddedEvent {
    private String aggregateId ;
    private CategoryInfo category;
    
    public ProductCategoryAddedEvent() {
    }

    public ProductCategoryAddedEvent(String aggregateId, CategoryInfo category) {
        this.aggregateId = aggregateId;
        this.category = category;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public CategoryInfo getCategory() {
        return category;
    }

    public void setCategory(CategoryInfo category) {
        this.category = category;
    }

    
}
