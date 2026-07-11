package com.giorgi.tasktrackerapi.dto.user;

import com.giorgi.tasktrackerapi.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleUpdateRequest {
    @NotNull
    private Role role;
}
