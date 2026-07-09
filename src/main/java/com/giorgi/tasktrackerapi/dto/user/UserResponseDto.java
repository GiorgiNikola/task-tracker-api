package com.giorgi.tasktrackerapi.dto.user;

import com.giorgi.tasktrackerapi.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDto {

    private Long id;

    private String email;

    private Role role;

    private LocalDateTime createDate;
}
