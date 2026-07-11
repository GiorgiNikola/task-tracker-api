package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.user.AuthResponseDto;
import com.giorgi.tasktrackerapi.dto.user.LoginRequest;
import com.giorgi.tasktrackerapi.dto.user.UserRegistrationRequest;
import com.giorgi.tasktrackerapi.dto.user.UserResponseDto;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.enums.Role;
import com.giorgi.tasktrackerapi.exception.DuplicateResourceException;
import com.giorgi.tasktrackerapi.exception.InvalidCredentialsException;
import com.giorgi.tasktrackerapi.exception.ResourceNotFoundException;
import com.giorgi.tasktrackerapi.mapper.UserMapper;
import com.giorgi.tasktrackerapi.repository.UserRepository;
import com.giorgi.tasktrackerapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserResponseDto register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public AuthResponseDto login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        AuthResponseDto responseDto = new AuthResponseDto();
        responseDto.setToken(jwtUtil.generateToken(user.getEmail(), user.getRole()));
        return responseDto;
    }
}
