package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.project.ProjectRequestDto;
import com.giorgi.tasktrackerapi.dto.project.ProjectResponseDto;
import com.giorgi.tasktrackerapi.entity.Project;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.enums.Role;
import com.giorgi.tasktrackerapi.exception.ProjectHasTasksException;
import com.giorgi.tasktrackerapi.exception.ResourceNotFoundException;
import com.giorgi.tasktrackerapi.mapper.ProjectMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;

    private User owner;

    private ProjectRequestDto requestDto;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");

        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setRole(Role.MANAGER);
        project.setOwner(owner);

        requestDto = new ProjectRequestDto();
        requestDto.setName("Test Project");
        requestDto.setDescription("Test Description");
    }

    @Test
    @DisplayName("createProject() should set authenticated user as owner")
    void createProject_validRequest_setsAuthenticatedUserAsOwner() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("owner@example.com");

            when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
            when(projectMapper.toEntity(requestDto)).thenReturn(project);

            ProjectResponseDto expectedResponse = new ProjectResponseDto();
            expectedResponse.setOwnerEmail("owner@example.com");
            when(projectMapper.toResponseDto(project)).thenReturn(expectedResponse);

            ProjectResponseDto result = projectService.createProject(requestDto);

            assertThat(result.getOwnerEmail()).isEqualTo("owner@example.com");
            verify(projectRepository).save(project);
            assertThat(project.getOwner()).isEqualTo(owner);
        }
    }

    @Test
    @DisplayName("createProject() should throw ResourceNotFoundException when authenticated user not found")
    void createProject_authenticatedUserNotFound_throwsResourceNotFoundException() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("owner@example.com");

            when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> projectService.createProject(requestDto))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(projectRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("deleteProject() should throw ResourceNotFoundException when project does not exist")
    void deleteProject_projectNotFound_throwsResourceNotFoundException() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> projectService.deleteProject(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).existsByProjectId(any());
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteProject() should throw ProjectHasTasksException when tasks exist")
    void deleteProject_hasExistingTasks_throwsProjectHasTasksException() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.existsByProjectId(1L)).thenReturn(true);

        assertThatThrownBy(() -> projectService.deleteProject(1L))
                .isInstanceOf(ProjectHasTasksException.class);

        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteProject() should delete when project exists with no tasks")
    void deleteProject_noTasks_deletesSuccessfully() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.existsByProjectId(1L)).thenReturn(false);

        projectService.deleteProject(1L);

        verify(projectRepository).deleteById(1L);
    }

    @Test
    @DisplayName("getProjectById() should return project when found")
    void getProjectById_existingProject_returnsProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        ProjectResponseDto expectedResponse = new ProjectResponseDto();
        expectedResponse.setName("Test Project");
        when(projectMapper.toResponseDto(project)).thenReturn(expectedResponse);

        ProjectResponseDto result = projectService.getProjectById(1L);

        assertThat(result.getName()).isEqualTo("Test Project");
    }

    @Test
    @DisplayName("getProjectById() should throw ResourceNotFoundException when not found")
    void getProjectById_notFound_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateProject() should update name and description")
    void updateProject_validRequest_updatesFields() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectRequestDto updateRequest = new ProjectRequestDto();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");

        ProjectResponseDto expectedResponse = new ProjectResponseDto();
        expectedResponse.setName("Updated Name");
        when(projectMapper.toResponseDto(project)).thenReturn(expectedResponse);

        ProjectResponseDto result = projectService.updateProject(1L, updateRequest);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(project.getName()).isEqualTo("Updated Name");
        assertThat(project.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("updateProject() should throw ResourceNotFoundException when project not found")
    void updateProject_notFound_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(1L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
