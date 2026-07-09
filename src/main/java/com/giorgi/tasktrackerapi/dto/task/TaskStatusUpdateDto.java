package com.giorgi.tasktrackerapi.dto.task;

import com.giorgi.tasktrackerapi.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDto {

    @NotNull
    private TaskStatus status;
}
