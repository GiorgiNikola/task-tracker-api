package com.giorgi.tasktrackerapi.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAssignDto {

    @NotNull
    private Long assignedUserId;
}
