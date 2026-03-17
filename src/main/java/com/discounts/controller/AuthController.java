package com.discounts.controller;

import com.discounts.dto.LoginRequest;
import com.discounts.dto.LoginResponse;
import com.discounts.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = this.authService.login(request);
        return ResponseEntity.ok(response);
    }
}
