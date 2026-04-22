package com.fernando.microservices.payment_service.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fernando.microservices.common_service.events.order_events.PaymentFailedEvent;
import com.fernando.microservices.common_service.events.order_events.PaymentSuccessfulEvent;
import com.fernando.microservices.payment_service.entitty.PaymentStatus;
import com.fernando.microservices.payment_service.repository.PaymentRepository;
import com.fernando.microservices.payment_service.services.JmsService;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/webhook")
public class WompiWebhookController {

    private final PaymentRepository paymentRepository;
    private final JmsService jmsService;

    @SuppressWarnings("unchecked")
    @PostMapping
    public ResponseEntity<?> handleWompiEvent(@RequestBody Map<String, Object> payload) {
        try {
            String eventType = (String) payload.get("event");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> transaction = (Map<String, Object>) data.get("transaction");

            String reference = (String) transaction.get("reference");
            String status = (String) transaction.get("status");
            String wompiId = (String) transaction.get("id");

            log.info("Wompi webhook — event={} ref={} status={}", eventType, reference, status);

            paymentRepository.findByWompiReference(reference).ifPresent(payment -> {
                PaymentStatus mapped = mapStatus(status);
                payment.setWompiTransactionId(wompiId);
                payment.setStatus(mapped);
                paymentRepository.save(payment);

                if (mapped == PaymentStatus.APPROVED) {
                    jmsService.sendToOrder(
                            new PaymentSuccessfulEvent(payment.getUserId(), payment.getOrderId()));
                } else if (mapped == PaymentStatus.DECLINED || mapped == PaymentStatus.ERROR) {
                    jmsService.sentToQueue(
                            new PaymentFailedEvent(payment.getOrderId(), payment.getUserId()));
                }
            });

        } catch (Exception e) {
            log.error("Error processing Wompi webhook", e);
        }
        return ResponseEntity.ok().build();
    }

    private PaymentStatus mapStatus(String s) {
        if (s == null)
            return PaymentStatus.PENDING;
        return switch (s.toUpperCase()) {
            case "APPROVED" -> PaymentStatus.APPROVED;
            case "DECLINED" -> PaymentStatus.DECLINED;
            case "VOIDED" -> PaymentStatus.VOIDED;
            case "ERROR" -> PaymentStatus.ERROR;
            default -> PaymentStatus.PENDING;
        };
    }
}