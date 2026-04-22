package com.fernando.microservices.user_info_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.user_info_service.dto.ShippingRuleDto;
import com.fernando.microservices.user_info_service.entity.UserInfo;
import com.fernando.microservices.user_info_service.services.UserInfoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Slf4j
public class UserInfoController {
    
    private final UserInfoService userInfoService;

    @GetMapping("/info")
    public UserInfo getUserInfoById(@RequestHeader("X-User-Id") String userIdString) {
        Long userId = Long.parseLong(userIdString);
        System.out.println("userId: " + userIdString);
        
        UserInfo userInfo = userInfoService.getInfoByUserId(userId);

        if (userInfo == null) {
            System.out.println("User has no user information");
        }

        return userInfo;
    }

    @GetMapping("/shipping-price")
    public ShippingRuleDto getShippingRule(@RequestHeader("X-User-Id") String userIdString, @RequestParam Long companyId) {
        Long userId = Long.parseLong(userIdString);
        System.out.println("The user requesting the shipping price: " + userIdString);
        if (userIdString == null) {
            log.info("The userId is null");
            return null;
        }

        return userInfoService.getShippingRuleByUserIdAndCompanyId(userId, companyId);
    }

    // @PutMapping("/update-info/{userId}")
    // public ResponseEntity<?> updateUserInfo(@PathVariable Long userId, @RequestBody UserInfo userInfo) {
    //     userInfoService.updateInfo(userId, userInfo);
    //     return ResponseEntity.ok("User information updated successfully");
    // }

    @PostMapping("/add-info")
    public ResponseEntity<?> registerInformation(@RequestHeader("X-User-Id") String userIdString, @RequestBody UserInfo userInfo) {
        Long userId = Long.parseLong(userIdString);
        System.out.println("user-id: " + userIdString);

        userInfoService.aggregateUserInfo(userId, userInfo);
        return ResponseEntity.ok("Info aggregated correctly");
    }
}
