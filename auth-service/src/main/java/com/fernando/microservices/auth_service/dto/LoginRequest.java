package com.fernando.microservices.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    
    @Email
    private String email;

    @Size(min = 4)
    @NotNull
    private String password;
}
