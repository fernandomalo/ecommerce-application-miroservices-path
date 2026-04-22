package com.fernando.microservices.payment_service.controllers;

import com.fernando.microservices.payment_service.dto.*;
import com.fernando.microservices.payment_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of("publicKey", paymentService.getWompiPublicKey()));
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentStatusResponse> initiatePayment(
            @RequestHeader("X-User-Id") String userIdString,
            @RequestParam Long orderId,
            @RequestBody InitiatePaymentRequest request) {
        Long userId = Long.parseLong(userIdString);
        return ResponseEntity.ok(paymentService.initiatePayment(userId, orderId, request));
    }

    @GetMapping("/status")
    public ResponseEntity<PaymentStatusResponse> getStatusByReference(
            @RequestParam String reference) {
        return ResponseEntity.ok(paymentService.getPaymentStatusByReference(reference));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getStatusByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentStatusByOrderId(orderId));
    }

    @GetMapping("/pending")
    public ResponseEntity<PaymentStatusResponse> getPendingPayment(
            @RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        return ResponseEntity.ok(paymentService.getPendingPaymentByUserId(userId));
    }
}