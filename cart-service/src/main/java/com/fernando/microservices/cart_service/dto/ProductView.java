package com.fernando.microservices.cart_service.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class ProductView {
    
    private String id;
    private Long companyId;

    private String companyName;
    private String companySlug;

    private String name;
    private String description;
    private Double price;
    private List<String> imageUrls;
    private List<String> categories;

    private BigInteger availableStock;

    private LocalDateTime createdAt;
}
