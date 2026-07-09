package com.giorgi.tasktrackerapi.dto.project;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String description;
}
