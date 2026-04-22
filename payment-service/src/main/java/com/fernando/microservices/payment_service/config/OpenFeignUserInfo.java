package com.fernando.microservices.payment_service.config;

import com.fernando.microservices.payment_service.dto.UserInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ecomod-user-info-service", url = "http://ecomod-user-info-service:8080")
public interface OpenFeignUserInfo {

    @GetMapping("/api/v1/users/info")
    UserInfoDto getUserInfo(@RequestHeader("X-User-Id") String userId);
}