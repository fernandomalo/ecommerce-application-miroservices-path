package com.fernando.microservices.user_info_service.services;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.fernando.microservices.user_info_service.dto.ShippingRuleDto;
import com.fernando.microservices.user_info_service.entity.Company;
import com.fernando.microservices.user_info_service.entity.UserInfo;
import com.fernando.microservices.user_info_service.repositories.CompanyRepository;
import com.fernando.microservices.user_info_service.repositories.UserInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private final RestClient restClient;

    public UserInfoServiceImpl(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://ecomod-shipping-rules-service:8080").build();
    }

    @Override
    @Transactional(readOnly = true)
    // @Cacheable(cacheNames = "userInfoCacheById", key = "#userId")
    public UserInfo getInfoByUserId(Long userId) {
        return userInfoRepository.findById(userId)
            .orElse(null);
    }

    @Override
    // @CachePut(cacheNames = "userInfoCacheById", key = "#userId")
    public void aggregateUserInfo(Long userId, UserInfo userInfo) {
        Optional<UserInfo> userInfoOp = userInfoRepository.findById(userId);

        if (userInfoOp.isPresent()) {
            UserInfo user = userInfoOp.get();
            user.setCountry(userInfo.getCountry());
            user.setRegion(userInfo.getRegion());
            user.setCity(userInfo.getCity());
            user.setLocation(userInfo.getLocation());
            user.setPhoneNumber(userInfo.getPhoneNumber());
            userInfoRepository.save(user);
            return;
        }

        UserInfo newUserInfo = new UserInfo(
            userId,
            userInfo.getCountry(),
            userInfo.getRegion(),
            userInfo.getCity(),
            userInfo.getLocation(),
            userInfo.getPhoneNumber(),
            BigInteger.ZERO,
            null
        );

        userInfoRepository.save(newUserInfo);
    }

    @Override
    public ShippingRuleDto getShippingRuleByUserIdAndCompanyId(Long userId, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("This company doesn't exists"));

        UserInfo userInfo = userInfoRepository.findById(userId)
                .orElseGet(null);

        String originZone = company.getCity();
        String destinationZone = userInfo.getCity();

        ShippingRuleDto shippingRuleDto = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/shipping-rules")
                            .queryParam("origin", originZone)
                            .queryParam("destination", destinationZone)
                            .build())
                        .retrieve()
                        .toEntity(ShippingRuleDto.class)
                        .getBody();

        return shippingRuleDto;

    }

    // @Override
    // public void updateInfo(Long userId, UserInfo userInfo) {
    //     UserInfo user = userInfoRepository.findById(userId)
    //         .orElseThrow(() -> new RuntimeException("User not found by that id"));

    //     user.setCountry(userInfo.getCountry());
    //     user.setRegion(userInfo.getRegion());
    //     user.setCity(userInfo.getCity());
    //     user.setLocation(userInfo.getLocation());
    //     user.setPhoneNumber(userInfo.getPhoneNumber());
    //     userInfoRepository.save(user);
    // }

}
