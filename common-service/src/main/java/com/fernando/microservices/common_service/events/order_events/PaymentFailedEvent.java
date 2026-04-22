package com.fernando.microservices.common_service.events.order_events;

public class PaymentFailedEvent {
    private Long orderId;
    private Long userId;
    
    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(Long orderId, Long userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
}
