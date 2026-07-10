package com.giorgi.tasktrackerapi.service;

import com.giorgi.tasktrackerapi.dto.project.ProjectRequestDto;
import com.giorgi.tasktrackerapi.dto.project.ProjectResponseDto;
import com.giorgi.tasktrackerapi.entity.Project;
import com.giorgi.tasktrackerapi.entity.User;
import com.giorgi.tasktrackerapi.mapper.ProjectMapper;
import com.giorgi.tasktrackerapi.repository.ProjectRepository;
import com.giorgi.tasktrackerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ProjectResponseDto createProject(ProjectRequestDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        Project project = projectMapper.toEntity(request);
        project.setOwner(owner);
        projectRepository.save(project);

        return projectMapper.toResponseDto(project);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isProjectOwner(#projectId, authentication)")
    public void deleteProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found");
        }
        projectRepository.deleteById(projectId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isProjectOwner(#projectId, authentication)")
    public ProjectResponseDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return projectMapper.toResponseDto(project);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.isProjectOwner(#projectId, authentication)")
    public ProjectResponseDto updateProject(Long projectId, ProjectRequestDto request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return projectMapper.toResponseDto(project);
    }
}
