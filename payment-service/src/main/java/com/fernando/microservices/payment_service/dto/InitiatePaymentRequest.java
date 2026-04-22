package com.fernando.microservices.payment_service.dto;

import lombok.Data;

@Data
public class InitiatePaymentRequest {
    private String paymentMethod;
    private String email; 

    private String acceptanceToken;

    // CARD
    private String cardToken;
    private Integer installments;

    // PSE
    private String financialInstitutionCode;
    private String userType;
    private String userLegalId;
    private String userLegalIdType;

    // NEQUI
    private String phoneNumber;
}