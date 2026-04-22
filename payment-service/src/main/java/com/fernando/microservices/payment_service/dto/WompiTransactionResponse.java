package com.fernando.microservices.payment_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WompiTransactionResponse {

    private DataWrapper data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataWrapper {
        private String id;
        private String status;
        private String reference;

        @JsonProperty("payment_method_type")
        private String paymentMethodType;

        @JsonProperty("payment_method")
        private PaymentMethodData paymentMethod;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentMethodData {
        private ExtraData extra;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtraData {
        @JsonProperty("async_payment_url")
        private String asyncPaymentUrl;
    }
}