package com.example.myapp.controller;

import com.example.myapp.dto.ApiResponse;
import com.example.myapp.dto.AuthRequest;
import com.example.myapp.dto.AuthResponse;
import com.example.myapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        AuthResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
