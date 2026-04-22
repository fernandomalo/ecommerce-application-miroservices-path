package com.fernando.microservices.common_service.events.order_events;

public class OrderConfirmedEvent {
    private Long userId;

    public OrderConfirmedEvent() {
    }

    public OrderConfirmedEvent(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
