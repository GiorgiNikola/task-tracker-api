package com.giorgi.tasktrackerapi.controller;

import com.giorgi.tasktrackerapi.dto.user.AuthResponseDto;
import com.giorgi.tasktrackerapi.dto.user.LoginRequest;
import com.giorgi.tasktrackerapi.dto.user.UserRegistrationRequest;
import com.giorgi.tasktrackerapi.dto.user.UserResponseDto;
import com.giorgi.tasktrackerapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponseDto responseDto = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequest request) {
        AuthResponseDto token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}
