package com.example.employeemanagement.controller;

import java.security.Principal;
import java.util.Map;

import com.example.employeemanagement.dto.RegisterRequest;
import com.example.employeemanagement.dto.LoginRequest;
import com.example.employeemanagement.service.JwtTokenService;
import com.example.employeemanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    private final JwtTokenService tokens;

    public AuthController(AuthService auth, JwtTokenService tokens) {
        this.auth = auth;
        this.tokens = tokens;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("username", auth.register(request), "role", "USER"));
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        return Map.of("username", principal.getName());
    }

    @PostMapping("/token")
    public Map<String, Object> token(@Valid @RequestBody LoginRequest request) {
        return tokens.login(request);
    }
}
