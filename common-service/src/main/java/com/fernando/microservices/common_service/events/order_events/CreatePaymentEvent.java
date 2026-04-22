package com.fernando.microservices.common_service.events.order_events;

public class CreatePaymentEvent {
    private Long userId;
    private Long orderId;
    private Double amount;
    
    public CreatePaymentEvent() {
    }

    public CreatePaymentEvent(Long userId, Long orderId, Double amount) {
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
}
