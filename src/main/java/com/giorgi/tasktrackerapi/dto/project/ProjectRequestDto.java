package com.giorgi.tasktrackerapi.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequestDto {

    @NotBlank
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
