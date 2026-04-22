package com.fernando.microservices.auth_service.controllers;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fernando.microservices.auth_service.dto.LoginRequest;
import com.fernando.microservices.auth_service.dto.SignupRequest;
import com.fernando.microservices.auth_service.entity.UserInfo;
import com.fernando.microservices.auth_service.entity.UserStatus;
import com.fernando.microservices.auth_service.security.jwt.JwtUtils;
import com.fernando.microservices.auth_service.security.service.UserDetailsImpl;
import com.fernando.microservices.auth_service.security.service.UserDetailsServiceImpl;
import com.fernando.microservices.auth_service.services.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            userService.registerUser(signupRequest);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not register this user " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication;
        try {
            UserInfo user = userService.getUserByEmail(loginRequest.getEmail());
            if (user.getStatus().equals(UserStatus.NOT_VERIFIED)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not verified");
            }
        } catch (Exception e) {
            System.out.println("User not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User can't be authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = userService.generateAccessToken(userDetails);
        String refreshToken = userService.generateRefreshToken(userDetails);

        String roles = userDetails.getAuthorities()
                .stream()
                .map(r -> r.getAuthority())
                .findFirst()
                .get();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/auth/refresh")
                .sameSite("Lax")
                .maxAge(Duration.ofMinutes(10))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "roles", roles));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Cookie is empty");
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        System.out.println("refresh-token: " + refreshToken);

        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("refreshToken not valid");
        }

        String email = jwtUtils.getEmailByToken(refreshToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(email);

        String accessToken = userService.generateAccessToken(userDetails);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie logoutCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("auth/refresh")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, logoutCookie.toString());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-status/{id}")
    public ResponseEntity<?> changeStatusToBusiness(@PathVariable Long id) {
        userService.changeStatusToBusiness(id);
        return ResponseEntity.ok("Role changed successfully");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUserEmail(@RequestParam(name = "code") Integer verificationCode) {
        userService.verifyUser(verificationCode);
        return ResponseEntity.ok("User verified successfully");
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendVerificationCode(@RequestParam(name = "email") String email) {
        userService.resendVerificationCode(email);
        return ResponseEntity.ok("Verification code resent successfully");
    }
}
