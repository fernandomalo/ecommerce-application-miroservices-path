package com.fernando.microservices.payment_service.dto;

import lombok.Data;

@Data
public class UserInfoDto {
    private Long userId;
    private String country;
    private String region;
    private String city;
    private String location;
    private String phoneNumber;
}