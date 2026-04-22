package com.fernando.microservices.common_service.events;

public class ProductNameUpdatedEvent {
    private String aggregateId;
    private String name;

    public ProductNameUpdatedEvent() {
    }

    public ProductNameUpdatedEvent(String aggregateId, String name) {
        this.aggregateId = aggregateId;
        this.name = name;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
