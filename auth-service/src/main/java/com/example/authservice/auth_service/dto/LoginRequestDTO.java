package com.example.authservice.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(size = 8, message = "Password must be 8 characters long")
    private String password;

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String value) {
        this.password = value;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String value) {
        this.email = value;
    }
}
