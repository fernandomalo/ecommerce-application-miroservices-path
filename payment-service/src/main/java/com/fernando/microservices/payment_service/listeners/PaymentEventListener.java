package com.fernando.microservices.payment_service.listeners;

import com.fernando.microservices.common_service.events.order_events.CreatePaymentEvent;
import com.fernando.microservices.payment_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @JmsListener(destination = "create.payment.queue")
    public void onCreatePayment(CreatePaymentEvent event) {
        log.info("Received CreatePaymentEvent: orderId={} userId={} amount={}",
                event.getOrderId(), event.getUserId(), event.getAmount());
        paymentService.handleCreatePaymentEvent(
                event.getUserId(), event.getOrderId(), event.getAmount());
    }
}