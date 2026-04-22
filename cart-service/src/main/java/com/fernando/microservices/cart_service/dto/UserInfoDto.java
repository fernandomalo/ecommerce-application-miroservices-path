package com.fernando.microservices.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    
    private String country;
    private String region;
    private String city;
    private String location;
    private String phoneNumber;
}
