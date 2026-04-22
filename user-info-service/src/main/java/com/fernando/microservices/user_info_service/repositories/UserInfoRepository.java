package com.fernando.microservices.user_info_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fernando.microservices.user_info_service.entity.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
}
