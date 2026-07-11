package com.giorgi.tasktrackerapi.controller;

import com.giorgi.tasktrackerapi.dto.user.UserResponseDto;
import com.giorgi.tasktrackerapi.dto.user.UserRoleUpdateRequest;
import com.giorgi.tasktrackerapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDto> updateUserRole(@PathVariable Long id,
                                                          @Valid @RequestBody UserRoleUpdateRequest request) {
        UserResponseDto responseDto =userService.updateUserRole(id, request);
        return ResponseEntity.ok(responseDto);
    }
}
