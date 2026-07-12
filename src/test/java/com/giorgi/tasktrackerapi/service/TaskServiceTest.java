package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.task.*;
import com.giorgi.tasktrackerapi.entity.Project;
import com.giorgi.tasktrackerapi.entity.Task;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.enums.Priority;
import com.giorgi.tasktrackerapi.enums.TaskStatus;
import com.giorgi.tasktrackerapi.exception.ResourceNotFoundException;
import com.giorgi.tasktrackerapi.mapper.TaskMapper;
import com.giorgi.tasktrackerapi.repository.ProjectRepository;
import com.giorgi.tasktrackerapi.repository.TaskRepository;
import com.giorgi.tasktrackerapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User assignedUser;
    private Project project;
    private TaskRequestDto requestDto;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);

        assignedUser = new User();
        assignedUser.setId(2L);
        assignedUser.setEmail("assignee@example.com");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(Priority.MEDIUM);
        task.setProject(project);

        requestDto = new TaskRequestDto();
        requestDto.setTitle("Test Task");
        requestDto.setDescription("Test Description");
        requestDto.setProjectId(1L);
        requestDto.setPriority(Priority.MEDIUM);
    }

    @Test
    @DisplayName("createTask() should default status to TODO and leave unassigned when no assignedUserId given")
    void createTask_noAssignedUser_savesWithTodoStatusAndNoAssignee() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskMapper.toEntity(requestDto)).thenReturn(task);
        TaskResponseDto expectedResponse = new TaskResponseDto();
        expectedResponse.setStatus(TaskStatus.TODO);
        when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

        TaskResponseDto result = taskService.createTask(requestDto);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getAssignedUser()).isNull();
        verify(userRepository, never()).findById(any());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("createTask() should throw ResourceNotFoundException when project does not exist")
    void createTask_projectNotFound_throwsResourceNotFoundException() {
        requestDto.setProjectId(99L);
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.createTask(requestDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTask() should assign user when assignedUserId given")
    void createTask_withAssignedUserId_assignsUser() {
        requestDto.setAssignedUserId(2L);
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskMapper.toEntity(requestDto)).thenReturn(task);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedUser));
        when(taskMapper.toResponseDto(task)).thenReturn(new TaskResponseDto());

        taskService.createTask(requestDto);

        assertThat(task.getAssignedUser()).isEqualTo(assignedUser);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    @DisplayName("createTask() should throw ResourceNotFoundException when assignedUserId does not exist")
    void createTask_assignedUserNotFound_throwsResourceNotFoundException() {
        requestDto.setAssignedUserId(99L);
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskMapper.toEntity(requestDto)).thenReturn(task);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(requestDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTaskById() should return task when found")
    void getTaskById_existingTask_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponseDto expectedResponse = new TaskResponseDto();
        expectedResponse.setTitle("Test Task");
        when(taskMapper.toResponseDto(task)).thenReturn(expectedResponse);

        TaskResponseDto result = taskService.getTaskById(1L);

        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("getTaskById() should throw ResourceNotFoundException when not found")
    void getTaskById_notFound_throwsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateTask() should update title, description, dueDate, priority")
    void updateTask_validRequest_updatesFields() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskUpdateDto updateDto = new TaskUpdateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Desc");
        updateDto.setDueDate(java.time.LocalDate.now());
        updateDto.setPriority(Priority.HIGH);
        when(taskMapper.toResponseDto(task)).thenReturn(new TaskResponseDto());

        taskService.updateTask(1L, updateDto);

        assertThat(task.getTitle()).isEqualTo("Updated Title");
        assertThat(task.getDescription()).isEqualTo("Updated Desc");
        assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("updateTask() should throw ResourceNotFoundException when task not found")
    void updateTask_notFound_throwsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(1L, new TaskUpdateDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteTask() should delete when task exists")
    void deleteTask_existingTask_deletesSuccessfully() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask() should throw ResourceNotFoundException when task not found")
    void deleteTask_notFound_throwsResourceNotFoundException() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("assignTask() should set assigned user")
    void assignTask_validRequest_assignsUser() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignedUser));
        when(taskMapper.toResponseDto(task)).thenReturn(new TaskResponseDto());

        TaskAssignDto assignDto = new TaskAssignDto();
        assignDto.setAssignedUserId(2L);

        taskService.assignTask(1L, assignDto);

        assertThat(task.getAssignedUser()).isEqualTo(assignedUser);
    }

    @Test
    @DisplayName("assignTask() should throw ResourceNotFoundException when task not found")
    void assignTask_taskNotFound_throwsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        TaskAssignDto assignDto = new TaskAssignDto();
        assignDto.setAssignedUserId(2L);

        assertThatThrownBy(() -> taskService.assignTask(1L, assignDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("assignTask() should throw ResourceNotFoundException when user not found")
    void assignTask_userNotFound_throwsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        TaskAssignDto assignDto = new TaskAssignDto();
        assignDto.setAssignedUserId(99L);

        assertThatThrownBy(() -> taskService.assignTask(1L, assignDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateTaskStatus() should update status")
    void updateTaskStatus_validRequest_updatesStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponseDto(task)).thenReturn(new TaskResponseDto());
        TaskStatusUpdateDto statusDto = new TaskStatusUpdateDto();
        statusDto.setStatus(TaskStatus.IN_PROGRESS);

        taskService.updateTaskStatus(1L, statusDto);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateTaskStatus() should throw ResourceNotFoundException when task not found")
    void updateTaskStatus_notFound_throwsResourceNotFoundException() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTaskStatus(1L, new TaskStatusUpdateDto()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getProjectTasks() should return filtered page of tasks")
    void getProjectTasks_validRequest_returnsPage() {
        Pageable pageable = Pageable.unpaged();
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findByProjectIdWithFilter(1L, TaskStatus.TODO, Priority.MEDIUM, pageable))
                .thenReturn(taskPage);
        when(taskMapper.toResponseDto(task)).thenReturn(new TaskResponseDto());

        Page<TaskResponseDto> result = taskService.getProjectTasks(1L, TaskStatus.TODO, Priority.MEDIUM, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("getUserTasks() should return authenticated user's tasks")
    void getUserTasks_authenticatedUser_returnsOwnTasks() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("assignee@example.com");

            when(userRepository.findByEmail("assignee@example.com")).thenReturn(Optional.of(assignedUser));

            Pageable pageable = Pageable.unpaged();
            Page<Task> taskPage = new PageImpl<>(List.of(task));
            when(taskRepository.findByAssignedUserIdWithFilter(2L, null, null, pageable))
                    .thenReturn(taskPage);
            when(taskMapper.toResponseDto(task)).thenReturn(new TaskResponseDto());

            Page<TaskResponseDto> result = taskService.getUserTasks(null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Test
    @DisplayName("getUserTasks() should throw ResourceNotFoundException when authenticated user not found")
    void getUserTasks_authenticatedUserNotFound_throwsResourceNotFoundException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("ghost@example.com");

            when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getUserTasks(null, null, Pageable.unpaged()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}