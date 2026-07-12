package com.giorgi.tasktrackerapi.dto.task;

import com.giorgi.tasktrackerapi.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskUpdateDto {

    @NotBlank
    @Size(max = 100, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    private Priority priority;
}
