package com.fernando.microservices.common_service.events.order_events;

public class PaymentSuccessfulEvent {
    private Long userId;
    private Long orderId;
    
    public PaymentSuccessfulEvent() {
    }

    public PaymentSuccessfulEvent(Long userId, Long orderId) {
        this.userId = userId;
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
}
