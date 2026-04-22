package com.fernando.microservices.user_info_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRuleDto {
    
    private Long id;
    private String originZone;
    private String destinationZone;
    private Double price;
    private Integer durationTime;
}