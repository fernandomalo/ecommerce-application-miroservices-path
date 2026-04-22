package com.fernando.microservices.catalog_command_service.dto;

import java.util.List;

import com.fernando.microservices.common_service.events.CategoryInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductRequest {
    
    private String name;
    private String description;
    private Double price;
    private List<String> imageUrls;
    private List<Long> categoryIds;
}
