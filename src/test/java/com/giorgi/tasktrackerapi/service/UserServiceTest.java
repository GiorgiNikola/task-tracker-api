package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.user.UserResponseDto;
import com.giorgi.tasktrackerapi.dto.user.UserRoleUpdateRequest;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.enums.Role;
import com.giorgi.tasktrackerapi.exception.ResourceNotFoundException;
import com.giorgi.tasktrackerapi.mapper.UserMapper;
import com.giorgi.tasktrackerapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRoleUpdateRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);

        request = new UserRoleUpdateRequest();
        request.setRole(Role.MANAGER);
    }

    @Test
    @DisplayName("updateUserRole() should update role when user exists")
    void updateUserRole_existingUser_updatesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserResponseDto expectedResponse = new UserResponseDto();
        expectedResponse.setRole(Role.MANAGER);
        when(userMapper.toResponseDto(user)).thenReturn(expectedResponse);

        UserResponseDto result = userService.updateUserRole(1L, request);

        assertThat(result.getRole()).isEqualTo(Role.MANAGER);
        assertThat(user.getRole()).isEqualTo(Role.MANAGER);
    }

    @Test
    @DisplayName("updateUserRole() should throw ResourceNotFoundException when user not found")
    void updateUserRole_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}