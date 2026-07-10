package com.giorgi.tasktrackerapi.mapper;

import com.giorgi.tasktrackerapi.dto.project.ProjectRequestDto;
import com.giorgi.tasktrackerapi.dto.project.ProjectResponseDto;
import com.giorgi.tasktrackerapi.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toEntity(ProjectRequestDto requestDto);
    @Mapping(source = "owner.email", target = "ownerEmail")
    ProjectResponseDto toResponseDto(Project project);
}
