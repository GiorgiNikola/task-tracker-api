package com.giorgi.tasktrackerapi.controller;

import com.giorgi.tasktrackerapi.dto.project.ProjectRequestDto;
import com.giorgi.tasktrackerapi.dto.project.ProjectResponseDto;
import com.giorgi.tasktrackerapi.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping()
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto request) {
        ProjectResponseDto projectResponseDto = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable Long id,
                                                            @Valid @RequestBody ProjectRequestDto request) {
        ProjectResponseDto projectResponseDto = projectService.updateProject(id, request);
        return ResponseEntity.ok(projectResponseDto);
    }

    @Operation(description = "Returns 403 for both unauthorized access and nonexistent IDs, to prevent resource enumeration.")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProject(@PathVariable Long id) {
        ProjectResponseDto projectResponseDto = projectService.getProjectById(id);
        return ResponseEntity.ok(projectResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
