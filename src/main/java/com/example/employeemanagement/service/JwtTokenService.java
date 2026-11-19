package com.example.employeemanagement.service;

import java.time.Instant;
import java.util.Map;
import com.example.employeemanagement.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JwtTokenService {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;

    public JwtTokenService(AuthenticationManager authenticationManager, JwtEncoder encoder) {
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
    }

    public Map<String, Object> login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword()));
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("employee-management")
                    .subject(authentication.getName())
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(1800))
                    .claim("roles", authentication.getAuthorities().stream()
                            .map(authority -> authority.getAuthority().substring("ROLE_".length())).toList())
                    .build();
            String token = encoder.encode(JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
            return Map.of("accessToken", token, "tokenType", "Bearer", "expiresIn", 1800);
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }
    }
}
