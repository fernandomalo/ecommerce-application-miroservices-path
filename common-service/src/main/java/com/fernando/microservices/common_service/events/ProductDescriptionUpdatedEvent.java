package com.fernando.microservices.common_service.events;

public class ProductDescriptionUpdatedEvent {
    private String aggregateId;
    private String description;
    
    public ProductDescriptionUpdatedEvent() {
    }

    public ProductDescriptionUpdatedEvent(String aggregateId, String description) {
        this.aggregateId = aggregateId;
        this.description = description;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
}
