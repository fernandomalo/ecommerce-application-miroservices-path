package com.fernando.microservices.order_service.services;

import org.springframework.jms.core.JmsClient;
import org.springframework.stereotype.Service;

import com.fernando.microservices.common_service.events.order_events.CreatePaymentEvent;
import com.fernando.microservices.common_service.events.order_events.OrderConfirmedEvent;
import com.fernando.microservices.common_service.events.order_events.OrderNotCreatedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JmsService {
    
    private final JmsClient jmsClient;

    public void sendToCart(OrderNotCreatedEvent orderNotCreatedEvent) {
        jmsClient.destination("error.order.queue")
            .send(orderNotCreatedEvent);
    }

    public void sendToPayment(CreatePaymentEvent createPaymentEvent) {
        jmsClient.destination("create.payment.queue")
            .send(createPaymentEvent);
    }

    public void sendOrderConfirmed(OrderConfirmedEvent orderConfirmedEvent) {
        jmsClient.destination("confirmed.order.queue")
            .send(orderConfirmedEvent);
    }
}
