package com.fernando.microservices.payment_service.services;

import org.springframework.jms.core.JmsClient;
import org.springframework.stereotype.Service;

import com.fernando.microservices.common_service.events.order_events.PaymentFailedEvent;
import com.fernando.microservices.common_service.events.order_events.PaymentSuccessfulEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JmsService {
    
    private final JmsClient jmsClient;

    public void sentToQueue(PaymentFailedEvent paymentFailedEvent) {
        jmsClient.destination("payment.failed.queue")
            .send(paymentFailedEvent);
    }

    public void sendToOrder(PaymentSuccessfulEvent paymentSuccessfulEvent) {
        jmsClient.destination("payment.success.queue")
            .send(paymentSuccessfulEvent);
    }
}
