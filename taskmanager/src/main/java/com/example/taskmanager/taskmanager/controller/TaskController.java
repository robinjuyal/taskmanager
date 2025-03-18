package com.example.taskmanager.taskmanager.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.taskmanager.taskmanager.dto.TaskDTO;
import com.example.taskmanager.taskmanager.model.Priority;
import com.example.taskmanager.taskmanager.model.Status;
import com.example.taskmanager.taskmanager.service.TaskService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskService taskService;
    
    @Value("${app.task.page-size:10}")
    private int defaultPageSize;
    
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
            @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        TaskDTO createdTask = taskService.createTask(userDetails.getUsername(), taskDTO);
        return ResponseEntity.created(URI.create("/api/tasks/" + createdTask.getId()))
                .body(createdTask);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTaskById(id, userDetails.getUsername()));
    }
    
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String sortBy,
            @AuthenticationPrincipal UserDetails userDetails) {
        int pageSize = size != null ? size : defaultPageSize;
        return ResponseEntity.ok(taskService.getTasks(
                userDetails.getUsername(), page, pageSize, status, priority, sortBy));
    }
    
    @GetMapping("/prioritized")
    public ResponseEntity<List<TaskDTO>> getPrioritizedTasks(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getPrioritizedTasks(userDetails.getUsername(), limit));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.updateTask(id, taskDTO, userDetails.getUsername()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
