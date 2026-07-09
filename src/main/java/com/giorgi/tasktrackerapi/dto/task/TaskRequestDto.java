package com.giorgi.tasktrackerapi.dto.task;

import com.giorgi.tasktrackerapi.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Long projectId;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private Priority priority;

    private Long assignedUserId;
}
