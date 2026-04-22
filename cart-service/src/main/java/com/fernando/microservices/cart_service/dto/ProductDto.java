package com.fernando.microservices.cart_service.dto;

import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
public class ProductDto {
    
    private String productId;
    private List<String> images;
    private String name;
    private Double price;
}
