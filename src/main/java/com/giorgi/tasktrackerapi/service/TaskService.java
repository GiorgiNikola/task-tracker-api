package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.task.*;
import com.giorgi.tasktrackerapi.entity.Task;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.enums.Priority;
import com.giorgi.tasktrackerapi.enums.TaskStatus;
import com.giorgi.tasktrackerapi.exception.ResourceNotFoundException;
import com.giorgi.tasktrackerapi.mapper.TaskMapper;
import com.giorgi.tasktrackerapi.repository.TaskRepository;
import com.giorgi.tasktrackerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TaskService {

    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isProjectOwner(#request.projectId, authentication)")
    public TaskResponseDto createTask(TaskRequestDto request) {
        Task task = taskMapper.toEntity(request);
        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            task.setAssignedUser(user);
        }
        task.setStatus(TaskStatus.TODO);
        taskRepository.save(task);
        return taskMapper.toResponseDto(task);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskProjectOwnerOrAssignedUser(#taskId, authentication)")
    public TaskResponseDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return taskMapper.toResponseDto(task);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskProjectOwnerOrAssignedUser(#taskId, authentication)")
    public TaskResponseDto updateTask(Long taskId, TaskUpdateDto request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());

        return taskMapper.toResponseDto(task);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskProjectOwner(#taskId, authentication)")
    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }
        taskRepository.deleteById(taskId);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isTaskProjectOwner(#taskId, authentication)")
    public TaskResponseDto assignTask(Long taskId, TaskAssignDto request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        task.setAssignedUser(user);
        return taskMapper.toResponseDto(task);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskSecurity.isAssignedUser(#taskId, authentication)")
    public TaskResponseDto updateTaskStatus(Long taskId, TaskStatusUpdateDto statusUpdateDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        task.setStatus(statusUpdateDto.getStatus());
        return taskMapper.toResponseDto(task);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isProjectOwner(#projectId, authentication)")
    public Page<TaskResponseDto> getProjectTasks(Long projectId,
                                                 TaskStatus status,
                                                 Priority priority,
                                                 Pageable pageable) {
        return taskRepository.findByProjectIdWithFilter(
                projectId,
                status,
                priority,
                pageable
        ).map(taskMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<TaskResponseDto> getUserTasks(TaskStatus status,
                                              Priority priority,
                                              Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
        Long userId = user.getId();
        return taskRepository.findByAssignedUserIdWithFilter(
                userId,
                status,
                priority,
                pageable
        ).map(taskMapper::toResponseDto);
    }
}
