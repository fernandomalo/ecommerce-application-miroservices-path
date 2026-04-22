package com.fernando.microservices.cart_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fernando.microservices.cart_service.dto.CompanyDto;
import com.fernando.microservices.cart_service.dto.UserInfoDto;

@FeignClient(name = "ecomod-user-info-service", url = "http://ecomod-user-info-service:8080")
public interface OpenFeignUserInfo {
    
    @GetMapping("/api/v1/users/info")
    UserInfoDto getUserInfoByUserId(@RequestHeader("X-User-Id") String userIdString);

    @GetMapping("/api/v1/companies/id/{id}")
    CompanyDto getCompanyById(@PathVariable Long id);
}
