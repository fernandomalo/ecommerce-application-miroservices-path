package com.fernando.microservices.common_service.events;

public class ProductOutOfStock {
    private String aggregateId;

    public ProductOutOfStock() {
    }

    public ProductOutOfStock(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    
}
