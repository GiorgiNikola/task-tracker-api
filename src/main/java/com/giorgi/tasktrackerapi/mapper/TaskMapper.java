package com.giorgi.tasktrackerapi.mapper;

import com.giorgi.tasktrackerapi.dto.task.TaskRequestDto;
import com.giorgi.tasktrackerapi.dto.task.TaskResponseDto;
import com.giorgi.tasktrackerapi.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(source = "projectId", target = "project.id")
    Task toEntity(TaskRequestDto requestDto);
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "assignedUser.email", target = "assignedUserEmail")
    @Mapping(source = "assignedUser.id", target = "assignedUserId")
    TaskResponseDto toResponseDto(Task task);
}
