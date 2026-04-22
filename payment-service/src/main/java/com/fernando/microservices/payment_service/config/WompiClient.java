package com.fernando.microservices.payment_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernando.microservices.payment_service.dto.WompiTransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WompiClient {

    private final WompiProperties properties;
    private final RestTemplate restTemplate;

    public WompiTransactionResponse createTransaction(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getPrivateKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        log.info("Sending transaction to Wompi: {}", body);

        ResponseEntity<WompiTransactionResponse> response = restTemplate.exchange(
            properties.getBaseUrl() + "/transactions",
            HttpMethod.POST,
            entity,
            WompiTransactionResponse.class
        );

        return response.getBody();
    }

    public WompiTransactionResponse getTransaction(String transactionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getPrivateKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<WompiTransactionResponse> response = restTemplate.exchange(
            properties.getBaseUrl() + "/transactions/" + transactionId,
            HttpMethod.GET,
            entity,
            WompiTransactionResponse.class
        );

        return response.getBody();
    }
}