package com.example.authservice.auth_service.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.authservice.auth_service.dto.LoginRequestDTO;
import com.example.authservice.auth_service.dto.LoginResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
public class AuthController {

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

        if(tokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = tokenOptional.get();
        return ResponseEntity.ok(new LoginResponseDTO(token)):

    }

}
