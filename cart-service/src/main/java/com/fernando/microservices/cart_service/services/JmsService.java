package com.fernando.microservices.cart_service.services;

import org.springframework.jms.core.JmsClient;
import org.springframework.stereotype.Service;

import com.fernando.microservices.common_service.events.order_events.CreateOrderEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JmsService {
    
    private final JmsClient jmsClient;

    public void sendEvent(CreateOrderEvent createOrderEvent) {
        jmsClient.destination("create.order.queue")
            .send(createOrderEvent);
    }
}
