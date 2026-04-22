package com.fernando.microservices.inventory_service.services;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fernando.microservices.common_service.events.ProductStockChanged;

@Service
public class RestClientService {
    
    private final RestClient restClient;

    public RestClientService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://ecomod-api-gateway:8080").build();
    }

    public void changeAvailableStockForProductView(ProductStockChanged productStockChanged) {
        restClient.put()
            .uri("/api/v1/products/see/change-stock/{id}", productStockChanged.getAggregateId())
            .body(productStockChanged)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .toBodilessEntity();
    }
}
