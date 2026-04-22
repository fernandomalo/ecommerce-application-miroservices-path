package com.fernando.microservices.payment_service.services;

import com.fernando.microservices.payment_service.dto.*;

public interface PaymentService {
    void handleCreatePaymentEvent(Long userId, Long orderId, Double amount);
    PaymentStatusResponse initiatePayment(Long userId, Long orderId, InitiatePaymentRequest request);
    PaymentStatusResponse getPaymentStatusByReference(String reference);
    PaymentStatusResponse getPaymentStatusByOrderId(Long orderId);
    String getWompiPublicKey();
    PaymentStatusResponse getPendingPaymentByUserId(Long userId);
}