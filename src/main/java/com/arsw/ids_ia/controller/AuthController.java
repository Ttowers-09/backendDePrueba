package com.arsw.ids_ia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.arsw.ids_ia.dto.ApiResponse;
import com.arsw.ids_ia.dto.JwtAuthenticationResponse;
import com.arsw.ids_ia.dto.LoginRequest;
import com.arsw.ids_ia.dto.RefreshTokenRequest;
import com.arsw.ids_ia.dto.SignUpRequest;
import com.arsw.ids_ia.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String jwt = authService.authenticateUser(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword()
            );

            String refreshToken = authService.generateRefreshToken(loginRequest.getUsernameOrEmail());

            return ResponseEntity.ok(new JwtAuthenticationResponse(
                    jwt,
                    refreshToken,
                    (long) jwtExpirationInMs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid username/email or password"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            if (authService.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Username is already taken!"));
            }

            if (authService.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Email Address already in use!"));
            }

            authService.registerUser(
                    signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    signUpRequest.getPassword(),
                    signUpRequest.getFirstName(),
                    signUpRequest.getLastName(),
                    signUpRequest.getRole()
            );

            return ResponseEntity.ok(new ApiResponse(true, "User registered successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            String newToken = authService.refreshToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.ok(new JwtAuthenticationResponse(
                    newToken,
                    refreshTokenRequest.getRefreshToken(),
                    (long) jwtExpirationInMs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid refresh token"));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        Boolean isAvailable = !authService.existsByUsername(username);
        return ResponseEntity.ok(new ApiResponse(isAvailable,
                isAvailable ? "Username is available" : "Username is already taken"));
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        Boolean isAvailable = !authService.existsByEmail(email);
        return ResponseEntity.ok(new ApiResponse(isAvailable,
                isAvailable ? "Email is available" : "Email is already in use"));
    }
}
