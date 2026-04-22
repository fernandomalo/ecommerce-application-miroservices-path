package com.fernando.microservices.user_info_service.services;

import com.fernando.microservices.user_info_service.dto.ShippingRuleDto;
import com.fernando.microservices.user_info_service.entity.UserInfo;

public interface UserInfoService {
    UserInfo getInfoByUserId(Long userId);
    void aggregateUserInfo(Long userId, UserInfo userInfo);
    ShippingRuleDto getShippingRuleByUserIdAndCompanyId(Long userId, Long companyId);
}
