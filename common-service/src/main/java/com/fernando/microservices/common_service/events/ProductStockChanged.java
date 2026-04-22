package com.fernando.microservices.common_service.events;

import java.math.BigInteger;

public class ProductStockChanged {
    private String aggregateId;
    private BigInteger availableStock;
    
    public ProductStockChanged() {
    }

    public ProductStockChanged(String aggregateId, BigInteger availableStock) {
        this.aggregateId = aggregateId;
        this.availableStock = availableStock;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }
    public BigInteger getAvailableStock() {
        return availableStock;
    }
    public void setAvailableStock(BigInteger availableStock) {
        this.availableStock = availableStock;
    }
}
