package com.teamtask.controller;

import com.teamtask.dto.request.ProjectRequest;
import com.teamtask.dto.response.ProjectResponse;
import com.teamtask.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin("*")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ProjectResponse createProject(
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        return projectService.createProject(request, authentication.getName());
    }

    @GetMapping
    public List<ProjectResponse> getProjects(Authentication authentication) {
        return projectService.getProjects(authentication.getName());
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return projectService.getProjectById(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request,
            Authentication authentication
    ) {
        return projectService.updateProject(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public String deleteProject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        projectService.deleteProject(id, authentication.getName());
        return "Project deleted successfully";
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ProjectResponse addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        return projectService.addMember(projectId, userId, authentication.getName());
    }
}