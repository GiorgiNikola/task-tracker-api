package com.giorgi.tasktrackerapi.controller;

import com.giorgi.tasktrackerapi.dto.task.*;
import com.giorgi.tasktrackerapi.enums.Priority;
import com.giorgi.tasktrackerapi.enums.TaskStatus;
import com.giorgi.tasktrackerapi.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping()
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto responseDto = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        TaskResponseDto responseDto = taskService.getTaskById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id,
                                                      @Valid @RequestBody TaskUpdateDto request) {
        TaskResponseDto responseDto = taskService.updateTask(id, request);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TaskResponseDto> assignTask(@PathVariable Long id,
                                                      @Valid @RequestBody TaskAssignDto request) {
        TaskResponseDto responseDto = taskService.assignTask(id, request);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(@PathVariable Long id,
                                                            @Valid @RequestBody TaskStatusUpdateDto request) {
        TaskResponseDto responseDto = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/project")
    public ResponseEntity<Page<TaskResponseDto>> getProjectTasks(@RequestParam Long projectId,
                                                                 @RequestParam(required = false) TaskStatus status,
                                                                 @RequestParam(required = false) Priority priority,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponseDto> responseDtos = taskService.getProjectTasks(projectId, status, priority, pageable);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<TaskResponseDto>> getUserTasks(@RequestParam(required = false) TaskStatus status,
                                                              @RequestParam(required = false) Priority priority,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaskResponseDto> responseDtos = taskService.getUserTasks(status, priority, pageable);
        return ResponseEntity.ok(responseDtos);
    }
}
