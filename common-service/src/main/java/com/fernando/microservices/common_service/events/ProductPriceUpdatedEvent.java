package com.fernando.microservices.common_service.events;

public class ProductPriceUpdatedEvent {
    private String aggregateId ;
    private Double price;

    public ProductPriceUpdatedEvent() {
    }

    public ProductPriceUpdatedEvent(String aggregateId, Double price) {
        this.aggregateId = aggregateId;
        this.price = price;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
