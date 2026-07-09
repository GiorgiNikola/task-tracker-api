package com.giorgi.tasktrackerapi.dto.project;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectResponseDto {

    private Long id;

    private String name;

    private String description;

    private String ownerEmail;
}
