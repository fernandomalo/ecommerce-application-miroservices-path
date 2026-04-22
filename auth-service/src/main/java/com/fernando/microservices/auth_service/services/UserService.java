package com.fernando.microservices.auth_service.services;

import com.fernando.microservices.auth_service.dto.SignupRequest;
import com.fernando.microservices.auth_service.entity.UserInfo;
import com.fernando.microservices.auth_service.security.service.UserDetailsImpl;

public interface UserService {
    
    void registerUser(SignupRequest signupRequest);
    String generateAccessToken(UserDetailsImpl userDetails);
    String generateRefreshToken(UserDetailsImpl userDetails);
    void changeStatusToBusiness(Long id);
    void verifyUser(Integer verificationCode);
    void resendVerificationCode(String email);
    UserInfo getUserByEmail(String email);
}
