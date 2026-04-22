package com.fernando.microservices.common_service.events.order_events;

public class OrderNotCreatedEvent {
    private Long userId;

    public OrderNotCreatedEvent() {
    }

    public OrderNotCreatedEvent(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
}
