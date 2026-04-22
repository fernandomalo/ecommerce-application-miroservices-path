package com.fernando.microservices.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusResponse {
    private String status;
    private String wompiTransactionId;
    private String reference;
    private String paymentMethod;
    private Double amount;
    private Long orderId;
    private String asyncPaymentUrl;  // PSE bank redirect, null for others
}