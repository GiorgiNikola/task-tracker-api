package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.user.AuthResponseDto;
import com.giorgi.tasktrackerapi.dto.user.LoginRequest;
import com.giorgi.tasktrackerapi.dto.user.UserRegistrationRequest;
import com.giorgi.tasktrackerapi.dto.user.UserResponseDto;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.enums.Role;
import com.giorgi.tasktrackerapi.exception.DuplicateResourceException;
import com.giorgi.tasktrackerapi.exception.InvalidCredentialsException;
import com.giorgi.tasktrackerapi.mapper.UserMapper;
import com.giorgi.tasktrackerapi.repository.UserRepository;
import com.giorgi.tasktrackerapi.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationRequest registrationRequest;

    private User user;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("register() should hash password and save user with USER role")
    void register_validRequest_savesUserWithUserRole() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setRole(Role.USER);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(expectedResponse);

        UserResponseDto result = authService.register(registrationRequest);

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("register() should throw DuplicateResourceException when user already exists")
    void register_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(registrationRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login() should return token when credentials are valid")
    void login_validCredentials_returnsToken() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), user.getRole())).thenReturn("mockedToken");

        AuthResponseDto result = authService.login(loginRequest);

        assertThat(result.getToken()).isEqualTo("mockedToken");
    }

    @Test
    @DisplayName("login() should throw InvalidCredentialsException when user not found")
    void login_userNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("login() should throw InvalidCredentialsException when password does not match")
    void login_wrongPassword_throwsInvalidCredentialsException() {

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any(), any());
    }
}