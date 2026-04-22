package com.fernando.microservices.auth_service.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fernando.microservices.auth_service.dto.SignupRequest;
import com.fernando.microservices.auth_service.entity.AppRole;
import com.fernando.microservices.auth_service.entity.UserInfo;
import com.fernando.microservices.auth_service.entity.UserRole;
import com.fernando.microservices.auth_service.entity.UserStatus;
import com.fernando.microservices.auth_service.entity.UserVerifier;
import com.fernando.microservices.auth_service.exceptions.DatabaseException;
import com.fernando.microservices.auth_service.repositories.UserRepository;
import com.fernando.microservices.auth_service.repositories.UserRoleRepository;
import com.fernando.microservices.auth_service.repositories.UserVerifierRepository;
import com.fernando.microservices.auth_service.security.jwt.JwtUtils;
import com.fernando.microservices.auth_service.security.service.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder;
    private final UserVerifierRepository userVerifierRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void registerUser(SignupRequest signupRequest) {
        UserRole roleCustomer = roleRepository.findByRole(AppRole.CUSTOMER)
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setRole(AppRole.CUSTOMER);
                    return roleRepository.save(role);
                });
        UserRole roleBusiness = roleRepository.findByRole(AppRole.BUSINESS)
                .orElseGet(() -> {
                    UserRole role = new UserRole();
                    role.setRole(AppRole.BUSINESS);
                    return roleRepository.save(role);
                });

        UserInfo user = new UserInfo();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(encoder.encode(signupRequest.getPassword()));
        user.setStatus(UserStatus.NOT_VERIFIED);
        user.setRole(roleCustomer);
        user.setCreatedAt(LocalDateTime.now());

        Integer verificationCode = codeGenerator();
        Instant expiryDate = Instant.now().plus(Duration.ofMinutes(5));

        UserVerifier verifier = new UserVerifier(verificationCode, expiryDate, user);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("Could not connect to the database " + e);
        }

        userVerifierRepository.save(verifier);

        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ print full stack trace, not just message
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }

    }

    @Override
    public String generateAccessToken(UserDetailsImpl userDetails) {
        return jwtUtils.createAccessToken(userDetails);
    }

    @Override
    public String generateRefreshToken(UserDetailsImpl userDetails) {
        return jwtUtils.createRefreshToken(userDetails);
    }

    @Override
    @Transactional
    public void changeStatusToBusiness(Long id) {
        UserInfo user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole role = roleRepository.findByRole(AppRole.BUSINESS)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public UserInfo getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Integer codeGenerator() {
        Random random = new Random();

        int min = 100000;
        int max = 999999;

        return random.nextInt((max - min) + 1) + min;
    }

    @Override
    @Transactional
    public void verifyUser(Integer verificationCode) {
        UserVerifier verifier = userVerifierRepository.findByCode(verificationCode)
                .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        if (verifier.isExpired()) {
            throw new RuntimeException("Verification code has expired");
        }

        UserInfo user = verifier.getUser();
        if (user.getStatus().equals(UserStatus.VERIFIED)) {
            throw new RuntimeException("User already verified");
        }

        user.setStatus(UserStatus.VERIFIED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resendVerificationCode(String email) {
        UserInfo user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus().equals(UserStatus.VERIFIED)) {
            throw new RuntimeException("User already verified");
        }

        UserVerifier verifier = userVerifierRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Verification code not found"));

        Integer newCode = codeGenerator();
        Instant newExpiryDate = Instant.now().plus(Duration.ofMinutes(5));
        verifier.setCode(newCode);
        verifier.setExpiryDate(newExpiryDate);
        userVerifierRepository.save(verifier);

        try {
            emailService.sendVerificationEmail(email, newCode);
        } catch (Exception e) {
            e.getStackTrace();
            throw new RuntimeException("Failed to resend verification email: " + e.getMessage());
        }
    }

}
