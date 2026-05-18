package com.teamtask.controller;

import com.teamtask.dto.request.TaskRequest;
import com.teamtask.dto.request.TaskStatusUpdateRequest;
import com.teamtask.dto.response.TaskResponse;
import com.teamtask.enums.TaskPriority;
import com.teamtask.enums.TaskStatus;
import com.teamtask.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin("*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponse createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        return taskService.createTask(request, authentication.getName());
    }

    @GetMapping
    public List<TaskResponse> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            Authentication authentication
    ) {
        return taskService.getTasks(
                authentication.getName(),
                status,
                priority,
                projectId,
                assigneeId
        );
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return taskService.getTaskById(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication
    ) {
        return taskService.updateTask(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public String deleteTask(
            @PathVariable Long id,
            Authentication authentication
    ) {
        taskService.deleteTask(id, authentication.getName());
        return "Task deleted successfully";
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request,
            Authentication authentication
    ) {
        return taskService.updateTaskStatus(id, request, authentication.getName());
    }
}