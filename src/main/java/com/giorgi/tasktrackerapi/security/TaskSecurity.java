package com.giorgi.tasktrackerapi.security;

import com.giorgi.tasktrackerapi.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TaskSecurity {

    private final TaskRepository taskRepository;

    public boolean isTaskProjectOwnerOrAssignedUser(Long taskId, Authentication auth) {
        return taskRepository.isProjectOwnerOrAssignedUser(taskId, auth.getName());
    }

    public boolean isAssignedUser(Long taskId, Authentication auth) {
        return taskRepository.existsByIdAndAssignedUserEmail(taskId, auth.getName());
    }

    public boolean isTaskProjectOwner(Long taskId, Authentication auth) {
        return taskRepository.isTaskProjectOwner(taskId, auth.getName());
    }
}
