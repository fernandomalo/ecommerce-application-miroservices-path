package com.fernando.microservices.common_service.events.order_events;

import java.util.List;
public class CreateOrderEvent {
    
    private Long userId;
    private List<Item> items;
    private Double totalAmount;
    
    public CreateOrderEvent() {
    }

    public CreateOrderEvent(Long userId, List<Item> items, Double totalAmount) {
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    
}
