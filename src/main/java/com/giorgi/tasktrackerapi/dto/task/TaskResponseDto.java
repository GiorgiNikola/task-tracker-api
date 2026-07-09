package com.giorgi.tasktrackerapi.dto.task;

import com.giorgi.tasktrackerapi.enums.Priority;
import com.giorgi.tasktrackerapi.enums.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskResponseDto {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private Priority priority;

    private LocalDate dueDate;

    private Long projectId;

    private String assignedUserEmail;

    private Long assignedUserId;
}
