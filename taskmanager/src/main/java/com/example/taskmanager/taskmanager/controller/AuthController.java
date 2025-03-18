package com.example.taskmanager.taskmanager.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.taskmanager.taskmanager.dto.AuthRequest;
import com.example.taskmanager.taskmanager.dto.AuthResponse;
import com.example.taskmanager.taskmanager.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody AuthRequest registerRequest,
            @RequestParam String email) {
        return ResponseEntity.ok(authService.register(registerRequest, email));
    }
}