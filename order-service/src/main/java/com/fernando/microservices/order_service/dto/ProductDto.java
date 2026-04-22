package com.fernando.microservices.order_service.dto;

import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class ProductDto {
    
    private String productId;
    private List<String> images;
    private String name;
    private Double price;
}
