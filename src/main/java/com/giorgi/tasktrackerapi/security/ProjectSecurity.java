package com.giorgi.tasktrackerapi.security;

import com.giorgi.tasktrackerapi.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProjectSecurity {
    private final ProjectRepository projectRepository;

    public boolean isProjectOwner(Long projectId, Authentication auth) {
        String email = auth.getName();
        return projectRepository.existsByIdAndOwnerEmail(projectId, email);
    }
}
